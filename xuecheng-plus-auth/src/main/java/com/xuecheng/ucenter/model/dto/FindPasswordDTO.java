package com.xuecheng.ucenter.model.dto;

import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
public class FindPasswordDTO {
    @NotEmpty(message = "手机号不能为空")
    String cellphone;
    String email;
    String checkcodekey;
    String checkcode;
    /**
     * 确认密码
     */
    @NotEmpty(message = "确认密码不能为空")
    String confirmpwd;
    @NotEmpty(message = "密码不能为空")
    String password;
}
