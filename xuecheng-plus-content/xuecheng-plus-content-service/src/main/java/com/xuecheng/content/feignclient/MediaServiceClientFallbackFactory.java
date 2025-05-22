package com.xuecheng.content.feignclient;

import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/**
 * @Author gc
 * @Description 媒资文件远程调用上传文件
 * @DateTime: 2025/5/22 15:51
 **/
@Component
@Slf4j
public class MediaServiceClientFallbackFactory implements FallbackFactory<MediaServiceClient> {
    @Override
    public MediaServiceClient create(Throwable throwable) {
        return new MediaServiceClient() {
            @Override
            public String uploadFile(MultipartFile upload, String objectName) {
                log.error("远程调用媒资服务上传文件失败，发生熔断,触发降级方法，{}",throwable.toString(),throwable);
                return null;
            }
        };
    }
}
