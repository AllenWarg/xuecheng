package com.xuecheng.learning.service.impl;

import com.alibaba.fastjson.JSON;
import com.xuecheng.content.model.po.CoursePublish;
import com.xuecheng.execption.CommonError;
import com.xuecheng.execption.XueChengException;
import com.xuecheng.learning.feignclient.ContentServiceClient;
import com.xuecheng.learning.feignclient.MediaServiceClient;
import com.xuecheng.learning.model.dto.TeachplanDto;
import com.xuecheng.learning.model.dto.XcCourseTablesDto;
import com.xuecheng.learning.service.CourseService;
import com.xuecheng.learning.service.LearningService;
import com.xuecheng.model.RestResponse;
import com.xuecheng.utils.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class LearningServiceImpl implements LearningService {
    @Autowired
    CourseService courseService;
    @Autowired
    MediaServiceClient mediaServiceClient;
    @Autowired
    ContentServiceClient contentServiceClient;

    @Override
    public RestResponse<String> getLearningVideo(String userId, Long courseId, Long teachplanId, String mediaId) {
        // 1.查询课程信息
        CoursePublish coursepublish = contentServiceClient.getCoursepublish(courseId);
        if (coursepublish==null) {
            log.debug("获取学习视频，查询发布课程失败。课程id：{}", courseId);
            XueChengException.cast(CommonError.OBJECT_NULL);
        }
        // 该课程的教学计划是否支持试学
        String teachplan = coursepublish.getTeachplan();
        List<TeachplanDto> teachplanDtoList = JSON.parseArray(teachplan,TeachplanDto.class);
        for (TeachplanDto teachplanDto : teachplanDtoList) {
            Long pId = (long) teachplanDto.getId();
            if (pId.equals(teachplanId)) {
                if ("1".equals(teachplanDto.getIsPreview())) {
                    return mediaServiceClient.getPlayUrlByMediaId(mediaId);
                }
            }
            for (TeachplanDto teachPlanTreeNode : teachplanDto.getTeachPlanTreeNodes()) {
                Long tId = (long) teachPlanTreeNode.getId();
                if (tId.equals(teachplanId)) {
                    if ("1".equals(teachplanDto.getIsPreview())) {
                        return mediaServiceClient.getPlayUrlByMediaId(mediaId);
                    }
                }
            }
        }

        // 2.学习资格判断
        // 登录的情况
        if (StringUtil.isNotEmpty(userId)) {
            XcCourseTablesDto xcCourseTablesDto = courseService.getLearnstatus(userId, courseId);
            // 学习资格，[{"code":"702001","desc":"正常学习"},{"code":"702002","desc":"没有选课或选课后没有支付"},{"code":"702003","desc":"已过期需要申请续期或重新支付"}]
            String learnStatus = xcCourseTablesDto.getLearnStatus();
            if ("702001".equals(learnStatus)) {
                RestResponse<String> playUrl = mediaServiceClient.getPlayUrlByMediaId(mediaId);
                if (playUrl==null) {
                    log.debug("获取学习视频链接失败，失败媒资文件id：{}", mediaId);
                    return null;
                }
                return playUrl;
            } else if ("702002".equals(learnStatus)) {
                RestResponse.validfail("没有选课或选课后没有支付");
            } else if ("702003".equals(learnStatus)) {
                RestResponse.validfail("已过期需要申请续期或重新支付");
            }
        }
        // 未登录时
        //[{"code":"201000","desc":"免费"},{"code":"201001","desc":"收费"}]
        String charge = coursepublish.getCharge();
        if ("201000".equals(charge)) {
            RestResponse<String> playUrl = mediaServiceClient.getPlayUrlByMediaId(mediaId);
            if (playUrl==null) {
                log.debug("获取学习视频链接失败，失败媒资文件id：{}", mediaId);
                XueChengException.cast(CommonError.OBJECT_NULL);
            }
        }

        return RestResponse.validfail("购买课程后才能继续学习");
    }
}
