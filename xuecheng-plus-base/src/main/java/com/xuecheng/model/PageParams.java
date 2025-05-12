package com.xuecheng.model;

import io.swagger.annotations.ApiParam;
import lombok.Data;
import lombok.ToString;

/**
 * @description 分页查询通用参数
 * @author Mr.M
 * @date 2022/9/6 14:02
 * @version 1.0
 */
@Data
@ToString
public class PageParams {
    @ApiParam("当前页面")
    //当前页码
    private Long pageNo = 1L;
    @ApiParam("每页条数")
    //每页记录数默认值
    private Long pageSize =10L;

    public PageParams(){

    }

    public PageParams(long pageNo,long pageSize){
        this.pageNo = pageNo;
        this.pageSize = pageSize;
    }

}

