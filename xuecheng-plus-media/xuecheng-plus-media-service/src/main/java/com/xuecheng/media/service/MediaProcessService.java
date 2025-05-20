package com.xuecheng.media.service;

import com.xuecheng.media.model.po.MediaProcess;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Author gc
 * @Description 媒体处理服务（视频转码等）
 * @DateTime: 2025/5/18 21:57
 **/
public interface MediaProcessService {

    /**
     * 获取媒资处理任务
     * @param shardIndex 当前服务标识（当前微服务的标识）
     * @param shardTotal 总共的标识数（微服务总个数）
     * @param count 处理任务的条数
     * @return
     */
    public List<MediaProcess> getMediaProcessListByShardIndex(int shardIndex,int shardTotal,int count);

    /**
     * 保存一个媒体处理任务
     * @param mediaProcess 媒体处理任务模型
     * @return
     */
    public int saveMediaProcess(MediaProcess mediaProcess);


    /**
     * 开启一个任务
     * @param id 任务id
     * @return 更新记录数
     */
    public int startTask(@Param("id") long id);


    /**
     * @description 保存任务结果
     * @param taskId  任务id
     * @param status 任务状态
     * @param fileId  文件id
     * @param url url
     * @param errorMsg 错误信息
     * @return void
     * @author Mr.M
     * @date 2022/10/15 11:29
     */
    void saveProcessFinishStatus(Long taskId,String status,String fileId,String url,String errorMsg);

    /**
     * 处理超时的任务
     * @param shardIndex 分片机器id
     * @param shardTotal 分片机器总数
     * @return
     */
    public int handleTimeoutMediaProcess(int shardIndex, int shardTotal);


}
