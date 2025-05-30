package com.xuecheng.ucenter.feignClient;

import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CheckCodeClientfallbackFactory implements FallbackFactory<CheckCodeClient> {
    @Override
    public CheckCodeClient create(Throwable throwable) {
        return new CheckCodeClient() {
            @Override
            public Boolean verify(String key, String code) {
                log.error("feign远程调用验证码认证服务失败,发生熔断，触发降级方法！原因：{}",throwable.toString(),throwable);
                return null;
            }
        };
    }
}
