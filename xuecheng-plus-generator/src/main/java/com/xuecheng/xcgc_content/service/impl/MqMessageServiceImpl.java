package com.xuecheng.xcgc_content.service.impl;

import com.xuecheng.xcgc_content.model.po.MqMessage;
import com.xuecheng.xcgc_content.mapper.MqMessageMapper;
import com.xuecheng.xcgc_content.service.MqMessageService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author gc
 */
@Slf4j
@Service
public class MqMessageServiceImpl extends ServiceImpl<MqMessageMapper, MqMessage> implements MqMessageService {

}
