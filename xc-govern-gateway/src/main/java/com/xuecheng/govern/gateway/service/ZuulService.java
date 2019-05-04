package com.xuecheng.govern.gateway.service;

import com.netflix.zuul.context.RequestContext;
import com.xuecheng.framework.utils.CookieUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class ZuulService {
    @Autowired
    StringRedisTemplate stringRedisTemplate;

    //1.判断是否存在cookie
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
    //2.判断请求头中的令牌
    public String getjwt(){
        RequestContext requestContext = RequestContext.getCurrentContext();
        HttpServletRequest request = requestContext.getRequest();
        //取出头部信息Authorization
        String authorization = request.getHeader("Authorization");
        return authorization;
    }
    //3.redis中是否存在
    public Boolean getRedis(String jwt){
        String jwtToken = stringRedisTemplate.opsForValue().get("token:" + jwt);
        Long expire = stringRedisTemplate.getExpire("token:" + jwt);
        if (StringUtils.isNotBlank(jwtToken)&& expire>0){
            return true;
        }
        return false;
    }
}
