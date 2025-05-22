package com.xuecheng.content;

import com.xuecheng.content.config.MultipartSupportConfig;
import com.xuecheng.content.feignclient.MediaServiceClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

/**
 * @Author gc
 * @Description 测试feign远程上传文件
 * @DateTime: 2025/5/22 13:46
 **/
@SpringBootTest
public class FeignUploadTest {

    @Autowired
    MediaServiceClient mediaServiceClient;

    //远程调用，上传文件
    @Test
    public void test() {
        String objectPath="E:\\fileTemp\\"+"test1.html";
        MultipartFile multipartFile = MultipartSupportConfig.getMultipartFile(new File(objectPath));
        String s = mediaServiceClient.uploadFile(multipartFile, "course/test.html");
        System.out.println(s);
    }

}
