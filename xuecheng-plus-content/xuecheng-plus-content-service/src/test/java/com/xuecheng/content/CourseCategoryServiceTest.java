package com.xuecheng.content;

import com.xuecheng.content.model.dto.CourseCategoryTreeDto;
import com.xuecheng.content.service.CourseCategoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

/**
 * @Author gc
 * @Description 测试课程分类数查询
 * @DateTime: 2025/5/13 15:32
 **/
@SpringBootTest
public class CourseCategoryServiceTest {
    @Autowired
    CourseCategoryService courseCategoryService;

    @Test
    public void  selectCourseCategoryTree(){
        List<CourseCategoryTreeDto> list = courseCategoryService.queryCourseCategoryTree("1-1");
        System.out.println(list);
    }


}
