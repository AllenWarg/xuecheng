package com.xuecheng.media.service;


import com.xuecheng.media.model.dto.MediaFilesDTO;
import com.xuecheng.media.model.dto.QueryMediaParamsDto;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.model.PageParams;
import com.xuecheng.model.PageResult;

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
    MediaFilesDTO uploadFile(Long companyId, MediaFilesDTO mediaFilesDTO, String localFilePath) throws Exception;

    /**
     * 保存MediaFiles到数据库
     * @param mediaFilesDTO 传输模型
     * @return
     */
    public MediaFilesDTO saveMediaFiles(MediaFilesDTO mediaFilesDTO);
}
