package com.xuecheng.orders.service;

import com.xuecheng.messagesdk.model.po.MqMessage;
import com.xuecheng.orders.model.dto.AddOrderDto;
import com.xuecheng.orders.model.dto.PayRecordDto;

import java.util.Map;

/**
 * 订单相关操作
 */
public interface OrderService {

    /**
     * 生成支付订单二维码
     * @param addOrderDto 生成订单传递模型
     * @param userId 用户id
     * @return
     */
    PayRecordDto generatePayOrder(AddOrderDto addOrderDto,Long userId);

    /**
     * 扫码支付订单
     * @param payNo
     */
    String payOrder(String payNo);

    /**
     * 支付宝支付通知
     * @param params
     */
    void alipayNotify(Map<String, String> params);

    /**
     * 查询支付结果
     * @param payNo 支付记录订单
     * @return
     */
    PayRecordDto queryPayResult(String payNo);



    /**
     * 发送通知结果
     * @param message
     */
    public void notifyPayResult(MqMessage message);
}
