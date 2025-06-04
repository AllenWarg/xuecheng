package com.xuecheng.ucenter.model.dto;

import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
public class RegisterUserDTO {
    @NotEmpty(message = "手机号不能为空")
    private String cellphone;
    @NotEmpty(message = "账号不能为空")
    private String username;
    @NotEmpty(message = "邮箱不能为空")
    private String email;
    @NotEmpty(message = "昵称不能为空")
    private String nickname;
    @NotEmpty(message = "密码不能为空")
    private String password;
    @NotEmpty(message = "确认密码不能为空")
    private String confirmpwd;
    private String checkcodekey;
    private String checkcode;
}
