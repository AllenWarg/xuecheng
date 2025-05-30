package com.xuecheng.ucenter.model.dto;

import lombok.Data;

@Data
public class FindPasswordDTO {
    String cellphone;
    String email;
    String checkcodekey;
    String checkcode;
    /**
     * 确认密码
     */
    String confirmpwd;
    String password;
}
