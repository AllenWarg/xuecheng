package com.xuecheng.media;

/**
 * @Author gc
 * @Description
 * @DateTime: 2025/5/18 0:39
 **/
public class Mytest {
    public static void main(String[] args) {
        String s="ab";
        if (s.length()<2){
            System.out.println(s.substring(0));
        }else {
            System.out.println(s.substring(1,2));
        }


    }
}
