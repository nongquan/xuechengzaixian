package com.xuecheng.api.auth;

import com.xuecheng.framework.domain.ucenter.ext.AuthResult;
import com.xuecheng.framework.domain.ucenter.request.LoginRequest;
import com.xuecheng.framework.domain.ucenter.response.JwtResult;
import com.xuecheng.framework.model.response.ResponseResult;

public interface AuthControllerApi {

    //用户登录
    public AuthResult getToken(LoginRequest loginRequest);
    //登录成功首页显示用户名
    public JwtResult getRedis();
    //退出登录
    public ResponseResult userlogout();
}
