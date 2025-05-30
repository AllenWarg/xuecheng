package com.xuecheng.ucenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.execption.XueChengException;
import com.xuecheng.ucenter.feignClient.CheckCodeClient;
import com.xuecheng.ucenter.mapper.XcUserMapper;
import com.xuecheng.ucenter.model.dto.AuthParamsDto;
import com.xuecheng.ucenter.model.dto.XcUserExt;
import com.xuecheng.ucenter.model.po.XcUser;
import com.xuecheng.ucenter.service.AuthService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service("password_authService")
public class PasswordAuthServiceImpl implements AuthService {
    @Autowired
    XcUserMapper xcUserMapper;
    @Autowired
    CheckCodeClient checkCodeClient;
    /**
     * 密码认证
     * @param authParamsDto 用户认证传输模型
     * @return
     */
    @Override
    public XcUserExt execute(AuthParamsDto authParamsDto) {
        if (authParamsDto==null){
            return null ;
        }
        //验证码验证
        Boolean checkCodeResult = checkCode(authParamsDto.getCheckcodekey(),authParamsDto.getCheckcode());
        if (checkCodeResult==null||!checkCodeResult){
            XueChengException.cast("验证码错误");
        }
        String username = authParamsDto.getUsername();
        XcUser xcUser = xcUserMapper.selectOne(new LambdaQueryWrapper<XcUser>().eq(XcUser::getUsername, username));
        if (xcUser==null){
            XueChengException.cast("用户名不存");
        }
        String password = xcUser.getPassword();
        //加密密码进行比较
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        boolean matches = bCryptPasswordEncoder.matches(authParamsDto.getPassword(), password);
        if (!matches){
            XueChengException.cast("密码错误");
        }
        XcUserExt xcUserExt = new XcUserExt();
        BeanUtils.copyProperties(xcUser,xcUserExt);
        return xcUserExt;
    }

    private Boolean checkCode(String checkcodekey,String checkcode){
        return checkCodeClient.verify(checkcodekey, checkcode);
    }
}
