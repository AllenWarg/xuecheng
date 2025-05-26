package com.xuecheng.content;

import com.xuecheng.content.model.dto.AddCourseBaseDto;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.service.CourseBaseInfoService;
import com.xuecheng.model.PageParams;
import com.xuecheng.model.PageResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class CourseBaseInfoServiceTests {
    @Autowired
    CourseBaseInfoService courseBaseInfoService;

    @Test
    void testCourseBaseInfoService(){
        //分页信息
        PageParams pageParams = new PageParams();
        pageParams.setPageNo(1L);
        pageParams.setPageSize(10L);
        //查询信息
        QueryCourseParamsDto queryCourseParamsDto = new QueryCourseParamsDto();
        //queryCourseParamsDto.setCourseName("java");
        queryCourseParamsDto.setAuditStatus("202004");
        //queryCourseParamsDto.setPublishStatus("203001");
        //调用测试接口
        PageResult<CourseBase> courseBasePageResult = courseBaseInfoService.queryCourseBaseList(123L,pageParams, queryCourseParamsDto);
        System.out.println(courseBasePageResult);

    }



    @Test
    void testAddCourseBaseInfoService(){
        AddCourseBaseDto addCourseBaseDto = new AddCourseBaseDto();
        addCourseBaseDto.setCharge("201000");
        addCourseBaseDto.setPrice(0F);
        addCourseBaseDto.setQq("666666");
        addCourseBaseDto.setPhone("666666");
        addCourseBaseDto.setValidDays(365);
        addCourseBaseDto.setWechat("666666");
        addCourseBaseDto.setMt("1-3");
        addCourseBaseDto.setSt("1-3-2");
        addCourseBaseDto.setName("Java软件开发自学教程");
        addCourseBaseDto.setPic("url");
        addCourseBaseDto.setTeachmode("200002");
        addCourseBaseDto.setUsers("java初学者");
        addCourseBaseDto.setTags("java");
        addCourseBaseDto.setGrade("204002");
        addCourseBaseDto.setDescription("Java软件开发课程描述信息");
        addCourseBaseDto.setOriginalPrice(1F);
        Object o = courseBaseInfoService.addCourseBaseInfo(addCourseBaseDto,594000L);
        System.out.println(o);
    }

    @Test
    void testQueryCourseBaseById(){
        AddCourseBaseDto addCourseBaseDto = courseBaseInfoService.queryCourseBaseById(128L);
        System.out.println(addCourseBaseDto);
    }
}
