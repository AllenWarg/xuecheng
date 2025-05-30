package com.xuecheng.learning.service;

import com.xuecheng.model.RestResponse;

/**
 * 学习相关的接口
 */
public interface LearningService {
    /**
     * 获取学习视频
     * @param UserId 用户id
     * @param courseId 课程id
     * @param teachplanId 课程计划id
     * @param mediaId 媒资文件id
     * @return
     */
    RestResponse<String>  getLearningVideo(String userId, Long courseId, Long teachplanId, String mediaId);
}
