package com.xuecheng.content.api;

import com.xuecheng.model.PageParams;
import com.xuecheng.model.PageResult;
import com.xuecheng.model.dto.QueryCourseParamsDto;
import com.xuecheng.model.po.CourseBaseDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @description 课程信息编辑接口
 * @author Mr.M
 * @date 2022/9/6 11:29
 * @version 1.0
 */
@Api(value = "课程信息编辑接口",tags = "课程信息编辑接口")
@RestController
public class CourseBaseInfoController {
    @ApiOperation("课程查询接口")
    @RequestMapping("/course/list")
    public PageResult<CourseBaseDTO> list(PageParams pageParams, @RequestBody(required=false) QueryCourseParamsDto queryCourseParams){
        CourseBaseDTO courseBase = new CourseBaseDTO();
        courseBase.setName("测试名称");
        courseBase.setCreateDate(LocalDateTime.now());
        List<CourseBaseDTO> courseBases = new ArrayList();
        courseBases.add(courseBase);
        PageResult<CourseBaseDTO> pageResult = new PageResult<>(courseBases,10,1,10);
        return pageResult;
    }

}
