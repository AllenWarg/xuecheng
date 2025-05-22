package com.xuecheng.content.feignclient;

import com.xuecheng.content.model.dto.CourseIndex;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @Author gc
 * @Description 搜索服务远程调用客户端
 * @DateTime: 2025/5/22 19:39
 **/

@FeignClient(value = "search",fallbackFactory = SearchServiceClientFallbackFactory.class)
public interface SearchServiceClient {

    @PostMapping("/search/index/course")
    public Boolean add(@RequestBody CourseIndex courseIndex);
}

