package com.xuecheng.ucenter.service;

import com.xuecheng.ucenter.model.dto.FindPasswordDTO;
import com.xuecheng.ucenter.model.dto.RegisterUserDTO;

/**
 * 用户账号相关操作
 */
public interface UserService {


    /**
     * 创建用户账户
     * @param registerUserDTO
     */
    void registerUserAccount(RegisterUserDTO registerUserDTO);

    /**
     * 找回用户账户密码
     * @param findPasswordDTO
     */
    void findUserAccountPassword(FindPasswordDTO findPasswordDTO);
}
