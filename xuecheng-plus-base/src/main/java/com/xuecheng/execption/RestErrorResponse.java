package com.xuecheng.execption;

import lombok.Data;

/**
 * @Author gc
 * @Description 统一异常数据响应类(响应给前端的数据模型)
 * @DateTime: 2025/5/14 0:17
 **/
@Data
public class RestErrorResponse {
    private String errMessage;
    public RestErrorResponse(String errMessage) {
        this.errMessage = errMessage;
    }
}
