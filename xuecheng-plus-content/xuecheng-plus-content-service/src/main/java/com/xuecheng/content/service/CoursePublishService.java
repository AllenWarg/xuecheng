package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.CourseIndex;
import com.xuecheng.content.model.dto.CoursePreviewDto;
import com.xuecheng.content.model.po.CoursePublish;

import java.io.File;

/**
 * @Author gc
 * @Description 课程发布服务
 * @DateTime: 2025/5/21 0:49
 **/
public interface CoursePublishService {
    /**
     * @description 获取课程预览信息
     * @param courseId 课程id
     * @return com.xuecheng.content.model.dto.CoursePreviewDto
     * @author Mr.M
     * @date 2022/9/16 15:36
     */
    public CoursePreviewDto getCoursePreviewInfo(Long courseId);

    /**
     * 提交课程审核
     * @param courseId 课程id
     */
    void commitAudit(Long companyIdLong,Long courseId);

    /**
     * 课程发布
     * @param companyId 机构id
     * @param courseId 课程id
     */
    void coursepublish(Long companyId, Long courseId);

    /**
     * 通过模板生成静态html
     * @param courseId 课程id
     * @return
     */
    File GenerateHTMLByTemplate(Long courseId);

    /**
     * 远程调用媒资服务上传文件
     * @param courseId 课程id
     * @param objectPath 对象存储路径
     * @return
     */
    public Boolean uploadFileToMinio(Long courseId,File file,String objectPath);


    /**
     * 添加课程索引
     * @param courseIndex 课程索引传输模型
     * @return
     */
    public Boolean addCourseIndex(CourseIndex courseIndex);

    /**
     * 根据课程id查询已经发布的课程
     * @param courseId
     * @return
     */
    CoursePublish getCoursePublish(Long courseId);

    /**
     * 根据课程id获取发布课程预览信息
     * @param courseId 课程id
     * @return
     */
    CoursePreviewDto getCoursePublishPreviewInfo(Long courseId);

    /**
     * 下架课程操作
     * @param courseId 课程id
     */
    void doCourseoffline(Long courseId);



    /**
     * @description 获取课程预览信息，缓存增强
     * @param courseId 课程id
     * @return com.xuecheng.content.model.dto.CoursePreviewDto
     * @author Mr.M
     * @date 2022/9/16 15:36
     */
    public CoursePreviewDto getCoursePreviewInfoCache(Long courseId);
}
