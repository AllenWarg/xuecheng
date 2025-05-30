package com.xuecheng.ucenter.controller;

import com.xuecheng.ucenter.model.dto.FindPasswordDTO;
import com.xuecheng.ucenter.model.dto.RegisterUserDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Mr.M
 * @version 1.0
 * @description 用户账号相关操作
 * @date 2022/9/27 17:25
 */
@Slf4j
@RestController
@Api("用户账号相关操作")
public class UserController {

    /**
     * 找回密码
     * @param findPasswordDTO
     */
    @ApiOperation("找回密码")
    @PostMapping("/findpassword")
    public void findPassword(@RequestBody FindPasswordDTO findPasswordDTO){
        //TODO
        /*执行流程
        1、校验验证码，不一致则抛出异常
        2、判断两次密码是否一致，不一致则抛出异常
        3、根据手机号和邮箱查询用户
        4、如果找到用户更新为新密码*/

    }

    /**
     * 用户注册
     * @param registerUserDTO
     */
    @ApiOperation("用户注册")
    @PostMapping("/register")
    public void registerUser(@RequestBody RegisterUserDTO registerUserDTO){
        //TODO

        /*
        执行流程：
        1、校验验证码，如果不一致则抛出异常
        2、校验两次密码是否一致，如果不一致则抛出异常
        3、校验用户是否存在，如果存在则抛出异常
        4、向用户表、用户角色关系表添加数据。角色为学生角色。
        */
    }




}
