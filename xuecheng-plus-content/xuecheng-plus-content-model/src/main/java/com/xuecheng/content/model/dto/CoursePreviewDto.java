package com.xuecheng.content.model.dto;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * @Author gc
 * @Description 课程预览数据模型
 * @DateTime: 2025/5/21 0:38
 **/
@Data
@Slf4j
public class CoursePreviewDto {
    //课程基本信息,课程营销信息
    CourseBaseInfoDTO courseBase;


    //课程计划信息
    List<TeachplanTreeDTO> teachplans;

    //师资信息暂时不加...

}
