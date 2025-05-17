package com.xuecheng.media.config;

import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Author gc
 * @Description minio配置类
 * @DateTime: 2025/5/17 0:23
 **/
@Configuration
public class MinioConfig {
    @Value("${minio.endpoint}")
    String url;
    @Value("${minio.accessKey}")
    String accessKey;
    @Value("${minio.secretKey}")
    String secretKey;

    @Bean
    public MinioClient minioClient(){
        MinioClient minioClient = MinioClient.builder()
                .endpoint(url)
                .credentials(accessKey, secretKey)
                .build();
        return minioClient;
    }
}
