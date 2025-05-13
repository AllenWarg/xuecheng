package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.CourseCategoryTreeDto;

import java.util.List;

/**
 * @Author gc
 * @Description 课程分类服务
 * @DateTime: 2025/5/13 18:09
 **/
public interface CourseCategoryService {
    List<CourseCategoryTreeDto> queryCourseCategoryTree(String id);
}
