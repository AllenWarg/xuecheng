package com.xuecheng.media;

import java.util.stream.Stream;

/**
 * @Author 作者 TODO
 * @Description 描述 TODO
 * @DateTime: 2025/5/18 0:39
 **/
public class Mytest {
    public static void main(String[] args) {
        Stream.iterate(0, i->i++)
                .limit(10)
                .forEach(System.out::println);
    }
}
