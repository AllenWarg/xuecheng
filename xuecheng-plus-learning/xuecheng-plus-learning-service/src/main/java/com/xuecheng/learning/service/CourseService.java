package com.xuecheng.learning.service;

import com.rabbitmq.client.Channel;
import com.xuecheng.content.model.po.CoursePublish;
import com.xuecheng.learning.model.dto.MyCourseTableParams;
import com.xuecheng.learning.model.dto.XcChooseCourseDto;
import com.xuecheng.learning.model.dto.XcCourseTablesDto;
import com.xuecheng.learning.model.po.XcCourseTables;
import com.xuecheng.model.PageResult;
import org.springframework.amqp.core.Message;

/**
 * 课程相关服务
 */
public interface CourseService {

    /**
     * 选课
     * @param courseId 课程id
     * @param userId 用户id
     * @return
     */
    public XcChooseCourseDto addChooseCourse(String userId,Long courseId);


    public XcChooseCourseDto saveChooseCourse(CoursePublish coursepublish, String userId);

    /**
     * 查询学习资格
     * @param courseId 课程id
     * @return
     */
    XcCourseTablesDto getLearnstatus(String userId,Long courseId);

    /**
     * 接收消息队列中课程支付成功消息
     * @param message 消息
     * @param channel 信道
     */
    public void receive(Message message, Channel channel);

    /**
     * 查询我的课表
     * @userId userid 用户id
     * @param  params 查询条件
     * @return
     */
    PageResult<XcCourseTables> queryMyCourseTable(MyCourseTableParams params);
}
