package com.xuecheng.content.feignclient;

import com.xuecheng.content.config.MultipartSupportConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

/**
 * @Author gc
 * @Description 媒资服务调用客户端
 * @DateTime: 2025/5/22 13:29
 **/
@FeignClient(value = "media-api",configuration = MultipartSupportConfig.class,fallbackFactory =MediaServiceClientFallbackFactory.class)
public interface MediaServiceClient {
    @PostMapping(value = "/media/upload/coursefile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    String uploadFile(@RequestPart("filedata") MultipartFile upload, @RequestParam(value = "objectPath", required = false) String objectName);
}
