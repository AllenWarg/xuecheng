package com.xuecheng.content;

import com.xuecheng.content.feignclient.MediaServiceClient;
import com.xuecheng.content.model.dto.CoursePreviewDto;
import com.xuecheng.content.service.CoursePublishService;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author gc
 * @Description 页面静态化测试
 * @DateTime: 2025/5/22 0:12
 **/
@SpringBootTest
public class FreemarkerTest {
    @Autowired
    CoursePublishService coursePublishService;
    @Autowired
    MediaServiceClient mediaServiceClient;

    @Test
    public void testGenerateByTemplate() throws Exception{
        CoursePreviewDto coursePreviewInfo = coursePublishService.getCoursePreviewInfo(136L);
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_22);
        String classPath = this.getClass().getResource("/").getPath();
        cfg.setDirectoryForTemplateLoading(new File(classPath+"/templates/"));
        cfg.setDefaultEncoding("UTF-8");
        Map<String,Object> root = new HashMap<>();
        root.put("model",coursePreviewInfo);
        //Template temp = cfg.getTemplate("test.ftl");
        Template template= cfg.getTemplate("course_template.ftl");
        String objectPath="E:\\fileTemp\\"+"test.html";
        Writer out = new FileWriter(new File(objectPath));
        template.process(root,out);
    }


}
