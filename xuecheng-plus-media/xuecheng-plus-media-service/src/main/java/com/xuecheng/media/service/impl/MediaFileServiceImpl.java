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
import com.xuecheng.media.service.MediaFileService;
import com.xuecheng.model.PageParams;
import com.xuecheng.model.PageResult;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.UploadObjectArgs;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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


    @Value("${minio.endpoint}")
    String url;
    @Value("${minio.bucket.files}")
    String filesBucket;
    @Value("${minio.bucket.videofiles}")
    String videoFilesBucket;

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


    public MediaFilesDTO uploadFile(Long companyId, MediaFilesDTO mediaFilesDTO, String localFilePath) throws Exception {
        String md5 = getFileMD5(new File(localFilePath));
        //查询该文件是否已经上传过了
        MediaFiles selectMediaFiles = mediaFilesMapper.selectById(md5);
        if (selectMediaFiles != null) {
            System.gc();
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
        String objectPath = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy/MM/dd/"))+md5+"/"+md5+fileNameExtension;
        //将本地文件上传到minio中
        localFileToMinio(bucket,objectPath,localFilePath,mimeType);
        InputStream minioObject = getMinioObject(bucket, objectPath);
        String minioObjectMD5 = getFileMD5(minioObject);
        if (!md5.equals(minioObjectMD5)){
            XueChengException.cast("文件上传失败！请重试。。。");
        }
        mediaFilesDTO.setId(md5);
        mediaFilesDTO.setFileId(md5);
        mediaFilesDTO.setCompanyId(companyId);
        mediaFilesDTO.setCompanyName("我就是000");
        mediaFilesDTO.setFilePath(objectPath);
        //将信息保存到数据库,通过代理对象调用保证事务受控制。
        MediaFilesDTO resMediaFiles = mediaFileServiceProxy.saveMediaFiles(mediaFilesDTO);
        return resMediaFiles;
    }

    private InputStream getMinioObject(String bucket,String objectPath) throws Exception{
        GetObjectArgs getObjectArgs=GetObjectArgs.builder()
                .bucket(bucket)
                .object(objectPath)
                .build();
        return minioClient.getObject(getObjectArgs);
    }

    /**
     * 获取文件的md5值
     * @param file
     * @return
     * @throws IOException
     */
    private String getFileMD5(File file) throws IOException {
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
        }finally {
            fileInputStream.close();
        }
        return md5;
    }
    /**
     * 获取文件的md5值
     * @param inputStream 输入流
     * @return
     * @throws IOException
     */
    private String getFileMD5(InputStream inputStream) throws Exception {
        String md5 = null;
        if (inputStream == null) {
            return null;
        }
        try {
            md5 = DigestUtils.md5DigestAsHex(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            inputStream.close();
        }
        return md5;
    }

    /**
     * 获取文件的二进制文件类型
     * @param fileNameExtension 文件的拓展名
     * @return
     */
    private String getMimeType(String fileNameExtension) {
        if (fileNameExtension == null) {
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
        mediaFiles.setFileId(mediaFiles.getId());
        int insert = mediaFilesMapper.insert(mediaFiles);
        if (insert < 0) {
            XueChengException.cast("文件上传失败，请重试！");
        }
        MediaFiles selectMediaFiles = mediaFilesMapper.selectById(mediaFiles.getId());
        if (selectMediaFiles==null){
            XueChengException.cast("文件上传失败，请重试！");
        }
        BeanUtils.copyProperties(selectMediaFiles,mediaFilesDTO);
        return mediaFilesDTO;
    }

    /**
     * 将本地文件上传到Minio分布式文件系统中
     *
     * @param bucket        桶名字
     * @param objectPath    桶里面的对象路径（minio中的文件路径）
     * @param localFilePath 本地文件路径
     * @param contentType   文件类型
     */
    private void localFileToMinio(String bucket, String objectPath, String localFilePath, String contentType) {
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
        } finally {
            System.gc();
            new File(localFilePath).delete();
        }
    }

    /**
     * 将本地文件上传到Minio分布式文件系统中
     *
     * @param bucket        桶名字
     * @param objectPath    桶里面的对象路径（minio中的文件路径）
     * @param localFilePath 本地文件路径
     */
    private void localFileToMinio(String bucket, String objectPath, String localFilePath) {
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
        } finally {
            System.gc();
            new File(localFilePath).delete();
        }
    }

}
