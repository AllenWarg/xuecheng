package com.xuecheng.search.controller;

import com.xuecheng.execption.XueChengException;
import com.xuecheng.search.po.CourseIndex;
import com.xuecheng.search.service.IndexService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

/**
 * @author Mr.M
 * @version 1.0
 * @description 课程索引接口
 * @date 2022/9/24 22:31
 */
@Api(value = "课程信息索引接口", tags = "课程信息索引接口")
@RestController
@RequestMapping("/index")
public class CourseIndexController {

    @Value("${elasticsearch.course.index}")
    private String courseIndexStore;

    @Autowired
    IndexService indexService;

    @ApiOperation("添加课程索引")
    @PostMapping("course")
    public Boolean add(@RequestBody CourseIndex courseIndex) {

        Long id = courseIndex.getId();
        if(id==null){
            XueChengException.cast("课程id为空");
        }
        Boolean result = indexService.addCourseIndex(courseIndexStore, String.valueOf(id), courseIndex);
        if(!result){
            XueChengException.cast("添加课程索引失败");
        }
        return result;

    }
    @ApiOperation("添加课程索引")
    @DeleteMapping("course/{courseId}")
    public Boolean removeDoc(@PathVariable("courseId") String courseId){
        Boolean b = indexService.deleteCourseIndex(courseIndexStore, courseId);
        if (!b){
            XueChengException.cast("删除课程索引失败");
        }
        return b;
    }
}
