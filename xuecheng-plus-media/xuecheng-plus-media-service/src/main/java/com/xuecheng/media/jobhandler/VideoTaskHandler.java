package com.xuecheng.media.jobhandler;

import com.xuecheng.media.model.po.MediaProcess;
import com.xuecheng.media.service.MediaFileService;
import com.xuecheng.media.service.MediaProcessService;
import com.xuecheng.utils.Mp4VideoUtil;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;

import java.io.*;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @Author gc
 * @Description 视频处理任务
 * @DateTime: 2025/5/19 0:48
 **/
@Component
@Slf4j
public class VideoTaskHandler {
    @Autowired
    MediaProcessService mediaProcessService;
    @Autowired
    MediaFileService mediaFileService;
    @Value("${videoprocess.ffmpegpath}")
    String ffmpeg_path;

    @XxlJob("videoTranscoding")
    public void videoTranscoding() {
        int shardIndex = XxlJobHelper.getShardIndex();
        int shardTotal = XxlJobHelper.getShardTotal();
        //获取系统核心数
        int count = Runtime.getRuntime().availableProcessors();
        //获取任务
        List<MediaProcess> mediaProcessList = mediaProcessService.getMediaProcessListByShardIndex(shardIndex, shardTotal, count);
        if (mediaProcessList == null) {
            log.debug("获取任务失败");
            return;
        }
        int size = mediaProcessList.size();
        log.debug("获取到{}个视频处理任务", size);
        if (size <= 0) {
            return;
        }
        CountDownLatch countDownLatch = new CountDownLatch(size);
        //创建线程池，设置线程数为任务数
        ExecutorService threadPool = Executors.newFixedThreadPool(size);
        for (MediaProcess mediaProcess:mediaProcessList) {
            threadPool.execute(() -> {
                try {
                    String fileId = mediaProcess.getFileId();
                    //1.分布式锁控制，开启任务
                    //获取任务id
                    Long taskId = mediaProcess.getId();
                    //进行分布式锁控制，开启任务
                    int ss = mediaProcessService.startTask(taskId);
                    if (ss <= 0) {
                        return;
                    }
                    //2.处理视频转码
                    log.debug("开始处理视频，任务id：{}，视频id：{}", taskId, fileId);
                    //获取桶和对象路径并下载到本地
                    String bucket = mediaProcess.getBucket();
                    String objectPath = mediaProcess.getFilePath();
                    InputStream minioObjectInputStream = mediaFileService.getMinioObject(bucket, objectPath);
                    if (minioObjectInputStream == null) {
                        log.debug("下载视频失败，任务id：{}，视频id：{}", taskId, fileId);
                        mediaProcessService.saveProcessFinishStatus(taskId, "3", fileId, null, "下载视频失败");
                        return;
                    }
                    File minioTempFile = null;
                    FileOutputStream fileOutputStream = null;
                    try {
                        minioTempFile = File.createTempFile("minioTempFile", ".temp");
                    } catch (IOException e) {
                        log.debug("创建临时视频失败，任务id：{}，视频id：{}", taskId, fileId);
                        mediaProcessService.saveProcessFinishStatus(taskId, "3", fileId, null, "创建临时视频失败");
                        return;
                    }
                    try {
                        fileOutputStream = new FileOutputStream(minioTempFile);
                        IOUtils.copy(minioObjectInputStream, fileOutputStream);
                    } catch (IOException e) {
                        log.debug("拷贝视频流失败，任务id：{}，视频id：{}", taskId, fileId);
                        mediaProcessService.saveProcessFinishStatus(taskId, "3", fileId, null, "拷贝视频流失败");
                        e.printStackTrace();
                        return;
                    } finally {
                        try {
                            fileOutputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    File mp4TempFile = null;
                    try {
                        mp4TempFile = File.createTempFile("mp4TempFile", ".mp4");
                    } catch (IOException e) {
                        log.debug("创建转码临时视频失败，任务id：{}，视频id：{}", taskId, fileId);
                        mediaProcessService.saveProcessFinishStatus(taskId, "3", fileId, null, "创建转码临时视频失败");
                        return;
                    }
                    //开始视频转码
                    log.debug("开始视频转码，任务id：{}，视频id：{}", taskId, fileId);
                    //ffmpeg的路径
                    //String ffmpeg_path = "D:\\soft\\ffmpeg\\ffmpeg.exe";//ffmpeg的安装位置
                    //源avi视频的路径
                    String video_path = minioTempFile.getAbsolutePath();
                    //转换后mp4文件的名称
                    String mp4_name = mp4TempFile.getName();
                    //转换后mp4文件的路径
                    String mp4_path = mp4TempFile.getAbsolutePath();
                    //创建工具类对象
                    Mp4VideoUtil videoUtil = new Mp4VideoUtil(ffmpeg_path, video_path, mp4_name, mp4_path);
                    try {
                        String outString = videoUtil.generateMp4();
                        log.debug("视频转码成功！成功信息：{}", outString);
                    } catch (Exception e) {
                        log.debug("视频转码失败，任务id：{}：，文件id：{}", taskId, fileId, e.getMessage());
                        mediaProcessService.saveProcessFinishStatus(taskId, "3", fileId, null, e.getMessage());
                        return;
                    } finally {
                        System.gc();
                        minioTempFile.delete();
                    }

                    //3.视频转码成功后
                    //将转换成功的视频上传minio
                    FileInputStream mp4TempFileFIS = null;
                    String mp4TempFileMD5 = null;
                    try {
                        mp4TempFileFIS = new FileInputStream(mp4TempFile);
                        mp4TempFileMD5 = DigestUtils.md5DigestAsHex(mp4TempFileFIS);
                    } catch (IOException e) {
                        log.debug("获取转码后的视频流失败，任务id：{}：，文件id：{}", taskId, fileId);
                        mediaProcessService.saveProcessFinishStatus(taskId, "3", fileId, null, "获取转码后的视频流失败");
                        e.printStackTrace();
                        return;
                    } finally {
                        try {
                            mp4TempFileFIS.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    //上传转码后的mp4文件到minio
                    String videoObjectPath = mediaFileService.getVideoObjectPath(fileId) + "/transcoding/" + fileId + ".mp4";
                    String contentType = mediaFileService.getMimeType(".mp4");
                    Boolean uploadRes = mediaFileService.localFileToMinio(bucket, videoObjectPath, mp4TempFile.getAbsolutePath(), contentType);
                    if (!uploadRes) {
                        log.debug("上传转码后的视频失败，任务id：{}：，文件id：{}", taskId, fileId);
                        mediaProcessService.saveProcessFinishStatus(taskId, "3", fileId, null, "上传转码后的视频失败");
                    }
                    //校验上传成功后的文件
                    InputStream minioObjectIS = mediaFileService.getMinioObject(bucket, videoObjectPath);
                    File minio = null;
                    try {
                        minio = File.createTempFile("minio", ".temp");
                        inputStreamToTempFile(minioObjectIS, minio);
                        String fileMD5 = mediaFileService.getFileMD5(minio);
                        if (!mp4TempFileMD5.equals(fileMD5)) {
                            log.debug("上传转码视频后校验md5失败，任务id：{}：，文件id：{}", taskId, fileId);
                            mediaProcessService.saveProcessFinishStatus(taskId, "3", fileId, null, "上传转码视频后校验md5失败");
                            return;
                        }
                    } catch (IOException e) {
                        log.debug("下载转码后的视频失败，任务id：{}：，文件id：{}", taskId, fileId);
                        mediaProcessService.saveProcessFinishStatus(taskId, "3", fileId, null, "下载转码后的视频失败");
                        e.printStackTrace();
                        return;
                    } finally {
                        System.gc();
                        minio.delete();
                    }
                    String url = "/" + bucket + "/" + videoObjectPath;
                    //保存成功的状态信息
                    mediaProcessService.saveProcessFinishStatus(taskId, "2", fileId, url, null);
                    log.debug("=====ok=====视频转码处理成功！任务id：{}，文件id：{}", taskId, fileId);
                } finally {
                    countDownLatch.countDown();
                    long taskCount = countDownLatch.getCount();
                    log.debug("=========还有{}个任务数待处理！", taskCount);
                }
            });
        }
        try {
            countDownLatch.await(30, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            log.debug("==========线程计数器退出等待异常！{}", e.getMessage());
            e.printStackTrace();
        }
    }

    private void inputStreamToTempFile(InputStream inputStream, File outputFile) {
        OutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(outputFile);
            IOUtils.copy(inputStream, outputStream);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
