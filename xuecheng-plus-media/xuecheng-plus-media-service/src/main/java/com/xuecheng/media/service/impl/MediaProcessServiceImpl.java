package com.xuecheng.media.service.impl;

import com.xuecheng.execption.XueChengException;
import com.xuecheng.media.mapper.MediaFilesMapper;
import com.xuecheng.media.mapper.MediaProcessHistoryMapper;
import com.xuecheng.media.mapper.MediaProcessMapper;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.model.po.MediaProcess;
import com.xuecheng.media.model.po.MediaProcessHistory;
import com.xuecheng.media.service.MediaProcessService;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @Author gc
 * @Description 媒体任务处理
 * @DateTime: 2025/5/18 22:17
 **/
@Service
@Slf4j
public class MediaProcessServiceImpl implements MediaProcessService {

    @Autowired
    MediaProcessMapper mediaProcessMapper;
    @Autowired
    MediaFilesMapper mediaFilesMapper;
    @Autowired
    MediaProcessHistoryMapper mediaProcessHistoryMapper;


    /**
     * 获取媒资处理任务
     * @param shardIndex 当前服务标识（当前微服务的标识）
     * @param shardTotal 总共的标识数（微服务总个数）
     * @param count 处理任务的条数
     * @return
     */
    public List<MediaProcess> getMediaProcessListByShardIndex(int shardIndex, int shardTotal, int count){
        return mediaProcessMapper.selectMediaProcessListByShardShardIndex(shardIndex,shardTotal,count);
    }

    /**
     * 保存一个媒体处理任务
     * @param mediaProcess 媒体处理任务模型
     * @return
     */
    @Transactional
    public int saveMediaProcess(MediaProcess mediaProcess){
        mediaProcess.setId(null);
        mediaProcess.setUrl(null);
        //状态,1:未处理，2：处理成功  3处理失败
        mediaProcess.setStatus("1");
        mediaProcess.setCreateDate(LocalDateTime.now());
        mediaProcess.setFailCount(0);
        int insert = mediaProcessMapper.insert(mediaProcess);
        if (insert<=0){
            log.error("添加视频转码任务失败！");
            XueChengException.cast("添加视频转码任务失败！");
        }
        return insert;
    }


    /**
     * 开启一个任务(基于数据库字段的分布式锁)
     * @param id 任务id
     * @return 更新记录数
     */
    public int startTask(@Param("id") long id){
        return mediaProcessMapper.startTask(id);
    }

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
    @Transactional
    public void saveProcessFinishStatus(Long taskId,String status,String fileId,String url,String errorMsg){
        MediaProcess mediaProcess = mediaProcessMapper.selectById(taskId);
        if (mediaProcess==null){
            return;
        }
        //1.处理失败
        if ("3".equals(status)){
            mediaProcess.setStatus("3");
            mediaProcess.setFailCount(mediaProcess.getFailCount()+1);
            mediaProcess.setErrormsg(errorMsg);
            mediaProcessMapper.updateById(mediaProcess);
            return;
        }


        //2.处理成功
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileId);
        if (mediaFiles==null){
            return;
        }
        mediaFiles.setUrl(url);
        mediaFilesMapper.updateById(mediaFiles);
        mediaProcess.setStatus("2");
        mediaProcess.setFinishDate(LocalDateTime.now());
        mediaProcess.setUrl(url);
        //更新任务表
        mediaProcessMapper.updateById(mediaProcess);
        //将成功的任务放入历史表中
        MediaProcessHistory mediaProcessHistory = new MediaProcessHistory();
        BeanUtils.copyProperties(mediaProcess,mediaProcessHistory);
        mediaProcessHistoryMapper.insert(mediaProcessHistory);
        //删除任务表中当前这个任务
        mediaProcessMapper.deleteById(mediaProcess);
    }

    /**
     * 处理超时的任务
     * @param shardIndex 分片机器id
     * @param shardTotal 分片机器总数
     * @return
     */
    @Transactional
    public int handleTimeoutMediaProcess(int shardIndex, int shardTotal){
       return mediaProcessMapper.updateTimeoutMediaProcess(shardIndex,shardTotal);
    }


}
