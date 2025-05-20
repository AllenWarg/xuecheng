package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.content.mapper.TeachplanMapper;
import com.xuecheng.content.mapper.TeachplanMediaMapper;
import com.xuecheng.content.model.dto.BindTeachplanMediaDto;
import com.xuecheng.content.model.po.Teachplan;
import com.xuecheng.content.model.po.TeachplanMedia;
import com.xuecheng.content.service.TeachplanyMediaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @Author gc
 * @Description 课程计划与媒资文件的绑定
 * @DateTime: 2025/5/20 16:41
 **/
@Service
@Slf4j
public class TeachplanyMediaServiceImpl implements TeachplanyMediaService {
    @Autowired
    TeachplanMediaMapper teachplanMediaMapper;
    @Autowired
    TeachplanMapper teachplanMapper;

    @Override
    @Transactional
    public TeachplanMedia bindTeachplanMedia(BindTeachplanMediaDto bindTeachplanMediaDto) {
        LambdaQueryWrapper<TeachplanMedia> qw = new LambdaQueryWrapper<>();
        qw.eq(TeachplanMedia::getTeachplanId,bindTeachplanMediaDto.getTeachplanId());
        TeachplanMedia teachplanMedia = teachplanMediaMapper.selectOne(qw);
        if (teachplanMedia!=null){
            //如果已经绑定了媒资文件，就先删除
            teachplanMediaMapper.deleteById(teachplanMedia);
        }
        Teachplan teachplan = teachplanMapper.selectById(bindTeachplanMediaDto.getTeachplanId());
        if (teachplan==null){
            return null;
        }
        teachplanMedia = new TeachplanMedia();
        BeanUtils.copyProperties(bindTeachplanMediaDto,teachplanMedia);
        teachplanMedia.setCourseId(teachplan.getCourseId());
        teachplanMedia.setMediaFilename(bindTeachplanMediaDto.getFileName());
        int insert = teachplanMediaMapper.insert(teachplanMedia);
        if (insert<=0){
            log.error("教学计划和媒资文件绑定失败");
            return null;
        }
        TeachplanMedia returnRes = teachplanMediaMapper.selectById(teachplanMedia);
        if (returnRes==null){
            return null;
        }
        log.debug("教学计划和媒资文件绑定成功，id：{}",teachplanMedia.getId());
        return returnRes;
    }


    /**
     * 删除课程计划和媒资文件之间的绑定
     * @param teachPlanId 课程计划id
     * @param mediaId 媒资文件id
     */
    public void removeTeachplanMedia(Long teachPlanId, String mediaId){
        LambdaQueryWrapper<TeachplanMedia> qw = new LambdaQueryWrapper<>();
        qw.eq(TeachplanMedia::getTeachplanId,teachPlanId);
        qw.eq(TeachplanMedia::getMediaId,mediaId);
        int delete = teachplanMediaMapper.delete(qw);
        if (delete<=0){
            log.error("删除课程计划和媒资文件之间的绑定失败，teachPlanId：{}，mediaId：{}",teachPlanId,mediaId);
        }
    }
}
