package com.xuecheng.ucenter.service;

import com.xuecheng.ucenter.model.dto.AuthParamsDto;
import com.xuecheng.ucenter.model.dto.XcUserExt;

/**
 * 统一认证接口
 */
public interface AuthService {
    //执行方法
    public XcUserExt execute(AuthParamsDto authParamsDto);
}
