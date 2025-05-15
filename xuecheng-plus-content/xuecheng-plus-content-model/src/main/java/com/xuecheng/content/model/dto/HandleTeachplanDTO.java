package com.xuecheng.content.model.dto;

import com.xuecheng.content.model.po.Teachplan;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @description 操作课程计划(添加修改)DTO
 * @author gc
 * @date 2022/9/6 14:36
 * @version 1.0
 */
@Data
@ToString
@NoArgsConstructor
@ApiModel(description = "课程计划DTO")
public class HandleTeachplanDTO extends Teachplan {

}
