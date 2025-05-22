package com.xuecheng.content.jobHandler;

import com.xuecheng.content.mapper.CoursePublishMapper;
import com.xuecheng.content.model.dto.CourseIndex;
import com.xuecheng.content.model.po.CoursePublish;
import com.xuecheng.content.service.CoursePublishService;
import com.xuecheng.messagesdk.model.po.MqMessage;
import com.xuecheng.messagesdk.service.MessageProcessAbstract;
import com.xuecheng.messagesdk.service.MqMessageService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * @Author gc
 * @Description 课程发布任务
 * @DateTime: 2025/5/21 22:02
 **/
@Component
@Slf4j
public class CoursePublishTask extends MessageProcessAbstract {

    @Autowired
    CoursePublishService coursePublishService;
    @Autowired
    CoursePublishMapper coursePublishMapper;

    @XxlJob("coursePublishTaskHandler")
    public void coursePublishTaskHandler() {
        int shardIndex = XxlJobHelper.getShardIndex();
        int shardTotal = XxlJobHelper.getShardTotal();
        String messageType="course_publish";
        int count=30;
        long timeout=60;
        process(shardIndex,shardTotal,messageType,count,timeout);
    }


    @Override
    public boolean execute(MqMessage mqMessage) {
        MqMessageService mqMessageService = this.getMqMessageService();
        String businessKey1 = mqMessage.getBusinessKey1();
        Long courseId = Long.valueOf(businessKey1);
        //课程静态化存入minio,第一阶段
        generateCourseHtml(mqMessage, courseId);

        //课程索引Elasticsearch，第二阶段
        createElasticsearchIndex(mqMessage, courseId);

        //课程缓存redis，第三阶段
        saveCourseCacheToRedis(mqMessage, courseId);
        MqMessage resMqMessage = mqMessageService.getById(mqMessage.getId());
        if ("1".equals(resMqMessage.getStageState1()) && "1".equals(resMqMessage.getStageState2()) && "1".equals(resMqMessage.getStageState3())) {
            return true;
        }
        return false;
    }


    private void generateCourseHtml(MqMessage mqMessage, Long courseId) {
        MqMessageService mqMessageService = this.getMqMessageService();
        //幂等性处理（保证无论调用多少次，但是只成功执行一次）
        Long taskId = mqMessage.getId();
        int stageOne = mqMessageService.getStageOne(taskId);
        if (stageOne >= 1) {
            log.debug("第一阶段生成静态html已经完成，无需重复执行。。");
            return;
        }
        //生成静态html具体代码
        File htmlFile = coursePublishService.GenerateHTMLByTemplate(courseId);
        if (htmlFile==null){
            return;
        }
        String objectPath="course/"+courseId+".html";
        Boolean resBoolean = coursePublishService.uploadFileToMinio(courseId, htmlFile, objectPath);
        if (resBoolean==null|| !resBoolean){
            return;
        }
        System.gc();
        htmlFile.delete();
        //执行成功后,更新第一阶段状态为完成状态。
        mqMessageService.completedStageOne(taskId);
    }

    private void createElasticsearchIndex(MqMessage mqMessage, Long courseId) {
        MqMessageService mqMessageService = this.getMqMessageService();
        //幂等性处理（保证无论调用多少次，但是只成功执行一次）
        Long taskId = mqMessage.getId();
        int stageTwo = mqMessageService.getStageTwo(taskId);
        if (stageTwo >= 1) {
            log.debug("第二阶段添加课程索引到elasticsearch，无需重复执行。。");
            return;
        }


        //添加课程索引到Elasticsearch中
        CoursePublish coursePublish = coursePublishMapper.selectById(courseId);
        if (coursePublish==null){
            return;
        }
        CourseIndex courseIndex = new CourseIndex();
        BeanUtils.copyProperties(coursePublish,courseIndex);
        Boolean aBoolean = coursePublishService.addCourseIndex(courseIndex);
        if (aBoolean==null||!aBoolean){
            log.error("添加课程索引到elasticsearch失败。。。");
            return;
        }
        //执行成功后,更新第一阶段状态为完成状态。
        mqMessageService.completedStageTwo(taskId);
    }

    private void saveCourseCacheToRedis(MqMessage mqMessage, Long courseId) {
        MqMessageService mqMessageService = this.getMqMessageService();
        //幂等性处理（保证无论调用多少次，但是只成功执行一次）
        Long taskId = mqMessage.getId();
        int stageThree = mqMessageService.getStageThree(taskId);
        if (stageThree >= 1) {
            log.debug("第三阶段将课程信息保存到redis中，无需重复执行。。");
            return;
        }

        //将课程缓存传入redis中
        //TODO
        //执行成功后,更新第一阶段状态为完成状态。
        mqMessageService.completedStageThree(taskId);
    }


}
