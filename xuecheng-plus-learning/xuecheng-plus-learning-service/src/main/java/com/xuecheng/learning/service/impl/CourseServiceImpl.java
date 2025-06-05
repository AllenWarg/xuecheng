package com.xuecheng.learning.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rabbitmq.client.Channel;
import com.xuecheng.content.model.po.CoursePublish;
import com.xuecheng.execption.XueChengException;
import com.xuecheng.learning.config.PayNotifyConfig;
import com.xuecheng.learning.feignclient.ContentServiceClient;
import com.xuecheng.learning.mapper.XcChooseCourseMapper;
import com.xuecheng.learning.mapper.XcCourseTablesMapper;
import com.xuecheng.learning.model.dto.MyCourseTableParams;
import com.xuecheng.learning.model.dto.XcChooseCourseDto;
import com.xuecheng.learning.model.dto.XcCourseTablesDto;
import com.xuecheng.learning.model.po.XcChooseCourse;
import com.xuecheng.learning.model.po.XcCourseTables;
import com.xuecheng.learning.service.CourseService;
import com.xuecheng.messagesdk.model.po.MqMessage;
import com.xuecheng.model.PageResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class CourseServiceImpl implements CourseService {
    @Autowired
    ContentServiceClient contentServiceClient;
    @Autowired
    XcChooseCourseMapper xcChooseCourseMapper;
    @Autowired
    XcCourseTablesMapper xcCourseTablesMapper;
    @Autowired
    CourseService courseService_prox;

    /**
     * 选课
     *
     * @param courseId 课程id
     * @return
     */
    @Override
    @Transactional
    public XcChooseCourseDto addChooseCourse(String userId, Long courseId) {
        CoursePublish coursepublish = contentServiceClient.getCoursepublish(courseId);
        if (coursepublish==null) {
            return null;
        }
        if (!coursepublish.getStatus().equals("203002")) {
            return null;
        }
        XcChooseCourseDto xcChooseCourseDto = saveChooseCourse(coursepublish, userId);
        return xcChooseCourseDto;
    }

    @Transactional
    public XcChooseCourseDto saveChooseCourse(CoursePublish coursepublish, String userId) {
        XcChooseCourseDto xcChooseCourseDto = new XcChooseCourseDto();
        LambdaQueryWrapper<XcChooseCourse> qw = new LambdaQueryWrapper<XcChooseCourse>()
                .eq(XcChooseCourse::getCourseId, coursepublish.getId())
                .eq(XcChooseCourse::getUserId,userId);
        XcChooseCourse xcChooseCourse = xcChooseCourseMapper.selectOne(qw);
        if (xcChooseCourse==null) {
            xcChooseCourse = new XcChooseCourse();
        } else {
            BeanUtils.copyProperties(xcChooseCourse, xcChooseCourseDto);
            //[{"code":"701001","desc":"选课成功"},{"code":"701002","desc":"待支付"}]
            if (xcChooseCourse.getStatus().equals("701001")) {
                //[{"code":"702001","desc":"正常学习"},{"code":"702002","desc":"没有选课或选课后没有支付"},{"code":"702003","desc":"已过期需要申请续期或重新支付"}]
                xcChooseCourseDto.setLearnStatus("702001");
            }
            if (xcChooseCourse.getStatus().equals("701002")) {
                xcChooseCourseDto.setLearnStatus("702002");
            }
            long endTime = xcChooseCourseDto.getValidtimeEnd().toEpochSecond(ZoneOffset.of("+8"));
            long nowTime = Instant.now().getEpochSecond();
            if (endTime - nowTime <= 0) {
                xcChooseCourseDto.setLearnStatus("702003");
            }
            return xcChooseCourseDto;
        }

        String market = coursepublish.getMarket();
        Map<String, String> marketMap = JSON.parseObject(market, Map.class);
        String charge = coursepublish.getCharge();

        BeanUtils.copyProperties(coursepublish, xcChooseCourse);
        xcChooseCourse.setCourseId(coursepublish.getId());
        xcChooseCourse.setCourseName(coursepublish.getName());
        LocalDateTime now = LocalDateTime.now();
        Integer validDays = coursepublish.getValidDays();
        xcChooseCourse.setCreateDate(now);
        xcChooseCourse.setValidtimeStart(now);
        xcChooseCourse.setValidtimeEnd(now.plusDays(validDays));
        xcChooseCourse.setId(null);
        xcChooseCourse.setCoursePrice(coursepublish.getPrice());
        xcChooseCourse.setUserId(userId);
        //[{"code":"201000","desc":"免费"},{"code":"201001","desc":"收费"}]
        if (charge.equals("201000")) {
            // 免费
            //[{"code":"700001","desc":"免费课程"},{"code":"700002","desc":"收费课程"}]
            xcChooseCourse.setOrderType("700001");
            //[{"code":"701001","desc":"选课成功"},{"code":"701002","desc":"待支付"}]
            xcChooseCourse.setStatus("701001");
            // 保存选择课程表
            int insert = xcChooseCourseMapper.insert(xcChooseCourse);
            // 保存课程表
            XcCourseTables xcCourseTables = saveCourseTables(xcChooseCourse);
            BeanUtils.copyProperties(xcChooseCourse, xcChooseCourseDto);
            if (xcCourseTables!=null && insert >= 0) {
                xcChooseCourseDto.setLearnStatus("702001");
            }
            return xcChooseCourseDto;
        }

        if (charge.equals("201001")) {
            // 收费
            xcChooseCourse.setOrderType("700002");
            xcChooseCourse.setStatus("701002");
            xcChooseCourseMapper.insert(xcChooseCourse);
            BeanUtils.copyProperties(xcChooseCourse, xcChooseCourseDto);
            xcChooseCourseDto.setLearnStatus("702002");
            return xcChooseCourseDto;
        }
        return null;
    }

    @Transactional
    private XcCourseTables saveCourseTables(XcChooseCourse xcChooseCourse) {
        if (xcChooseCourse==null) {
            return null;
        }
        XcCourseTables xcCourseTables = xcCourseTablesMapper
                .selectOne(new LambdaQueryWrapper<XcCourseTables>().eq(XcCourseTables::getChooseCourseId, xcChooseCourse.getId()));
        if (xcCourseTables!=null) {
            return xcCourseTables;
        } else {
            xcCourseTables = new XcCourseTables();
        }
        BeanUtils.copyProperties(xcChooseCourse, xcCourseTables);
        xcCourseTables.setCourseType(xcChooseCourse.getOrderType());
        xcCourseTables.setChooseCourseId(xcChooseCourse.getId());
        xcCourseTablesMapper.insert(xcCourseTables);
        return xcCourseTablesMapper.selectById(xcCourseTables.getId());
    }

    /**
     * 查询学习资格
     *
     * @param courseId 课程id
     * @return
     */
    public XcCourseTablesDto getLearnstatus(String userId, Long courseId) {
        XcCourseTablesDto xcCourseTablesDto = new XcCourseTablesDto();
        CoursePublish coursepublish = contentServiceClient.getCoursepublish(courseId);
        if (coursepublish==null) {
            return null;
        }
        if (!coursepublish.getStatus().equals("203002")) {
            return null;
        }
        XcChooseCourseDto xcChooseCourseDto = courseService_prox.saveChooseCourse(coursepublish, userId);
        BeanUtils.copyProperties(xcChooseCourseDto, xcCourseTablesDto);
        xcCourseTablesDto.setChooseCourseId(xcChooseCourseDto.getId());
        return xcCourseTablesDto;
    }


    /**
     * 接收消息队列中课程支付成功消息
     * @param message 消息
     * @param channel 信道
     */
    @RabbitListener(queues = PayNotifyConfig.PAYNOTIFY_QUEUE)
    public void receive(Message message, Channel channel){
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        MqMessage mqMessage = JSON.parseObject(message.getBody(), MqMessage.class);
        log.debug("学习中心服务接收支付结果:{}", mqMessage);
        //消息类型
        String messageType = mqMessage.getMessageType();
        //选课id
        String chooseCourseId = mqMessage.getBusinessKey1();
        //订单类型,60201表示购买课程
        String orderType = mqMessage.getBusinessKey2();
        //只处理购买课程成功的消息通知
        if (PayNotifyConfig.MESSAGE_TYPE.equals(messageType)&&"60201".equals(orderType)){
            XcChooseCourse xcChooseCourse = xcChooseCourseMapper.selectById(chooseCourseId);
            if (xcChooseCourse==null){
                log.debug("学习服务接收消息通知，查询选课表记录为空，出错的消息：{}",mqMessage.toString());
                return;
            }
            if (!xcChooseCourse.getStatus().equals("701002")){
                log.debug("学习服务接收消息通知，查询选课表记录状态不为待支付，出错的消息：{}",mqMessage.toString());
                return;
            }
            XcCourseTables xcCourseTables = saveCourseTables(xcChooseCourse);
            if (xcCourseTables==null){
                log.debug("学习服务接收消息通知，保存我的课程表记录出错，出错的消息：{}",mqMessage.toString());
                XueChengException.cast("学习服务接收消息通知，保存我的课程表记录出错，选课记录id："+chooseCourseId);
                return;
            }
            xcChooseCourse.setStatus("701001");
            int i = xcChooseCourseMapper.updateById(xcChooseCourse);
            if (i<=0){
                XueChengException.cast("学习服务接收消息通知，更改选课表记录出错，选课记录id："+chooseCourseId);
            }
        }

    }


    /**
     * 查询我的课表
     * @param params
     * @return
     */
    public PageResult<XcCourseTables> queryMyCourseTable(MyCourseTableParams params){
        Page<XcCourseTables> page = new Page<>(params.getPage(), params.getSize());
        List<XcCourseTables> xcCourseTablesList = xcCourseTablesMapper.selectMyCourseTables(page,params);
        PageResult<XcCourseTables> xcCourseTablesPageResult = new PageResult<>();
        xcCourseTablesPageResult.setItems(xcCourseTablesList);
        long total = page.getTotal();
        long current = page.getCurrent();
        long size = page.getSize();
        xcCourseTablesPageResult.setPageSize(size);
        xcCourseTablesPageResult.setPage(current);
        xcCourseTablesPageResult.setCounts(total);
        return xcCourseTablesPageResult;
    }
}
