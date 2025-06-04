package com.xuecheng.ucenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.execption.XueChengException;
import com.xuecheng.ucenter.mapper.XcUserMapper;
import com.xuecheng.ucenter.mapper.XcUserRoleMapper;
import com.xuecheng.ucenter.model.dto.FindPasswordDTO;
import com.xuecheng.ucenter.model.dto.RegisterUserDTO;
import com.xuecheng.ucenter.model.po.XcUser;
import com.xuecheng.ucenter.model.po.XcUserRole;
import com.xuecheng.ucenter.service.UserService;
import com.xuecheng.utils.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Slf4j
public class UserServiceImpl implements UserService {
    @Autowired
    XcUserMapper xcUserMapper;
    @Autowired
    XcUserRoleMapper xcUserRoleMapper;


    /**
     * 找回用户账户密码
     * @param findPasswordDTO
     */
    public void findUserAccountPassword(FindPasswordDTO findPasswordDTO){

        // 执行流程
        // 1、校验验证码，不一致则抛出异常
        String checkcodekey = findPasswordDTO.getCheckcodekey();
        String checkcode = findPasswordDTO.getCheckcode();
        //暂时不校验

        // 2、判断两次密码是否一致，不一致则抛出异常
        String password = findPasswordDTO.getPassword();
        String confirmpwd = findPasswordDTO.getConfirmpwd();
        if (!password.equals(confirmpwd)){
            XueChengException.cast("两次输入的密码不匹配请重新输入");
        }
        // 3、根据手机号和邮箱查询用户
        LambdaQueryWrapper<XcUser> qw = new LambdaQueryWrapper<>();
        qw.eq(XcUser::getCellphone,findPasswordDTO.getCellphone())
                .or()
                .eq(StringUtil.isNotEmpty(findPasswordDTO.getEmail()),XcUser::getEmail,findPasswordDTO.getEmail());
        XcUser xcUser = xcUserMapper.selectOne(qw);
        if (xcUser==null){
            XueChengException.cast("暂无使用该手机号或者该邮箱注册的账号。");
        }
        // 4、如果找到用户更新为新密码
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        String encode = bCryptPasswordEncoder.encode(findPasswordDTO.getPassword());
        xcUser.setPassword(encode);
        xcUserMapper.updateById(xcUser);
    }

    /**
     * 创建用户账户
     *
     * @param registerUserDTO
     */
    @Transactional
    @Override
    public void registerUserAccount(RegisterUserDTO registerUserDTO) {
        // 执行流程：
        // 1、校验验证码，如果不一致则抛出异常
        String checkcode = registerUserDTO.getCheckcode();
        String checkcodekey = registerUserDTO.getCheckcodekey();
        //因为需要发送手机验证码这里暂时直接不验证，直接通过

        // 2、校验两次密码是否一致，如果不一致则抛出异常
        String password = registerUserDTO.getPassword();
        String confirmpwd = registerUserDTO.getConfirmpwd();
        if (!password.equals(confirmpwd)) {
            XueChengException.cast("两次密码输入不一致");
        }
        // 3、校验用户是否存在，如果存在则抛出异常
        String username = registerUserDTO.getUsername();
        XcUser xcUser = xcUserMapper.selectOne(new LambdaQueryWrapper<XcUser>().eq(XcUser::getUsername, username));
        if (xcUser!=null) {
            XueChengException.cast("账号已经存在，无需重复注册");
        }
        LambdaQueryWrapper<XcUser> qw = new LambdaQueryWrapper<>();
        qw.eq(XcUser::getCellphone, registerUserDTO.getCellphone())
                .or()
                .eq(XcUser::getEmail,registerUserDTO.getEmail());
        XcUser xcUserTemp = xcUserMapper.selectOne(qw);
        if (xcUserTemp!=null) {
            XueChengException.cast("该手机号或者邮箱已经注册过帐号了，无法需重复注册");
        }
        // 4、向用户表、用户角色关系表添加数据。角色为学生角色。
        xcUser = new XcUser();
        BeanUtils.copyProperties(registerUserDTO, xcUser);
        //添加用户账户
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        String encodePassWord = bCryptPasswordEncoder.encode(password);
        xcUser.setPassword(encodePassWord);
        xcUser.setName("学成注册用户");
        //101001学生101003管理员101002机构
        xcUser.setUtype("101001");
        xcUser.setStatus("1");
        xcUser.setCreateTime(LocalDateTime.now());
        int insert = xcUserMapper.insert(xcUser);
        if (insert<=0){
            XueChengException.cast("注册失败，请重试！");
        }
        //添加用户角色
        XcUserRole xcUserRole = new XcUserRole();
        xcUserRole.setUserId(xcUser.getId());
        /* 17学生18老师20教学管理员6管理员8超级管理员 */
        xcUserRole.setRoleId("17");
        xcUserRole.setCreateTime(LocalDateTime.now());
        int insert1 = xcUserRoleMapper.insert(xcUserRole);
        if (insert1<=0){
            XueChengException.cast("注册失败，请重试！");
        }
    }
}
