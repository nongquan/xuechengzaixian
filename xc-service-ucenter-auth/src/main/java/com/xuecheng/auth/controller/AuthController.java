package com.xuecheng.auth.controller;

import com.xuecheng.api.auth.AuthControllerApi;
import com.xuecheng.auth.service.AuthService;
import com.xuecheng.framework.domain.ucenter.ext.AuthResult;
import com.xuecheng.framework.domain.ucenter.ext.AuthToken;
import com.xuecheng.framework.domain.ucenter.request.LoginRequest;
import com.xuecheng.framework.domain.ucenter.response.AuthCode;
import com.xuecheng.framework.domain.ucenter.response.JwtResult;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.framework.utils.CookieUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@RestController
@RequestMapping("/")
public class AuthController implements AuthControllerApi {

    @Autowired
    private AuthService authService;
    @Value("${auth.clientId}")
    private String clientId;
    @Value("${auth.clientSecret}")
    private String clientSecret;
    @Value("${auth.cookieDomain}")
    private String cookieDomain;
    @Value("${auth.cookieMaxAge}")
    private int cookieMaxAge;

    //用户登录

    @PostMapping("/userlogin")
    public AuthResult getToken(LoginRequest loginRequest) {
        //判断参数是否存在
        if (loginRequest==null|| StringUtils.isBlank(loginRequest.getUsername())||StringUtils.isBlank(loginRequest.getPassword())){
            ExceptionCast.cast(CommonCode.INVALID_PARAM);
        }
        //拿到令牌
        AuthToken authToken = authService.getToken(loginRequest.getUsername(),loginRequest.getPassword(),clientId,clientSecret);
        if (authToken==null){
            ExceptionCast.cast(AuthCode.AUTH_FALL_TOKEN);
        }
        //把令牌保存到Cookie
        String access_token = authToken.getAccess_token();
        String jwt_token = authToken.getJwt_token();
        this.saveCookis("uid",access_token,cookieMaxAge);

        /*return new AuthResult(CommonCode.SUCCESS,access_token);*/
        return new AuthResult(CommonCode.SUCCESS,jwt_token);
    }

    //把access_token存到cookie
    public void saveCookis(String cookie_key,String cookie_value,int exprieTime){
        //HttpServletResponse response,String domain域名,String path, String name,String value, int maxAge,boolean httpOnly
        //cookie时间设置-1 表示关闭浏览器才清除cookie  设置大于0 则是秒
        HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
        CookieUtil.addCookie(response,cookieDomain,"/",cookie_key,cookie_value,exprieTime,false);
    }

    //登录成功首页显示用户名
    @GetMapping("/userjwt")
    public JwtResult getRedis() {

        String jwt = this.getCookie("uid");
        if (StringUtils.isBlank(jwt)){
            return null;
        }
        return   authService.getRedis(jwt);
    }
    //退出登录
    @PostMapping("/userlogout")
    public ResponseResult userlogout(){
        //取出身份令牌
        String jwt = this.getCookie("uid");
        if (StringUtils.isBlank(jwt)){
            return null;
        }
        //删除redis
        Boolean aBoolean = authService.deleteRedis(jwt);
        if (!aBoolean){
            ExceptionCast.cast(CommonCode.SERVER_ERROR);
        }
        //删除cookie
        this.deleteCookie("uid");
        return new ResponseResult(CommonCode.SUCCESS);
    }
    //取出cookie
    public String getCookie(String key){
        //HttpServletRequest request,String ... cookieNames
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        Map<String, String> map = CookieUtil.readCookie(request, key);
        if (map!=null&&map.get(key)!=null){
            String jwt = map.get(key);
            return jwt;
        }
        return null;
    }
    //删除cookie
    public void deleteCookie(String key){
        HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
        CookieUtil.addCookie(response,cookieDomain,"/",key,null,0,false);
    }
}
