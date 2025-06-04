package com.xuecheng.ucenter.controller;

import com.xuecheng.ucenter.model.dto.FindPasswordDTO;
import com.xuecheng.ucenter.model.dto.RegisterUserDTO;
import com.xuecheng.ucenter.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Mr.M
 * @version 1.0
 * @description 用户账号相关操作
 * @date 2022/9/27 17:25
 */
@Slf4j
@Api("用户账号相关操作")
@RestController
public class UserController {
    @Autowired
    UserService userService;
    /**
     * 找回密码
     * @param findPasswordDTO
     */
    @ApiOperation("找回密码")
    @PostMapping("/findpassword")
    public void findPassword(@RequestBody FindPasswordDTO findPasswordDTO){
        userService.findUserAccountPassword(findPasswordDTO);
    }

    /**
     * 用户注册
     * @param registerUserDTO
     */
    @ApiOperation("用户注册")
    @RequestMapping("/register")
    public void registerUser(@RequestBody RegisterUserDTO registerUserDTO){
        userService.registerUserAccount(registerUserDTO);
    }




}
