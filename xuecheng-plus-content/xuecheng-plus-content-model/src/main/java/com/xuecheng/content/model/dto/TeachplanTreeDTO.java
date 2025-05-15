package com.xuecheng.content.model.dto;

import com.xuecheng.content.model.po.Teachplan;
import com.xuecheng.content.model.po.TeachplanMedia;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

/**
 * @description 课程计划DTO
 * @author gc
 * @date 2022/9/6 14:36
 * @version 1.0
 */
@Data
@ToString
@NoArgsConstructor
@ApiModel(description = "课程计划DTO")
public class TeachplanTreeDTO extends Teachplan {
    TeachplanMedia teachplanMedia;
    List<TeachplanTreeDTO> teachPlanTreeNodes;
}
