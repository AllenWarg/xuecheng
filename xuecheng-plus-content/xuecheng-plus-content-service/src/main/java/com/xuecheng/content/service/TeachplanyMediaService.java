package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.BindTeachplanMediaDto;
import com.xuecheng.content.model.po.TeachplanMedia;

/**
 * @Author gc
 * @Description 课程计划与媒资文件的绑定
 * @DateTime: 2025/5/20 16:41
 **/
public interface TeachplanyMediaService {
    /**
     * 将课程计划和媒资文件进行绑定
     * @param bindTeachplanMediaDto 绑定DTO
     */
    TeachplanMedia bindTeachplanMedia(BindTeachplanMediaDto bindTeachplanMediaDto);

    /**
     * 删除课程计划和媒资文件之间的绑定
     * @param teachPlanId 课程计划id
     * @param mediaId 媒资文件id
     */
   public void removeTeachplanMedia(Long teachPlanId, String mediaId);
}
