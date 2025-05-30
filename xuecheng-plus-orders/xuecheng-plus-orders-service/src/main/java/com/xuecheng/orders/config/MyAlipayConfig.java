package com.xuecheng.orders.config;

import com.alipay.api.AlipayConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Mr.M
 * @version 1.0
 * @description 支付宝配置参数
 * @date 2022/10/20 22:45
 */
@Configuration
public class MyAlipayConfig {
    @Value("${alipay.serverUrl}")
    private String serverUrl;
    @Value("${alipay.appId}")
    private String appId;
    private String format = "json";
    private String charset = "utf-8";
    private String signType = "RSA2";
    @Value("${alipay.privateKey}")
    private String privateKey;
    @Value("${alipay.alipayPublicKey}")
    private String alipayPublicKey;


    @Bean
    public AlipayConfig alipayConfig(){
        AlipayConfig alipayConfig = new AlipayConfig();
        alipayConfig.setAppId(appId);
        alipayConfig.setServerUrl(serverUrl);
        alipayConfig.setCharset(charset);
        alipayConfig.setPrivateKey(privateKey);
        alipayConfig.setAlipayPublicKey(alipayPublicKey);
        alipayConfig.setFormat(format);
        alipayConfig.setSignType(signType);
        return alipayConfig;
    }
}