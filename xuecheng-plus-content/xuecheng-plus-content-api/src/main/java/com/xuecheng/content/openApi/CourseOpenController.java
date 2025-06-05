package com.xuecheng.content.openApi;

import com.xuecheng.content.model.dto.CoursePreviewDto;
import com.xuecheng.content.service.CoursePublishService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author gc
 * @Description 课程公开查询接口
 * @DateTime: 2025/5/21 1:36
 **/
@Api(value = "课程公开查询接口",tags = "课程公开查询接口")
@RestController
@RequestMapping("/open")
public class CourseOpenController {
    @Autowired
    CoursePublishService coursePublishService;
    @GetMapping("/course/whole/{courseId}")
    public CoursePreviewDto getPreviewInfo(@PathVariable("courseId") Long courseId) {
        //获取课程预览信息
        //CoursePreviewDto coursePreviewInfo = coursePublishService.getCoursePreviewInfo(courseId);
        CoursePreviewDto coursePreviewInfo = coursePublishService.getCoursePreviewInfoCache(courseId);
        return coursePreviewInfo;
    }



}
