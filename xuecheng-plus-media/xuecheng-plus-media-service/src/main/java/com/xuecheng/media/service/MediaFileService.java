package com.xuecheng.media.service;


import com.xuecheng.media.model.dto.MediaFilesDTO;
import com.xuecheng.media.model.dto.QueryMediaParamsDto;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.model.PageParams;
import com.xuecheng.model.PageResult;
import com.xuecheng.model.RestResponse;
import io.minio.errors.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * @author Mr.M
 * @version 1.0
 * @description 媒资文件管理业务类
 * @date 2022/9/10 8:55
 */
public interface MediaFileService {

    /**
     * @param pageParams          分页参数
     * @param queryMediaParamsDto 查询条件
     * @return com.xuecheng.base.model.PageResult<com.xuecheng.media.model.po.MediaFiles>
     * @description 媒资文件查询方法
     * @author Mr.M
     * @date 2022/9/10 8:57
     */
    public PageResult<MediaFiles> queryMediaFiels(Long companyId, PageParams pageParams, QueryMediaParamsDto queryMediaParamsDto);

    /**
     * 上传媒资文件
     * @param companyId 机构id
     * @param mediaFilesDTO 媒资文件传输dto
     * @param localFilePath 本地文件路径
     */
    MediaFilesDTO uploadFile(Long companyId, MediaFilesDTO mediaFilesDTO, String localFilePath,String objectPath) throws Exception;

    /**
     * 保存MediaFiles到数据库
     * @param mediaFilesDTO 传输模型
     * @return
     */
    public MediaFilesDTO saveMediaFiles(MediaFilesDTO mediaFilesDTO);

    /**
     * 检查文件是否已经上传
     * @param fileMd5
     * @return
     */
    RestResponse<Boolean> checkFile(String fileMd5);

    /**
     * 检查分片是否已经上传
     * @param fileMd5 源文件的md5
     * @param chunk 第几个分片
     * @return
     */
    RestResponse<Boolean> checkChunk(String fileMd5, int chunk) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException, Exception;

    /**
     * 上传分片文件
     * @param fileMd5 源文件md5
     * @param chunk 第几个分片
     * @param localFilePath 本地文件路径
     * @return
     */
    RestResponse uploadChunk(String fileMd5, int chunk, String localFilePath);

    /**
     * 合并minio中的文件
     * @param fileMd5 源文件md5
     * @param fileName 源文件名字
     * @param chunkTotal 一共有多少分块
     * @return
     */
    RestResponse mergeChunks(String fileMd5, String fileName, int chunkTotal);


    /**
     * 获取minio中的文件
     *
     * @param bucket     桶名字
     * @param objectPath 桶中的路径
     * @return
     */
    public InputStream getMinioObject(String bucket, String objectPath);

    /**
     * 将本地文件上传到Minio分布式文件系统中
     * @param bucket        桶名字
     * @param objectPath    桶里面的对象路径（minio中的文件路径）
     * @param localFilePath 本地文件路径
     * @param contentType   文件类型
     */
    public Boolean localFileToMinio(String bucket, String objectPath, String localFilePath, String contentType);

    /**
     * 将本地文件上传到Minio分布式文件系统中
     * @param bucket        桶名字
     * @param objectPath    桶里面的对象路径（minio中的文件路径）
     * @param localFilePath 本地文件路径
     */
    public Boolean localFileToMinio(String bucket, String objectPath, String localFilePath);

    /**
     * 得到视频对象的objectPath
     *
     * @param fileMd5 源文件的md5
     * @return
     */
    public String getVideoObjectPath(String fileMd5);

    /**
     * 获取文件的二进制文件类型
     *
     * @param fileNameExtension 文件的拓展名
     * @return
     */
    public String getMimeType(String fileNameExtension);


    /**
     * 获取文件的md5值
     *
     * @param file 文件
     * @return
     * @throws IOException
     */
    public String getFileMD5(File file);


    /**
     * 根据媒资id获取文件id
     * @param mediaId 媒资文件id
     * @return
     */
    RestResponse<String> queryMediaFielsById(String mediaId);
}
