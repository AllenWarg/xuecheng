package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.CourseTeacherDTO;
import com.xuecheng.content.model.po.CourseTeacher;

import java.util.List;

/**
 * @Author gc
 * @Description 课程教师管理服务
 * @DateTime: 2025/5/13 18:09
 **/
public interface CourseTeacherService {
    /**
     * 查询课程教师信息
     * @param courseId 课程id
     * @return
     */
    public List<CourseTeacher> queryCourseTeacherList(Long courseId);


    /**
     * 保存课程教师
     * @param courseTeacherDTO
     * @return
     */
    CourseTeacherDTO saveOrEditCourseTeacher(CourseTeacherDTO courseTeacherDTO);

    /**
     * 删除课程教师信息
     * @param courseId 课程id
     * @param teacherId 教师id
     * @return
     */
    void removeCourseTeacher(Long courseId, Long teacherId);
}
