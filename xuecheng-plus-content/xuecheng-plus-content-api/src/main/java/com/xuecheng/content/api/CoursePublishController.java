package com.xuecheng.content.api;

import com.xuecheng.content.model.dto.CoursePreviewDto;
import com.xuecheng.content.model.po.CoursePublish;
import com.xuecheng.content.service.CoursePublishService;
import com.xuecheng.content.util.SecurityUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

/**
 * @Author gc
 * @Description 课程发布、课程课程预览、课程审核提交
 * @DateTime: 2025/5/20 23:39
 **/
@Api(tags = "课程发布、课程课程预览、课程审核提交")
@Controller
public class CoursePublishController {
    @Autowired
    CoursePublishService coursePublishService;

    /**
     * 课程发布预览,返回模板视图
     *
     * @param courseId
     * @return
     */
    @ApiOperation("课程发布预览")
    @GetMapping("/coursepreview/{courseId}")
    public ModelAndView preview(@PathVariable Long courseId) {
        CoursePreviewDto coursePreviewInfo = coursePublishService.getCoursePreviewInfo(courseId);
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("model", coursePreviewInfo);
        modelAndView.setViewName("course_template");
        return modelAndView;
    }

    @ApiOperation("课程审核提交")
    @ResponseBody
    @PostMapping("/courseaudit/commit/{courseId}")
    public void commitAudit(@PathVariable("courseId") Long courseId) {
        // Long companyId=594000L;
        SecurityUtil.XcUser user = SecurityUtil.getUser();
        Long companyId = Long.valueOf(user.getCompanyId());
        coursePublishService.commitAudit(companyId, courseId);
    }

    @ApiOperation("课程发布")
    @ResponseBody
    @PostMapping("/coursepublish/{courseId}")
    public void coursepublish(@PathVariable("courseId") Long courseId) {
//        Long companyId=594000L;
        String companyId = SecurityUtil.getUser().getCompanyId();

        coursePublishService.coursepublish(Long.valueOf(companyId), courseId);
    }

    @ApiOperation("查询课程发布信息")
    @ResponseBody
    @GetMapping("/r/coursepublish/{courseId}")
    public CoursePublish getCoursepublish(@PathVariable("courseId") Long courseId) {
        CoursePublish coursePublish = coursePublishService.getCoursePublish(courseId);
        return coursePublish;
    }

    @ApiOperation("发布课程预览信息")
    @ResponseBody
    @GetMapping("/course/whole/{courseId}")
    public CoursePreviewDto getCoursePublishPreview(@PathVariable("courseId") Long courseId) {
        // return coursePublishService.getCoursePublishPreviewInfo(courseId);
        return coursePublishService.getCoursePreviewInfoCache(courseId);
    }


    @ApiOperation("课程下架")
    @ResponseBody
    @GetMapping("/courseoffline/{courseId}")
    public void courseoffline(@PathVariable("courseId") Long courseId) {
        coursePublishService.doCourseoffline(courseId);
    }


}
