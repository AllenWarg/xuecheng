package com.xuecheng.media;

import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.RemoveObjectArgs;
import io.minio.UploadObjectArgs;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.util.DigestUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

/**
 * @Author gc
 * @Description 测试minio基础api
 * @DateTime: 2025/5/16 22:23
 **/
//@SpringBootTest
public class MinioTest {
    MinioClient minioClient;

    @BeforeEach
    public void init(){
        String url="http://192.168.72.65:9000";
        String accessKey="minioadmin";
        String secretKey="minioadmin";
        // Create a minioClient with the MinIO server playground, its access key and secret key.
        minioClient = MinioClient.builder()
                        .endpoint(url)
                        .credentials(accessKey, secretKey)
                        .build();
    }

    //测试上传文件到minio
    @Test
    public void testUpload() throws Exception {
        String bucketName="testbucket";
        String objectPath="/test/test.mp4";
        String filename="F:\\学习视频\\视频_开发学习类\\web前端\\【01】HTML\\day01\\01.基础班学习目标__9fxw.com.mp4";
        UploadObjectArgs uploadObjectArgs = UploadObjectArgs.builder()
                .bucket(bucketName)
                .object(objectPath)
                .filename(filename)
                .contentType("video/mp4")
                .build();
        // 上传文件
        minioClient.uploadObject(uploadObjectArgs);
    }
    //查询minio中的文件（对比minio中文件的md5）
    @Test
    public void testGetObject() throws Exception {
        String bucketName="testbucket";
        String objectPath="merge/test.mp4";
        String filename="F:\\学习视频\\视频_开发学习类\\web前端\\【01】HTML\\day01\\01.基础班学习目标__9fxw.com.mp4";
        GetObjectArgs getObjectArgs=GetObjectArgs.builder()
                .bucket(bucketName)
                .object(objectPath)
                .build();
        InputStream object =null;
        try {
            object=minioClient.getObject(getObjectArgs);
        }catch (Exception e){
            e.printStackTrace();
        }
        String objectMD5 = DigestUtils.md5DigestAsHex(object);
        FileInputStream fileInputStream = new FileInputStream(new File(filename));
        String fileMD5 = DigestUtils.md5DigestAsHex(fileInputStream);
        if (fileMD5.equals(objectMD5)){
            System.out.println("文件一致");
        }else {
            System.out.println("文件不一致");
        }
        fileInputStream.close();
        object.close();
    }
    //删除minio中的文件
    @Test
    public void testRemoveObject() throws Exception {
        String bucketName="testbucket";
        String objectPath="/test/test.mp4";
        String filename="F:\\学习视频\\视频_开发学习类\\web前端\\【01】HTML\\day01\\01.基础班学习目标__9fxw.com.mp4";
        RemoveObjectArgs removeObjectArgs = RemoveObjectArgs.builder()
                .bucket(bucketName)
                .object(objectPath)
                .build();
        minioClient.removeObject(removeObjectArgs);
    }

}
