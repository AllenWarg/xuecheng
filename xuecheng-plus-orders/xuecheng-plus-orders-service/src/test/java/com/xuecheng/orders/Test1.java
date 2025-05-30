package com.xuecheng.orders;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.AlipayConfig;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayTradeQueryModel;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author Mr.M
 * @version 1.0
 * @description TODO
 * @date 2022/10/2 10:32
 */
@SpringBootTest
public class Test1 {
    @Autowired
    AlipayConfig alipayConfig;

    /**
     * 查询支付宝订单支付信息
     * @throws AlipayApiException
     */
    @Test
    public void queryOrderStatusTest() throws AlipayApiException {
        // 初始化SDK
        AlipayClient alipayClient = new DefaultAlipayClient(alipayConfig);

        // 构造请求参数以调用接口
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        AlipayTradeQueryModel model = new AlipayTradeQueryModel();
        // 设置订单支付时传入的商户订单号
        model.setOutTradeNo("70501111111S001111119");
        request.setBizModel(model);
        AlipayTradeQueryResponse response = alipayClient.execute(request);
        String body = response.getBody();
        System.out.println(body);
    }

}
