package com.xuecheng.media;

import okhttp3.*;

import java.io.IOException;

/**
 * @Author gc
 * @Description
 * @DateTime: 2025/5/18 0:39
 **/
public class Mytest {
    public static void main(String[] args) throws IOException {
        OkHttpClient client = new OkHttpClient();
        MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
        String json="{\"id\":\"12345\"}";
        RequestBody body = RequestBody.create(mediaType, json);
        Request request = new Request.Builder()
                .url("http://localhost/test")
                .post()
                .build();
        Response response = client.newCall(request).execute();
        System.out.println(response.body().string());
    }
}
