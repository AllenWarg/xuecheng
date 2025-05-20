package com.xuecheng.media.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xuecheng.media.model.po.MediaProcess;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author itcast
 */
@Mapper
public interface MediaProcessMapper extends BaseMapper<MediaProcess> {

    @Select("SELECT * FROM media_process " +
            "WHERE id%#{shardTotal}=#{shardIndex} AND (`status`='1' or `status`='3') AND fail_count<3 LIMIT 0,#{count}")
    public List<MediaProcess> selectMediaProcessListByShardShardIndex(@Param("shardIndex") int shardIndex
            , @Param("shardTotal") int shardTotal
            , @Param("count") int count);

    /**
     * 开启一个任务(基于数据库字段的分布式锁)
     * @param id 任务id
     * @return 更新记录数
     */
    @Update("update media_process m set m.status='4' where (m.status='1' or m.status='3') and m.fail_count<3 and m.id=#{id}")
    int startTask(@Param("id") long id);

    /**
     * 更新视频处理超时的任务状态
     * @param shardIndex 分片机器id
     * @param shardTotal 分片机器总数
     * @return
     */
    @Update("UPDATE media_process SET status='1' WHERE `status`='4' AND id%#{shardTotal}=#{shardIndex}")
    int updateTimeoutMediaProcess(@Param("shardIndex") int shardIndex, @Param("shardTotal") int shardTotal);

}
