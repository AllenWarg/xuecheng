package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.content.mapper.CourseTeacherMapper;
import com.xuecheng.content.model.dto.CourseTeacherDTO;
import com.xuecheng.content.model.po.CourseTeacher;
import com.xuecheng.content.service.CourseTeacherService;
import com.xuecheng.execption.XueChengException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author gc
 * @Description 课程教师信息管理服务实现类
 * @DateTime: 2025/5/15 17:36
 **/
@Service
@Slf4j
public class CourseTeacherServiceImpl implements CourseTeacherService {
    @Autowired
    CourseTeacherMapper courseTeacherMapper;
    @Override
    public List<CourseTeacher> queryCourseTeacherList(Long courseId) {
        ArrayList<CourseTeacherDTO> courseTeacherDTOS = new ArrayList<>();
        LambdaQueryWrapper<CourseTeacher> lqw = new LambdaQueryWrapper<>();
        lqw.eq(CourseTeacher::getCourseId,courseId);
        List<CourseTeacher> courseTeachers = courseTeacherMapper.selectList(lqw);
        if (courseTeachers==null){
            XueChengException.cast("查询课程教师失败！");
        }
        return courseTeachers;
    }

    @Override
    public CourseTeacherDTO saveOrEditCourseTeacher(CourseTeacherDTO courseTeacherDTO) {
        CourseTeacher courseTeacher=new CourseTeacher();
        BeanUtils.copyProperties(courseTeacherDTO,courseTeacher);
        if (courseTeacher.getId()==null){
            //添加
            int s = courseTeacherMapper.insert(courseTeacher);
            if (s<=0){
                XueChengException.cast("添加课程教师失败！");
            }
        }else {
            //修改
            int s=courseTeacherMapper.updateById(courseTeacher);
            if (s<=0){
                XueChengException.cast("修改课程教师失败！");
            }
        }
        CourseTeacher rs = courseTeacherMapper.selectById(courseTeacher.getId());
        if (rs==null){
            XueChengException.cast("添加或修改课程教师失败！");
        }
        BeanUtils.copyProperties(rs,courseTeacherDTO);
        return courseTeacherDTO;
    }

    public void removeCourseTeacher(Long courseId, Long teacherId){
       LambdaQueryWrapper<CourseTeacher> lqw = new LambdaQueryWrapper<>();
       lqw.eq(CourseTeacher::getCourseId,courseId)
                       .eq(CourseTeacher::getId,teacherId);
       int delete = courseTeacherMapper.delete(lqw);
       if (delete<0){
           XueChengException.cast("删除课程教师失败！");
       }
   }
}
