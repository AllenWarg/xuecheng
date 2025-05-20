package com.xuecheng.media;

import io.minio.ComposeObjectArgs;
import io.minio.ComposeSource;
import io.minio.MinioClient;
import io.minio.UploadObjectArgs;
import io.minio.errors.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @Author gc
 * @Description 文件的分片还有分片合并
 * @DateTime: 2025/5/17 20:43
 **/
public class FileChunkAndMergeTest {

    //文件分块方法1
    @Test
    public void fileChunk() throws IOException {
        String inputPath = "D:\\fileTemp\\chunk.mp4";
        String outPath = "D:\\fileTemp\\xuecheng\\chunk\\";
        File sourceFile = new File(inputPath);
        File chunkOutPath = new File(outPath);
        if (!chunkOutPath.exists()) {
            chunkOutPath.mkdirs();
        }
        long chunkSize = 1 * 1024 * 1024;//1M
        long chunkNum= (long) Math.ceil(sourceFile.length()*1.0/chunkSize);
        byte[] bytes=new byte[1024];
        RandomAccessFile rf = new RandomAccessFile(sourceFile,"r");
        long sum=0;
        while (sum<=chunkNum){
            int len=-1;
            File tempFile=new File(outPath+sum);
            tempFile.createNewFile();
            RandomAccessFile wf = new RandomAccessFile(tempFile, "rw");
            while ((len=rf.read(bytes))!=-1){
                wf.write(bytes,0,len);
                if (tempFile.length() >= chunkSize) {
                    break;
                }
            }
            wf.close();
            sum++;
        }
        rf.close();
        System.out.println("分块完成！");
    }
    //文件分块方法2
    @Test
    public void fileChunk2() throws IOException {
        String inputPath = "D:\\fileTemp\\chunk.mp4";
        String outPath = "D:\\fileTemp\\xuecheng\\chunk\\";
        File sourceFile = new File(inputPath);
        File chunkOutPath = new File(outPath);
        if (!chunkOutPath.exists()) {
            chunkOutPath.mkdirs();
        }
        long chunkSize=5*1024*1024;
        long chunkNum= (long) (sourceFile.length()*1.0/chunkSize);
        FileInputStream fileInputStream = new FileInputStream(sourceFile);
        long sum=0;
        byte[] bytes = new byte[1024*1024];
        while (sum<=chunkNum){
            File file = new File(outPath + sum);
            file.createNewFile();
            FileOutputStream fileOutputStream
                    = new FileOutputStream(file);
            int len=0;
            while ((len=fileInputStream.read(bytes))!=-1){
                fileOutputStream.write(bytes,0, len);
                fileOutputStream.flush();
                if (file.length()>=chunkSize){
                    break;
                }
            }
            System.out.println("分块"+sum);
            fileOutputStream.close();
            sum++;
        }
        fileInputStream.close();
        System.out.println("文件分块完成");
    }
    //合并分块
    @Test
    public void fileMerge() throws IOException {
        String inputPath = "D:\\fileTemp\\xuecheng\\chunk\\";
        String outPath = "D:\\fileTemp\\xuecheng\\merge\\";
        if (!(new File(outPath).exists())){
            new File(outPath).mkdirs();
        }
        File sourceFile = new File(inputPath);
        File[] files = sourceFile.listFiles();
        Arrays.sort(files,(f1,f2)->{
            return Integer.valueOf(f1.getName())-Integer.valueOf(f2.getName());
        });
        byte[] bytes = new byte[1024];
        FileOutputStream fos = new FileOutputStream(new File(outPath+"merge.mp4"));
        FileInputStream fis=null;
        int i=0;
        for (File file : files) {
            fis=new FileInputStream(file);
            int len=0;
            while ((len=fis.read(bytes))!=-1){
                fos.write(bytes,0,len);
            }
            fos.flush();
            System.out.println("合并分块"+i);
            fis.close();
            i++;
        }
        fos.close();
    }


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

    //上传分块到minio
    @Test
    public void chunkToMinio() throws IOException, ServerException, InsufficientDataException, ErrorResponseException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        String bucketName="testbucket";
        String inputPath = "D:\\fileTemp\\xuecheng\\chunk\\";
        File file = new File(inputPath);
        File[] files = file.listFiles();
        int i=0;
        for (File item : files) {
            String objectPath="chunk/"+item.getName();
            UploadObjectArgs uploadObjectArgs = UploadObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectPath)
                    .filename(item.getAbsolutePath())
                    .build();
            System.out.println("上传分块"+i);
            // 上传文件
            minioClient.uploadObject(uploadObjectArgs);
            i++;
        }
    }
    //在minio中合并分块
    @Test
    public void minioMergeChunk() throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        //List<ComposeSource> sources;
        //ComposeSource composeSource = ComposeSource.builder().bucket("testbucket").object("chunk/1").build();
        List<ComposeSource> composeSources = Stream
                .iterate(0, i -> i++)
                .limit(22)
                .map(i -> ComposeSource.builder().bucket("testbucket").object("chunk/" + i).build())
                .collect(Collectors.toList());

        ComposeObjectArgs composeObjectArgs = ComposeObjectArgs.builder()
                .bucket("testbucket")
                .object("merge/merge.mp4")
                .sources(composeSources)
                .build();
        minioClient.composeObject(composeObjectArgs);
    }



}
