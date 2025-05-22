package com.xuecheng.content.model.dto;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @Author gc
 * @Description 课程教师传输模型
 * @DateTime: 2025/5/15 17:19
 **/
@Data
@ApiModel(description = "课程教师传输模型")
public class CourseTeacherDTO{
    /**
     * 主键
     */
    private Long id;

    /**
     * 课程标识
     */
    private Long courseId;

    /**
     * 教师标识
     */
    private String teacherName;

    /**
     * 教师职位
     */
    private String position;

    /**
     * 教师简介
     */
    private String introduction;

    /**
     * 照片
     */
    private String photograph;

    /**
     * 创建时间
     */
    private LocalDateTime createDate;


}
