package com.xuecheng.orders.service.impl;

import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.AlipayConfig;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayTradeQueryModel;
import com.alipay.api.domain.AlipayTradeWapPayModel;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.internal.util.StringUtils;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.request.AlipayTradeWapPayRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.alipay.api.response.AlipayTradeWapPayResponse;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.execption.XueChengException;
import com.xuecheng.messagesdk.model.po.MqMessage;
import com.xuecheng.messagesdk.service.MqMessageService;
import com.xuecheng.orders.config.PayNotifyConfig;
import com.xuecheng.orders.mapper.XcOrdersGoodsMapper;
import com.xuecheng.orders.mapper.XcOrdersMapper;
import com.xuecheng.orders.mapper.XcPayRecordMapper;
import com.xuecheng.orders.model.dto.AddOrderDto;
import com.xuecheng.orders.model.dto.PayRecordDto;
import com.xuecheng.orders.model.po.XcOrders;
import com.xuecheng.orders.model.po.XcOrdersGoods;
import com.xuecheng.orders.model.po.XcPayRecord;
import com.xuecheng.orders.service.OrderService;
import com.xuecheng.utils.IdWorkerUtils;
import com.xuecheng.utils.QRCodeUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Service
public class OrderServiceImpl implements OrderService {
    @Value("${pay.qrcodeurl}")
    String qrcodeurl;
    @Autowired
    AlipayConfig alipayConfig;
    @Autowired
    XcOrdersMapper xcOrdersMapper;
    @Autowired
    XcPayRecordMapper xcPayRecordMapper;
    @Autowired
    XcOrdersGoodsMapper xcOrdersGoodsMapper;
    @Autowired
    MqMessageService mqMessageService;
    @Autowired
    RabbitTemplate rabbitTemplate;

    @Transactional
    @Override
    public PayRecordDto generatePayOrder(AddOrderDto addOrderDto, Long userId) {
        // 1.创建商品订单和订单对应商品
        XcOrders xcOrders = saveXcOrders(addOrderDto, userId);
        XcOrdersGoods xcOrdersGoods = saveXcOrdersGoods(xcOrders);
        // 2.生成支付记录
        XcPayRecord xcPayRecord = saveXcPayRecord(xcOrders);
        // 3.根据支付记录生成订单二维码
        if (xcPayRecord==null) {
            return null;
        }

        String payUrl = qrcodeurl + xcPayRecord.getPayNo();
        String qrCode = null;
        try {
            qrCode = new QRCodeUtil().createQRCode(payUrl, 200, 200);
        } catch (IOException e) {
            XueChengException.cast("生成支付二维码出错。。。");
        }
        PayRecordDto payRecordDto = new PayRecordDto();
        BeanUtils.copyProperties(xcPayRecord, payRecordDto);
        payRecordDto.setQrcode(qrCode);
        return payRecordDto;
    }


    /**
     * 创建订单商品
     *
     * @param xcOrders
     * @return
     */
    @Transactional
    private XcOrdersGoods saveXcOrdersGoods(XcOrders xcOrders) {
        if (xcOrders==null) {
            return null;
        }
        XcOrdersGoods xcOrdersGoods = xcOrdersGoodsMapper
                .selectOne(new LambdaQueryWrapper<XcOrdersGoods>().eq(XcOrdersGoods::getOrderId, xcOrders.getId()));
        if (xcOrdersGoods!=null) {
            return xcOrdersGoods;
        }

        xcOrdersGoods = new XcOrdersGoods();
        xcOrdersGoods.setOrderId(xcOrders.getId());
        xcOrdersGoods.setGoodsId(xcOrders.getOutBusinessId());
        xcOrdersGoods.setGoodsType(xcOrders.getOrderType());
        xcOrdersGoods.setGoodsName(xcOrders.getOrderName());
        xcOrdersGoods.setGoodsPrice(xcOrders.getTotalPrice());
        int insert = xcOrdersGoodsMapper.insert(xcOrdersGoods);
        if (insert <= 0) {
            return null;
        }
        return xcOrdersGoodsMapper.selectById(xcOrdersGoods.getId());
    }


    /**
     * 查询支付结果
     *
     * @param payNo 支付记录订单
     * @return
     */
    @Transactional
    public PayRecordDto queryPayResult(String payNo) {
        if (StringUtils.isEmpty(payNo)) {
            return null;
        }
        PayRecordDto payRecordDto = new PayRecordDto();
        XcPayRecord xcPayRecord = xcPayRecordMapper.selectOne(new LambdaQueryWrapper<XcPayRecord>().eq(XcPayRecord::getPayNo, payNo));
        XcOrders xcOrders = xcOrdersMapper.selectById(xcPayRecord.getOrderId());
        if (xcPayRecord.getStatus().equals("601002") && xcOrders.getStatus().equals("601002")) {
            BeanUtils.copyProperties(xcPayRecord, payRecordDto);
            return payRecordDto;
        }
        // 初始化SDK
        AlipayClient alipayClient = null;
        String body = null;
        AlipayTradeQueryResponse response = null;
        try {
            alipayClient = new DefaultAlipayClient(alipayConfig);
            // 构造请求参数以调用接口
            AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
            AlipayTradeQueryModel model = new AlipayTradeQueryModel();
            // 设置订单支付时传入的商户订单号
            model.setOutTradeNo(payNo);
            request.setBizModel(model);
            response = alipayClient.execute(request);
            if (!response.isSuccess()) {
                return null;
            }
            body = response.getBody();
        } catch (AlipayApiException e) {
            XueChengException.cast("暂未支付，有问题请联系管理员！");
        }

        String tradeStatus = response.getTradeStatus();
        String outTradeNo = response.getOutTradeNo();

        if ("TRADE_SUCCESS".equals(tradeStatus)) {
            // 保存外部系统支付记录号
            xcPayRecord.setOutPayNo(response.getTradeNo());
            // 保存支付成功消息
            saveAliPayStatus(xcOrders, xcPayRecord);
        }
        BeanUtils.copyProperties(xcPayRecord, payRecordDto);
        return payRecordDto;

    }

    /**
     * 支付宝支付通知
     *
     * @param params
     */
    @Transactional
    public void alipayNotify(Map<String, String> params) {
        // 获取支付宝的通知返回参数，可参考技术文档中页面跳转同步通知参数列表(以下仅供参考)//
        // 商户订单号
        String out_trade_no = params.get("out_trade_no");
        // 支付宝交易号
        String trade_no = params.get("trade_no");
        // 交易状态
        String trade_status = params.get("trade_status");

        XcPayRecord xcPayRecord = xcPayRecordMapper
                .selectOne(new LambdaQueryWrapper<XcPayRecord>().eq(XcPayRecord::getPayNo, out_trade_no));

        XcOrders xcOrders = xcOrdersMapper.selectById(xcPayRecord.getOrderId());
        // 获取支付宝的通知返回参数，可参考技术文档中页面跳转同步通知参数列表(以上仅供参考)//
        // 计算得出通知验证结果
        // boolean AlipaySignature.rsaCheckV1(Map<String, String> params, String publicKey, String charset, String sign_type)
        boolean verify_result = false;
        try {
            verify_result = AlipaySignature.rsaCheckV1(params, alipayConfig.getAlipayPublicKey(), alipayConfig.getCharset(), "RSA2");
        } catch (AlipayApiException e) {
            throw new RuntimeException(e);
        }

        if (verify_result) {// 验证成功
            //////////////////////////////////////////////////////////////////////////////////////////
            // 请在这里加上商户的业务逻辑程序代码
            System.out.println(String.valueOf(verify_result));
            //——请根据您的业务逻辑来编写程序（以下代码仅作参考）——

            if (trade_status.equals("TRADE_FINISHED")) {
                // 判断该笔订单是否在商户网站中已经做过处理
                // 如果没有做过处理，根据订单号（out_trade_no）在商户网站的订单系统中查到该笔订单的详细，并执行商户的业务程序
                // 请务必判断请求时的total_fee、seller_id与通知时获取的total_fee、seller_id为一致的
                // 如果有做过处理，不执行商户的业务程序
                System.out.println(trade_status);
                // 注意：
                // 如果签约的是可退款协议，退款日期超过可退款期限后（如三个月可退款），支付宝系统发送该交易状态通知
                // 如果没有签约可退款协议，那么付款完成后，支付宝系统发送该交易状态通知。
            } else if (trade_status.equals("TRADE_SUCCESS")) {
                // 判断该笔订单是否在商户网站中已经做过处理
                // 如果没有做过处理，根据订单号（out_trade_no）在商户网站的订单系统中查到该笔订单的详细，并执行商户的业务程序
                // 请务必判断请求时的total_fee、seller_id与通知时获取的total_fee、seller_id为一致的
                // 如果有做过处理，不执行商户的业务程序
                xcPayRecord.setOutPayNo(params.get("trade_no"));
                saveAliPayStatus(xcOrders, xcPayRecord);

                // 注意：
                // 如果签约的是可退款协议，那么付款完成后，支付宝系统发送该交易状态通知。
            }

        }
    }

    /**
     * 扫码支付订单
     *
     * @param payNo 支付订单号
     */
    public String payOrder(String payNo) {
        XcPayRecord xcPayRecord = xcPayRecordMapper
                .selectOne(new LambdaQueryWrapper<XcPayRecord>().eq(XcPayRecord::getPayNo, payNo));
        if (xcPayRecord==null) {
            log.error("查询支付订单时出错，pyaNo={}", payNo);
            XueChengException.cast("扫码支付失败！请联系管理员。");
            return null;
        }
        AlipayClient alipayClient = null;
        String pageRedirectionData = null;
        try {
            alipayClient = new DefaultAlipayClient(alipayConfig);
            AlipayTradeWapPayRequest request = new AlipayTradeWapPayRequest();
            AlipayTradeWapPayModel model = new AlipayTradeWapPayModel();
            model.setOutTradeNo(xcPayRecord.getPayNo().toString());
            model.setTotalAmount(xcPayRecord.getTotalPrice().toString());
            model.setSubject(xcPayRecord.getOrderName());
            model.setProductCode("QUICK_WAP_WAY");
            request.setBizModel(model);
            request.setNotifyUrl("http://alipaytest.594000.xyz/orders/alipayNotify");
            AlipayTradeWapPayResponse response = alipayClient.pageExecute(request, "POST");
            pageRedirectionData = response.getBody();
        } catch (AlipayApiException e) {
            log.error("扫码支付订单时出错，pyaNo={}", payNo);
            XueChengException.cast("扫码支付失败！");
        }
        return pageRedirectionData;
    }


    private XcPayRecord saveXcPayRecord(XcOrders xcOrders) {
        if (xcOrders==null) {
            return null;
        }
        XcPayRecord xcPayRecord = xcPayRecordMapper.selectOne(new LambdaQueryWrapper<XcPayRecord>().eq(XcPayRecord::getOrderId, xcOrders.getId()));
        if (xcPayRecord!=null) {
            return xcPayRecord;
        }
        xcPayRecord = new XcPayRecord();
        xcPayRecord.setId(generateRandomId());
        xcPayRecord.setPayNo(generateRandomId());
        xcPayRecord.setOrderId(xcOrders.getId());
        xcPayRecord.setOrderName(xcOrders.getOrderName());
        xcPayRecord.setTotalPrice(xcOrders.getTotalPrice());
        xcPayRecord.setCurrency("CNY");
        xcPayRecord.setCreateDate(LocalDateTime.now());
        //[{"code":"601001","desc":"未支付"},{"code":"601002","desc":"已支付"},{"code":"601003","desc":"已退款"}]
        xcPayRecord.setStatus("601001");
        xcPayRecord.setUserId(xcOrders.getUserId());
        int insert = xcPayRecordMapper.insert(xcPayRecord);
        if (insert <= 0) {
            return null;
        }
        return xcPayRecordMapper.selectById(xcPayRecord.getId());
    }

    /**
     * 生成时间戳+5位随机数的id
     */
    private Long generateRandomId() {

        // Long millis = System.currentTimeMillis();
        // Long randomNum = (long) (new Random().nextInt(90000) + 10000);
        // Long orderId = Long.valueOf(millis.toString() + randomNum.toString());
        // return orderId;
        return IdWorkerUtils.getInstance().nextId();
    }

    /**
     * 保存订单
     *
     * @param addOrderDto
     * @return
     */
    @Transactional
    private XcOrders saveXcOrders(AddOrderDto addOrderDto, Long userId) {
        XcOrders xcOrders = xcOrdersMapper
                .selectOne(new LambdaQueryWrapper<XcOrders>().eq(XcOrders::getOutBusinessId, addOrderDto.getOutBusinessId()));
        if (xcOrders!=null) {
            return xcOrders;
        }
        xcOrders = new XcOrders();
        BeanUtils.copyProperties(addOrderDto, xcOrders);
        Long orderId = generateRandomId();
        xcOrders.setId(orderId);
        xcOrders.setCreateDate(LocalDateTime.now());
        //[{"code":"600001","desc":"未支付"},{"code":"600002","desc":"已支付"},{"code":"600003","desc":"已关闭"},{"code":"600004","desc":"已退款"},{"code":"600005","desc":"已完成"}]
        xcOrders.setStatus("600001");
        xcOrders.setUserId(userId.toString());
        int insert = xcOrdersMapper.insert(xcOrders);
        if (insert <= 0) {
            return null;
        }
        return xcOrdersMapper.selectById(orderId);
    }

    @Transactional
    // 保存支付状态
    public void saveAliPayStatus(XcOrders xcOrders, XcPayRecord xcPayRecord) {
        //[{"code":"601001","desc":"未支付"},{"code":"601002","desc":"已支付"},{"code":"601003","desc":"已退款"}]
        xcPayRecord.setStatus("601002");
        xcOrders.setStatus("601002");
        xcOrdersMapper.updateById(xcOrders);
        xcPayRecordMapper.updateById(xcPayRecord);
        log.debug("===========课程支付成功！支付交易id：{}，订单id：{}" + xcPayRecord.getOrderId(), xcOrders.getId());
        // 添加支付成功的消息到数据库
        MqMessage payresultNotify = mqMessageService
                .addMessage("payresult_notify", xcOrders.getOutBusinessId(), xcOrders.getOrderType(), null);
        // 发送选课支付成功消息到rabbitMQ
        notifyPayResult(payresultNotify);
    }

    /**
     * 发送选消息到rabbitMQ
     *
     * @param message
     */
    @Override
    public void notifyPayResult(MqMessage message) {
        // 消息
        String jsonString = JSON.toJSONString(message);
        // 设置消息持久化
        Message msgObj = MessageBuilder.withBody(jsonString.getBytes(StandardCharsets.UTF_8))
                .setDeliveryMode(MessageDeliveryMode.PERSISTENT)
                .build();
        // 2.全局唯一的消息ID，需要封装到CorrelationData中
        CorrelationData correlationData = new CorrelationData(message.getId().toString());
        // 3.添加callback
        correlationData.getFuture().addCallback(result -> {
                    if (result.isAck()) {
                        // 3.1.ack，消息成功
                        log.debug("通知支付结果消息发送成功, ID:{}", correlationData.getId());
                        // 删除消息表中的记录
                        mqMessageService.completed(message.getId());
                    } else {
                        // 3.2.nack，消息失败
                        log.error("通知支付结果消息发送失败, ID:{}, 原因{}", correlationData.getId(), result.getReason());
                    }
                },
                ex -> log.error("消息发送异常, ID:{}, 原因{}", correlationData.getId(), ex.getMessage())
        );
        // 4.发送消息
        rabbitTemplate.convertAndSend(PayNotifyConfig.PAYNOTIFY_EXCHANGE_FANOUT, "", msgObj, correlationData);
    }

}
