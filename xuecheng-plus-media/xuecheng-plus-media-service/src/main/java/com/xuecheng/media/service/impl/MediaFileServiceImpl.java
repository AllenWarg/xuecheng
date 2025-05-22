package com.xuecheng.media.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import com.xuecheng.execption.XueChengException;
import com.xuecheng.media.mapper.MediaFilesMapper;
import com.xuecheng.media.model.dto.MediaFilesDTO;
import com.xuecheng.media.model.dto.QueryMediaParamsDto;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.model.po.MediaProcess;
import com.xuecheng.media.service.MediaFileService;
import com.xuecheng.media.service.MediaProcessService;
import com.xuecheng.model.PageParams;
import com.xuecheng.model.PageResult;
import com.xuecheng.model.RestResponse;
import io.minio.*;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * @author gc
 * @version 1.0
 * @description 媒资文件管理
 * @date 2022/9/10 8:58
 */
@Service
@Slf4j
public class MediaFileServiceImpl implements MediaFileService {

    @Autowired
    MediaFilesMapper mediaFilesMapper;

    @Autowired
    MinioClient minioClient;

    @Autowired
    MediaFileService mediaFileServiceProxy;

    @Autowired
    MediaProcessService mediaProcessService;

    @Value("${minio.endpoint}")
    String url;
    @Value("${minio.bucket.files}")
    String filesBucket;
    @Value("${minio.bucket.videofiles}")
    String videoFilesBucket;

    /**
     * 根据媒资id获取文件id
     * @param mediaId 媒资文件id
     * @return
     */
    public RestResponse<String> queryMediaFielsById(String mediaId){
        MediaFiles mediaFiles = mediaFilesMapper.selectById(mediaId);
        if (mediaFiles==null){
            return RestResponse.validfail("未上传媒资文件");
        }
        if (StringUtils.isEmpty(mediaFiles.getUrl())){
            return RestResponse.validfail("媒体文件正在转码中");
        }
        return RestResponse.success(mediaFiles.getUrl());
    }

    @Override
    public PageResult<MediaFiles> queryMediaFiels(Long companyId, PageParams pageParams, QueryMediaParamsDto queryMediaParamsDto) {

        //构建查询条件对象
        LambdaQueryWrapper<MediaFiles> queryWrapper = new LambdaQueryWrapper<>();

        //分页对象
        Page<MediaFiles> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());
        // 查询数据内容获得结果
        Page<MediaFiles> pageResult = mediaFilesMapper.selectPage(page, queryWrapper);
        // 获取数据列表
        List<MediaFiles> list = pageResult.getRecords();
        // 获取数据总数
        long total = pageResult.getTotal();
        // 构建结果集
        PageResult<MediaFiles> mediaListResult = new PageResult<>(list, total, pageParams.getPageNo(), pageParams.getPageSize());
        return mediaListResult;
    }


    public MediaFilesDTO uploadFile(Long companyId, MediaFilesDTO mediaFilesDTO, String localFilePath,String objectPath) throws Exception {
        String md5 = getFileMD5(new File(localFilePath));
        //查询该文件是否已经上传过了
        MediaFiles selectMediaFiles = mediaFilesMapper.selectById(md5);
        if (selectMediaFiles != null) {
            new File(localFilePath).delete();
            BeanUtils.copyProperties(selectMediaFiles, mediaFilesDTO);
            return mediaFilesDTO;
        }
        String fileNameExtension = mediaFilesDTO.getFilename()
                .substring(mediaFilesDTO.getFilename().lastIndexOf("."));
        String mimeType = getMimeType(fileNameExtension);
        String bucket = "";
        switch (mimeType.split("/")[0]) {
            case "image":
                bucket = filesBucket;
                break;
            case "video":
                bucket = videoFilesBucket;
                break;
            default:
                bucket = filesBucket;
                break;
        }
        if (StringUtils.isEmpty(objectPath)){
           objectPath = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyy/MM/dd/")) + md5 + "/" + md5 + fileNameExtension;
        }

        //将本地文件上传到minio中
        localFileToMinio(bucket, objectPath, localFilePath, mimeType);
        InputStream minioObject = getMinioObject(bucket, objectPath);
        String minioObjectMD5 = getFileMD5(minioObject);
        if (!md5.equals(minioObjectMD5)) {
            XueChengException.cast("文件上传失败！请重试。。。");
        }
        mediaFilesDTO.setId(md5);
        mediaFilesDTO.setFileId(md5);
        mediaFilesDTO.setCompanyId(companyId);
        mediaFilesDTO.setCompanyName("我就是000");
        mediaFilesDTO.setFilePath(objectPath);
        mediaFilesDTO.setTags("课程图片");
        //将信息保存到数据库,通过代理对象调用保证事务受控制。
        MediaFilesDTO resMediaFiles = mediaFileServiceProxy.saveMediaFiles(mediaFilesDTO);
        return resMediaFiles;
    }

    /**
     * 检查文件是否已经上传
     *
     * @param fileMd5 文件md5
     * @return
     */
    public RestResponse<Boolean> checkFile(String fileMd5) {
        Boolean res = null;
        String msg = null;
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileMd5);
        if (mediaFiles == null) {
            res = false;
        } else {
            InputStream minioObject
                    = getMinioObject(mediaFiles.getBucket(), mediaFiles.getFilePath());
            if (minioObject != null) {
                res = true;
            }
        }
        return RestResponse.success(res);
    }

    /**
     * 检查分片是否已经上传
     *
     * @param fileMd5 源文件的md5
     * @param chunk   第几个分片
     * @return
     */
    public RestResponse<Boolean> checkChunk(String fileMd5, int chunk) {
        RestResponse<Boolean> res = null;
        String bucket = videoFilesBucket;
        String videoObjectPath = getVideoObjectPath(fileMd5);
        InputStream minioObject = getMinioObject(bucket, videoObjectPath + "/chunk/" + chunk);
        if (minioObject == null) {
            res = RestResponse.success(false);
        } else {
            res = RestResponse.success(true);
        }
        return res;
    }

    /**
     * 上传分片文件
     *
     * @param fileMd5       源文件md5
     * @param chunk         第几个分片
     * @param localFilePath 本地文件路径
     * @return
     */
    public RestResponse<Boolean> uploadChunk(String fileMd5, int chunk, String localFilePath) {
        String bucket = videoFilesBucket;
        String objectPath = getVideoObjectPath(fileMd5) + "/chunk/" + chunk;
        Boolean aBoolean = localFileToMinio(bucket, objectPath, localFilePath);
        return RestResponse.success(aBoolean);
    }

    /**
     * 合并minio中的文件
     *
     * @param fileMd5    源文件md5
     * @param fileName   源文件名字
     * @param chunkTotal 一共有多少分块
     * @return
     */
    public RestResponse<Boolean> mergeChunks(String fileMd5, String fileName, int chunkTotal) {
        MediaFiles mediaFilesCheck=mediaFilesMapper.selectById(fileMd5);
        if (mediaFilesCheck!=null){
            return RestResponse.success(true);
        }
        String bucket = videoFilesBucket;
        String videoObjectPath = getVideoObjectPath(fileMd5);
        String type = fileName.substring(fileName.lastIndexOf("."));
        //构建minio中分块对象路径
        List<String> objectPaths=new ArrayList<>();
        for (int i = 0; i < chunkTotal; i++) {
            objectPaths.add(videoObjectPath + "/chunk/" + i);
        }
        //合并分块
        InputStream minioObject
                = mergeMinioChunks(bucket, objectPaths, fileMd5, fileName);
        if (minioObject==null){
            log.error("合并文件失败，文件MD5："+fileMd5);
            return RestResponse.validfail(false,"合并文件失败，文件MD5："+fileMd5);
        }
        //将minio中合成的文件下载下来对比md5值
        File minioTempFile = null;
        FileOutputStream fileOutputStream = null;
        try {
            minioTempFile = File.createTempFile("minio", ".temp");
            fileOutputStream = new FileOutputStream(minioTempFile);
            IOUtils.copy(minioObject, fileOutputStream);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                fileOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //校验合并合并的文件的md5值
        String minioMD5 = getFileMD5(minioTempFile);
        if (!fileMd5.equals(minioMD5)) {
            log.error("文件合并校验失败！"+fileMd5);
            return RestResponse.validfail(false, "文件合并md5校验失败！");
        }
        //保存到数据库中
        MediaFilesDTO mediaFilesDTO = new MediaFilesDTO();
        mediaFilesDTO.setFileId(fileMd5);
        mediaFilesDTO.setId(fileMd5);
        mediaFilesDTO.setCompanyId(594000L);
        mediaFilesDTO.setCompanyName("我就是000");
        mediaFilesDTO.setFilename(fileName);
        mediaFilesDTO.setFilePath(videoObjectPath + "/merge/" + fileMd5 + type);
        mediaFilesDTO.setTags("课程视频");
        mediaFilesDTO.setFileSize(minioTempFile.length());
        //保存文件信息到数据库中
        mediaFileServiceProxy.saveMediaFiles(mediaFilesDTO);
        //获取保存在到数据库中的文件信息
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileMd5);
        //删除本地临时minio文件
        System.gc();
        minioTempFile.delete();
        //删除minio中分块文件
        deleteMinioObjects(bucket,objectPaths);
        log.info("文件上传成功！！！！！" + fileMd5);
        return RestResponse.success(true);
    }

    /**
     * 合并minio中的分块
     * @param bucket 桶名字
     * @param objectPaths 分块对象路径
     * @param fileMd5 源文件md5
     * @param fileName 源文件名称
     * @return
     */
    private InputStream mergeMinioChunks(String bucket,List<String> objectPaths,String fileMd5,String fileName){
        //需要合并文件对象
        List<ComposeSource> composeSources= new ArrayList<>();
        //构建合并对象
        for (String item : objectPaths) {
            composeSources.add(ComposeSource.builder()
                    .bucket(bucket)
                    .object(item)
                    .build());
        }
        String videoObjectPath = getVideoObjectPath(fileMd5);
        String type = fileName.substring(fileName.lastIndexOf("."));
        ComposeObjectArgs composeObjectArgs = ComposeObjectArgs.builder()
                .bucket(bucket)
                .object(videoObjectPath + "/merge/" + fileMd5 + type)
                .sources(composeSources)
                .build();
        try {
            minioClient.composeObject(composeObjectArgs);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        InputStream minioObject = getMinioObject(bucket, videoObjectPath + "/merge/" + fileMd5 + type);
        return minioObject;
    }


    /**
     * 得到视频对象的objectPath
     *
     * @param fileMd5 源文件的md5
     * @return
     */
    public String getVideoObjectPath(String fileMd5) {
        String s1 = fileMd5.substring(0, 1);
        String s2 = fileMd5.substring(1, 2);
        String objectPath = s1 + "/" + s2 + "/" + fileMd5;
        return objectPath;
    }

    /**
     * 获取minio中的文件
     *
     * @param bucket     桶名字
     * @param objectPath 桶中的路径
     * @return
     */
    public InputStream getMinioObject(String bucket, String objectPath) {
        GetObjectArgs getObjectArgs = GetObjectArgs.builder()
                .bucket(bucket)
                .object(objectPath)
                .build();
        InputStream inputStream = null;
        try {
            inputStream = minioClient.getObject(getObjectArgs);
        } catch (Exception e) {
            return null;
        }
        return inputStream;
    }

    /**
     * 删除minio中合并之后的分块文件
     * @param bucket 桶名字
     * @param objectPaths 分块对象路径
     * @return
     */
    private void deleteMinioObjects(String bucket, List<String> objectPaths) {
        List<DeleteObject> objects = new LinkedList<>();
        for (String item : objectPaths) {
            objects.add(new DeleteObject(item));
        }
        RemoveObjectsArgs removeObjectsArgs = RemoveObjectsArgs.builder()
                .bucket(bucket)
                .objects(objects)
                .build();
        Iterable<Result<DeleteError>> results = minioClient.removeObjects(removeObjectsArgs);
        results.forEach(item->{
            try {
                DeleteError deleteError = item.get();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * 获取文件的md5值
     *
     * @param file 文件
     * @return
     * @throws IOException
     */
    public String getFileMD5(File file) {
        String md5 = null;
        if (file == null || (!file.isFile())) {
            return null;
        }
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(file);
            md5 = DigestUtils.md5DigestAsHex(fileInputStream);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                fileInputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return md5;
    }

    /**
     * 获取文件的md5值
     * @param inputStream 输入流
     * @return
     * @throws IOException
     */
    private String getFileMD5(InputStream inputStream) {
        String md5 = null;
        if (inputStream == null) {
            return null;
        }
        try {
            md5 = DigestUtils.md5DigestAsHex(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return md5;
    }

    /**
     * 获取文件的二进制文件类型
     *
     * @param fileNameExtension 文件的拓展名
     * @return
     */
    public String getMimeType(String fileNameExtension) {
        if (fileNameExtension == null||"".equals(fileNameExtension)) {
            return null;
        }
        ContentInfo extensionMatch = ContentInfoUtil.findExtensionMatch(fileNameExtension);
        String mimeType = extensionMatch.getMimeType();
        return mimeType;
    }

    /**
     * 保存数据库
     *
     * @param mediaFilesDTO mediaFiles传输模型
     * @return
     */
    @Transactional
    public MediaFilesDTO saveMediaFiles(MediaFilesDTO mediaFilesDTO) {
        MediaFiles mediaFiles = new MediaFiles();
        BeanUtils.copyProperties(mediaFilesDTO, mediaFiles);
        String fileNameExtension = mediaFiles.getFilename()
                .substring(mediaFiles.getFilename().lastIndexOf('.'));
        String mimeType = getMimeType(fileNameExtension);
        switch (mimeType.split("/")[0]) {
            case "image":
                mediaFiles.setBucket(filesBucket);
                mediaFiles.setFileType("001001");
                break;
            case "video":
                mediaFiles.setBucket(videoFilesBucket);
                mediaFiles.setFileType("001002");
                break;
            default:
                mediaFiles.setBucket(filesBucket);
                mediaFiles.setFileType("001003");
                break;
        }
        //拼接访问文件的路径
        mediaFiles.setUrl("/" + mediaFiles.getBucket() + "/" + mediaFiles.getFilePath());
        //将avi视频转码为mp4的任务保存
        if ("video/x-msvideo".equals(mimeType)){
            mediaFiles.setUrl(null);
            MediaProcess mediaProcess = new MediaProcess();
            BeanUtils.copyProperties(mediaFiles,mediaProcess);
            mediaProcessService.saveMediaProcess(mediaProcess);
        }
        mediaFiles.setFileId(mediaFiles.getId());
        mediaFiles.setStatus("1");
        int insert = mediaFilesMapper.insert(mediaFiles);
        if (insert < 0) {
            XueChengException.cast("文件上传失败，请重试！");
        }
        MediaFiles selectMediaFiles = mediaFilesMapper.selectById(mediaFiles.getId());
        if (selectMediaFiles == null) {
            XueChengException.cast("文件上传失败，请重试！");
        }
        BeanUtils.copyProperties(selectMediaFiles, mediaFilesDTO);

        return mediaFilesDTO;
    }



    /**
     * 将本地文件上传到Minio分布式文件系统中
     * @param bucket        桶名字
     * @param objectPath    桶里面的对象路径（minio中的文件路径）
     * @param localFilePath 本地文件路径
     * @param contentType   文件类型
     */
    public Boolean localFileToMinio(String bucket, String objectPath, String localFilePath, String contentType) {
        UploadObjectArgs uploadObjectArgs = null;
        try {
            uploadObjectArgs = UploadObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectPath)
                    .filename(localFilePath)
                    .contentType(contentType)
                    .build();
            // 上传文件
            minioClient.uploadObject(uploadObjectArgs);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            System.gc();
            new File(localFilePath).delete();
        }
        return true;
    }

    /**
     * 将本地文件上传到Minio分布式文件系统中
     * @param bucket        桶名字
     * @param objectPath    桶里面的对象路径（minio中的文件路径）
     * @param localFilePath 本地文件路径
     */
    public Boolean localFileToMinio(String bucket, String objectPath, String localFilePath) {
        UploadObjectArgs uploadObjectArgs = null;
        try {
            uploadObjectArgs = UploadObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectPath)
                    .filename(localFilePath)
                    .build();
            // 上传文件
            minioClient.uploadObject(uploadObjectArgs);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            new File(localFilePath).delete();
        }
        return true;
    }

}
