package com.xuecheng.ucenter.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.execption.XueChengException;
import com.xuecheng.ucenter.mapper.XcUserMapper;
import com.xuecheng.ucenter.mapper.XcUserRoleMapper;
import com.xuecheng.ucenter.model.dto.AuthParamsDto;
import com.xuecheng.ucenter.model.dto.XcUserExt;
import com.xuecheng.ucenter.model.po.XcUser;
import com.xuecheng.ucenter.model.po.XcUserRole;
import com.xuecheng.ucenter.service.AuthService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service("wx_authService")
public class WeiXInAuthServiceImpl implements AuthService {
    @Autowired
    RestTemplate restTemplate;
    @Autowired
    XcUserMapper xcUserMapper;
    @Autowired
    XcUserRoleMapper xcUserRoleMapper;

    @Override
    public XcUserExt execute(AuthParamsDto authParamsDto) {
        String username = authParamsDto.getUsername();
        XcUser xcUser = getUserByUsername(username);
        if(xcUser==null){
            XueChengException.cast("用户不存在，请重新扫码");
        }
        XcUserExt xcUserExt = new XcUserExt();
        BeanUtils.copyProperties(xcUser,xcUserExt);
        return xcUserExt;
    }

    /**
     * 根据用户名获取用户信息
     * @param username
     * @return
     */
    private XcUser getUserByUsername(String username){
        XcUser xcUser = xcUserMapper.selectOne(new LambdaQueryWrapper<XcUser>().eq(XcUser::getUsername, username));
        return xcUser;
    }

    public Map<String,String> getWxUserInfo(String code){

        String appid="wxf98fdeafea26d968";
        String appsecret= "a70309232e14fc52c6473c01ee8cb3ed";
        //得到token
        Map<String, String> wxAccessToken = getWxAccessToken(appid,appsecret,code);
        String accessToken = wxAccessToken.get("access_token");
        String openid = wxAccessToken.get("openid");
        String unionid=wxAccessToken.get("unionid");
        //获取微信用户信息
        Map<String, String> wxUserInfo = getWxUserInfo(accessToken, openid);
        return wxUserInfo;
    }

    /**
     * 将获取到的微信用户信息添加到数据库中
     * @param wxUserInfo 用户信息
     * @return
     */
    @Transactional
    public XcUser saveWxUser(Map<String,String> wxUserInfo){

        String nickname = wxUserInfo.get("nickname");
        String unionid = wxUserInfo.get("unionid");
        String headimgurl = wxUserInfo.get("headimgurl");
        String openid = wxUserInfo.get("openid");
        //获取数据库中的用户
        XcUser xcUser = getUserByUsername(openid);
        if (xcUser!=null){
            return xcUser;
        }else {
            xcUser=new XcUser();
        }
        xcUser.setId(UUID.randomUUID().toString());
        xcUser.setUsername(openid);
        xcUser.setNickname(nickname);
        xcUser.setWxUnionid(unionid);
        xcUser.setName(nickname);
        xcUser.setUtype("101001");
        xcUser.setStatus("1");
        xcUser.setCreateTime(LocalDateTime.now());
        xcUser.setPassword("123456");
        int insert = xcUserMapper.insert(xcUser);
        if (insert<=0){
            XueChengException.cast("扫码登录失败！");
        }
        XcUserRole xcUserRole = new XcUserRole();
        xcUserRole.setUserId(UUID.randomUUID().toString());
        xcUserRole.setId(xcUser.getId());
        xcUserRole.setRoleId("17");
        xcUserRole.setCreateTime(LocalDateTime.now());
        int insert1 = xcUserRoleMapper.insert(xcUserRole);
        if (insert1<=0){
            XueChengException.cast("扫码登录失败！");
        }
        return xcUser;
    }



    private Map<String,String> getWxAccessToken(String appid,String secret,String code){
        String urlTemplate="https://api.weixin.qq.com/sns/oauth2/access_token?appid=%s&secret=%s&code=%s&grant_type=authorization_code";
        String url = String.format(urlTemplate, appid, secret, code);
        ResponseEntity<String> exchange = restTemplate.exchange(url, HttpMethod.POST, null, String.class);
        String body = exchange.getBody();
        Map<String,String> mapResult = JSON.parseObject(body, Map.class);
        return mapResult;
    }

    private Map<String,String> getWxUserInfo(String access_token,String openid){
        String urlTemplate="https://api.weixin.qq.com/sns/userinfo?access_token=%s&openid=%s&lang=zh_CN";
        String url = String.format(urlTemplate, access_token, openid);
        ResponseEntity<String> exchange = restTemplate.exchange(url, HttpMethod.GET, null, String.class);
        String body = new String(exchange.getBody().getBytes(StandardCharsets.ISO_8859_1),StandardCharsets.UTF_8);
        Map<String,String> mapResult = JSON.parseObject(body, Map.class);
        return mapResult;
    }
}