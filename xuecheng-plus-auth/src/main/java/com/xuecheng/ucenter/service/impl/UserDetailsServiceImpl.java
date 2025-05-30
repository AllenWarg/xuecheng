package com.xuecheng.ucenter.service.impl;

import com.alibaba.fastjson.JSON;
import com.xuecheng.ucenter.mapper.XcMenuMapper;
import com.xuecheng.ucenter.mapper.XcUserMapper;
import com.xuecheng.ucenter.model.dto.AuthParamsDto;
import com.xuecheng.ucenter.model.dto.XcUserExt;
import com.xuecheng.ucenter.model.po.XcMenu;
import com.xuecheng.ucenter.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author gc
 * @Description 认证查询数据库账号信息
 * @DateTime: 2025/5/23 18:50
 **/
@Service
@Slf4j
public class UserDetailsServiceImpl implements UserDetailsService {
    @Autowired
    XcUserMapper xcUserMapper;
    @Autowired
    ApplicationContext applicationContext;
    @Autowired
    XcMenuMapper xcMenuMapper;

    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        AuthParamsDto authParamsDto = null;
        try {
            authParamsDto = JSON.parseObject(s, AuthParamsDto.class);
        }catch (RuntimeException e){
            log.error("解析用户认证信息失败！");
            e.printStackTrace();
        }
        /*String username=authParamsDto.getUsername();
        LambdaQueryWrapper<XcUser> qw = new LambdaQueryWrapper<>();
        qw.eq(XcUser::getUsername, username);
        XcUser xcUser = xcUserMapper.selectOne(qw);
        if (xcUser == null) {
            return null;
        }
        String password = xcUser.getPassword();
        //添加一个权限
        String[] authorities = {"test"};
        xcUser.setPassword(null);
         //扩展用户认证信息
        String xcUserJSon = JSON.toJSONString(xcUser);
        UserDetails userDetails = User.withUsername(xcUserJSon)
                .password(password)
                .authorities(authorities).build();
        */
        String authType = authParamsDto.getAuthType()+"_authService";
        AuthService authService = applicationContext.getBean(authType, AuthService.class);
        XcUserExt xcUserExt = authService.execute(authParamsDto);
        return getUserDetails(xcUserExt);
    }


    private UserDetails getUserDetails(XcUserExt xcUserExt){
        String password = xcUserExt.getPassword();
        xcUserExt.setPassword(null);
        ArrayList<String> permissions = new ArrayList<>();
        //查询用户的资源访问权限
        List<XcMenu> xcMenus = xcMenuMapper.selectPermissionByUserId(xcUserExt.getId());
        for (XcMenu xcMenu : xcMenus) {
            permissions.add(xcMenu.getCode());
        }
        String[] permissionsArray = permissions.toArray(new String[permissions.size()]);
        String xcUserExtJson = JSON.toJSONString(xcUserExt);
        UserDetails userDetails = User.withUsername(xcUserExtJson).password(password).authorities(permissionsArray).build();
        return userDetails;
    }

}
