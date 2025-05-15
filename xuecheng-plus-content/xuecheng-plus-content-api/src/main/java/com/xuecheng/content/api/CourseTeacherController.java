package com.xuecheng.content.api;

import com.xuecheng.content.model.dto.CourseTeacherDTO;
import com.xuecheng.content.model.po.CourseTeacher;
import com.xuecheng.content.service.CourseTeacherService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @description 课程教师接口
 * @author Mr.M
 * @date 2022/9/6 11:29
 * @version 1.0
 */
@Slf4j
@Api(value = "课程教师接口",tags = "课程教师接口")
@RestController
public class CourseTeacherController {
    @Autowired
    CourseTeacherService courseTeacherService;


    /**
     * 查询课程教师
     * @param courseId 课程id
     * @return
     */
    @ApiOperation("查询课程教师信息")
    @GetMapping("/courseTeacher/list/{courseId}")
    public List<CourseTeacher> getcourseTeacherList(@PathVariable("courseId") Long courseId){
        return courseTeacherService.queryCourseTeacherList(courseId);
    }

    /**
     * 添加和修改课程教师接口
     * @param courseTeacherDTO 课程教师传输模型
     * @return
     */
    @ApiOperation("添加课程教师信息")
    @RequestMapping(value = "/courseTeacher",method = {RequestMethod.POST,RequestMethod.PUT})
    public CourseTeacherDTO  saveCourseTeacher(@RequestBody CourseTeacherDTO courseTeacherDTO){
        return courseTeacherService.saveOrEditCourseTeacher(courseTeacherDTO);
    }


    /**
     * 删除课程教师信息
     * @param courseId 课程id
     * @param teacherId 教师id
     * @return
     */
    @ApiOperation("删除课程教师信息")
    @DeleteMapping(value = "/courseTeacher/course/{courseId}/{teacherId}")
    public void removeCourseTeacher(@PathVariable Long courseId,@PathVariable Long teacherId){
        courseTeacherService.removeCourseTeacher(courseId,teacherId);
    }
}
