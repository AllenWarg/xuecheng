package com.xuecheng.media.openApi;

import com.xuecheng.media.service.MediaFileService;
import com.xuecheng.model.RestResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author gc
 * @Description 媒资文件管理
 * @DateTime: 2025/5/21 13:58
 **/
@Api(value = "媒资文件管理接口",tags = "媒资文件管理接口")
@RestController
@RequestMapping("/open")
public class MediaOpenController {
    @Autowired
    MediaFileService mediaFileService;
    /**
     * 根据媒资文件id获取链接
     * @param mediaId 媒资文件id
     * @return
     */
    @ApiOperation("预览文件")
    @GetMapping("/preview/{mediaId}")
    public RestResponse<String> getPlayUrlByMediaId(@PathVariable String mediaId) {
        return mediaFileService.queryMediaFielsById(mediaId);
    }
}
