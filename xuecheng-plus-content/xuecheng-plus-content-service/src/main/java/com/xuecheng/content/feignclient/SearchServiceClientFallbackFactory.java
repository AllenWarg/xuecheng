package com.xuecheng.content.feignclient;

import com.xuecheng.content.model.dto.CourseIndex;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @Author gc
 * @Description 搜索服务远程调用降级方法
 * @DateTime: 2025/5/22 19:40
 **/
@Component
@Slf4j
public class SearchServiceClientFallbackFactory implements FallbackFactory<SearchServiceClient> {
    @Override
    public SearchServiceClient create(Throwable throwable) {
        return new SearchServiceClient() {
            @Override
            public Boolean add(CourseIndex courseIndex) {
                log.error("搜索服务远程调用失败,课程id:{}",courseIndex.getId(),throwable.toString(),throwable);
                return null;
            }
        };
    }
}
