package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.AddCourseBaseDto;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.model.PageParams;
import com.xuecheng.model.PageResult;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @Author: gc
 * @Description: 课程信息表管理接口
 * @DateTime: 2025/5/12 15:37
 **/
public interface CourseBaseInfoService {
    /**
     * @Description 课程信息分页查询
     * @param pageParams 分页参数
     * @param queryCourseParams 查询参数
     * @param companyId 机构id
     * @return 响应结果
     */
    public PageResult<CourseBase> queryCourseBaseList(Long companyId, PageParams pageParams, @RequestBody(required=false) QueryCourseParamsDto queryCourseParams);

    /**
     * @Description 通过课程id查询
     * @param id 课程id
     * @return 响应结果
     */
    AddCourseBaseDto queryCourseBaseById(Long id);

    /**
     * @Description 添加课程信息
     * @param addCourseBaseDto 课程基本信息
     * @return 响应结果
     */
    public AddCourseBaseDto addCourseBaseInfo(AddCourseBaseDto addCourseBaseDto,Long companyId);

    /**
     * @Description 修改课程信息
     * @param addCourseBaseDto 课程基本信息
     * @return 响应结果
     */
    public AddCourseBaseDto editCourseBaseInfo(AddCourseBaseDto addCourseBaseDto);

    /**
     * 删除课程信息
     * @param id 课程信息id
     */
    void removeCourseBaseInfo(Long id);
}
