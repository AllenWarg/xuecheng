package com.xuecheng.content;

import com.spring4all.swagger.EnableSwagger2Doc;
import com.xuecheng.config.LocalDateTimeConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@EnableSwagger2Doc
@SpringBootApplication
@Import(LocalDateTimeConfig.class)
public class ContentApplication {
    public static void main(String[] args) {
        SpringApplication.run(ContentApplication.class);
    }
}
