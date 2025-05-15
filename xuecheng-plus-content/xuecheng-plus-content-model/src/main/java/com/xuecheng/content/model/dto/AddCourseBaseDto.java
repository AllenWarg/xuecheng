package com.xuecheng.content.model.dto;

import com.xuecheng.execption.ValidationGroups;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.util.Date;

/**
 * @description 课程添加和修改Dto
 * @author gc
 * @date 2022/9/6 14:36
 * @version 1.0
 */
@Data
@ToString
@NoArgsConstructor
@ApiModel(description = "课程添加DTO")
public class AddCourseBaseDto {
    private static final long serialVersionUID = 1L;

    /**
     * 课程id
     */
    private long id;
    /**
     * 课程名称
     */
    @ApiModelProperty(value = "课程名称",required = true)
    @NotEmpty(message = "新增课程名称不能为空",groups = ValidationGroups.Inster.class)
    @NotEmpty(message = "修改课程名称不能为空",groups = ValidationGroups.Update.class)
    private String name;

    /**
     * 适用人群
     */
    @ApiModelProperty(value = "适用人群",required = true)
    @NotEmpty(message = "适用人群不能为空")
    @Size(message = "适用人群内容过少",min = 5)
    private String users;

    /**
     * 课程标签
     */
    @ApiModelProperty(value = "课程标签",required = true)
    @NotEmpty
    private String tags;

    /**
     * 大分类
     */
    @ApiModelProperty(value = "大分类",required = true)
    @NotEmpty(message = "课程大分类不能为空")
    private String mt;

    /**
     * 大分类名称
     */
    private String mtName;


    /**
     * 小分类
     */
    @ApiModelProperty(value = "小分类",required = true)
    @NotEmpty(message = "课程小分类不能为空")
    private String st;
    /**
     * 小分类名称
     */
    private String stName;
    /**
     * 课程等级
     */
    @ApiModelProperty(value = "课程等级",required = true)
    @NotEmpty(message = "课程等级不能为空")
    private String grade;

    /**
     * 教育模式(common普通，record 录播，live直播等）
     */
    @ApiModelProperty(value = "教育模式(common普通，record 录播，live直播等）",required = true)
    @NotEmpty(message = "教育模式不能为空")
    private String teachmode;

    /**
     * 课程介绍
     */
    @ApiModelProperty(value = "课程介绍",required = true)
    @NotEmpty(message = "课程介绍不能为空")
    private String description;

    /**
     * 课程图片
     */
    private String pic;

    /**
     * 收费规则，对应数据字典
     */
    @ApiModelProperty(value = "收费规则",required = true)
    @NotEmpty(message = "收费规则不能为空")
    private String charge;

    /**
     * 现价
     */
    @Min(value = 0,message = "现价不能小于0")
    @ApiModelProperty(value = "现价",required = true)
    private Float price;
    /**
     * 原价
     */
    @Min(value = 0,message = "原价不能小于0")
    @ApiModelProperty(value = "原价",required = true)
    private Float originalPrice;

    /**
     * 咨询qq
     */
    private String qq;

    /**
     * 微信
     */
    private String wechat;

    /**
     * 电话
     */
    @ApiModelProperty(value = "电话",required = true)
    @NotEmpty(message = "电话不能为空")
    private String phone;

    /**
     * 有效期天数
     */
    private Integer validDays;
    /**
     * 课程发布id
     */
    private Long coursePubId;

    /**
     * 课程发布日期
     */
    private Date coursePubDate;
}
