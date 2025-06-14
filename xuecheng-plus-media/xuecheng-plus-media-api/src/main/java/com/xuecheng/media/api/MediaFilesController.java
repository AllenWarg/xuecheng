package com.xuecheng.media.api;

import com.xuecheng.media.model.dto.MediaFilesDTO;
import com.xuecheng.media.model.dto.QueryMediaParamsDto;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.service.MediaFileService;
import com.xuecheng.media.util.SecurityUtil;
import com.xuecheng.model.PageParams;
import com.xuecheng.model.PageResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

/**
 * @author Mr.M
 * @version 1.0
 * @description 媒资文件管理接口
 * @date 2022/9/6 11:29
 */
@Api(value = "媒资文件管理接口", tags = "媒资文件管理接口")
@RestController
public class MediaFilesController {
    @Autowired
    MediaFileService mediaFileService;


    @ApiOperation("媒资列表查询接口")
    @PostMapping("/files")
    public PageResult<MediaFiles> list(PageParams pageParams, @RequestBody QueryMediaParamsDto queryMediaParamsDto) {
        // Long companyId = 1232141425L;
        SecurityUtil.XcUser user = SecurityUtil.getUser();
        Long companyId = Long.valueOf(user.getCompanyId());
        return mediaFileService.queryMediaFiels(companyId, pageParams, queryMediaParamsDto);
    }

    @ApiOperation("文件上传")
    @PostMapping("/upload/coursefile")
    public MediaFilesDTO uploadFile(@RequestPart("filedata") MultipartFile multipartFile
            ,@RequestParam(value="objectPath",required = false) String objectPath) throws Exception {
        SecurityUtil.XcUser user = SecurityUtil.getUser();
        Long companyId=null;
        if (user!=null){
            companyId = Long.valueOf(user.getCompanyId());
        }
        String filename = multipartFile.getOriginalFilename();
        long fileSize = multipartFile.getSize();
        File tempFile = File.createTempFile("minio", ".temp");
        multipartFile.transferTo(tempFile);
        MediaFilesDTO uploadMediaFilesDTO = new MediaFilesDTO();
        uploadMediaFilesDTO.setFilename(filename);
        uploadMediaFilesDTO.setFileSize(fileSize);
        String localFilePath=tempFile.getAbsolutePath();
        MediaFilesDTO mediaFilesDTO = mediaFileService.uploadFile(companyId, uploadMediaFilesDTO, localFilePath,objectPath);
        return mediaFilesDTO;
    }



}
