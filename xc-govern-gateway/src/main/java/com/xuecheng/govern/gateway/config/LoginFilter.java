package com.xuecheng.govern.gateway.config;

import com.alibaba.fastjson.JSON;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.govern.gateway.service.ZuulService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class LoginFilter extends ZuulFilter {
    @Autowired
    private ZuulService zuulService;

    //过滤器的类型
    @Override
    public String filterType() {
        return "pre";
    }
    //过滤器执行顺序
    @Override
    public int filterOrder() {
        //int值来定义过滤器的执行顺序，数值越小优先级越高
        return 0;
    }
    //过滤器是否启用
    @Override
    public boolean shouldFilter() {
        // 该过滤器需要执行
        return true;
    }
    //进行拦截
    @Override
    public Object run() throws ZuulException {
        //通过网关需要先登录
        //1.判断是否存在cookie
        String jwt = zuulService.getCookie("uid");
       /* jwt = "eec80a5f-7adc-4401-ac22-f99264c0cdbf";*/
        if (StringUtils.isBlank(jwt)){
            this.refuseVisit();
            return null;
        }
        //2.判断请求头中的令牌
        String token = zuulService.getjwt();
        if (StringUtils.isBlank(token)){
            this.refuseVisit();
            return null;
        }
        //3.redis中是否存在
        Boolean redis = zuulService.getRedis(jwt);
        if (!redis){
            this.refuseVisit();
            return null;
        }
        return null;
    }
    //拒绝访问
    public void refuseVisit(){
        RequestContext requestContext = RequestContext.getCurrentContext();
        HttpServletResponse response = requestContext.getResponse();
        HttpServletRequest request = requestContext.getRequest();
        requestContext.setSendZuulResponse(false);// 拒绝访问
        requestContext.setResponseStatusCode(200);// 设置响应状态码
        ResponseResult unauthenticated = new ResponseResult(CommonCode.UNAUTHENTICATED);
        String jsonString = JSON.toJSONString(unauthenticated);
        requestContext.setResponseBody(jsonString);
        requestContext.getResponse().setContentType("application/json;charset=UTF‐8");
    }
}
