package com.xuecheng.content.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author gc
 * @Description 绑定教学计划的媒资文件
 * @DateTime: 2025/5/20 16:21
 **/
@ApiModel(description = "绑定教学计划媒资传输模型")
@Data
public class BindTeachplanMediaDto {
    @ApiModelProperty(value = "媒资文件id", required = true)
    private String mediaId;
    @ApiModelProperty(value = "媒资文件名称", required = true)
    private String fileName;
    @ApiModelProperty(value = "课程计划标识", required = true)
    private Long teachplanId;
}
