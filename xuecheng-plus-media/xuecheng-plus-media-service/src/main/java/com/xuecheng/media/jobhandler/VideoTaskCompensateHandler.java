package com.xuecheng.media.jobhandler;

import com.xuecheng.media.service.MediaProcessService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @Author gc
 * @Description 视频任务补偿处理（针对视频处理超过30分钟仍在处理的任务进行补偿操作处理）
 * @DateTime: 2025/5/19 0:48
 **/
@Component
@Slf4j
public class VideoTaskCompensateHandler {
    @Autowired
    MediaProcessService mediaProcessService;

    @XxlJob("videoTaskCompensateHandler")
    public void videoCompensate() {
        int shardIndex = XxlJobHelper.getShardIndex();
        int shardTotal = XxlJobHelper.getShardTotal();
        try {
            mediaProcessService.handleTimeoutMediaProcess(shardIndex,shardTotal);
        }catch (RuntimeException e){
            log.debug("视频处理任务超时补偿失败，原因：{}",e.getMessage());
        }

    }

}
