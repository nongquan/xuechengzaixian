package com.xuecheng.auth.service;

import com.alibaba.fastjson.JSON;
import com.xuecheng.framework.client.XcServiceList;
import com.xuecheng.framework.domain.ucenter.ext.AuthToken;
import com.xuecheng.framework.domain.ucenter.response.AuthCode;
import com.xuecheng.framework.domain.ucenter.response.JwtResult;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.ResponseResult;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class AuthService {

    @Autowired
    LoadBalancerClient loadBalancerClient;
    @Autowired
    RestTemplate restTemplate;
    @Autowired
    StringRedisTemplate stringRedisTemplate;
    @Value("${auth.tokenValiditySeconds}")
    private  int redisTime;

    //用户登录
    public AuthToken getToken(String username, String password, String clientId, String clientSecret) {
        //远程调用获取令牌
        Map allToken = this.getAllToken(username, password, clientId, clientSecret);
        String access_token = (String) allToken.get("access_token");
        String refresh_token = (String) allToken.get("refresh_token");
        String jti = (String) allToken.get("jti");
        if (StringUtils.isNotBlank(access_token)&&StringUtils.isNotBlank(refresh_token)&&StringUtils.isNotBlank(jti)){

            String allTokenJson = JSON.toJSONString(allToken);
            //存储数据到Redis
            boolean b = this.saveRedis( "token:" + jti, allTokenJson, redisTime);
            if (!b){
                ExceptionCast.cast(CommonCode.FAIL);
            }

            AuthToken authToken = new AuthToken();
            authToken.setAccess_token(jti);
            authToken.setJwt_token(access_token);
            authToken.setRefresh_token(refresh_token);

            return authToken;
        }
        return  null;
    }
    //远程调用获取令牌
    public Map getAllToken(String username, String password, String clientId, String clientSecret){
        //从eureka中获取认证服务的地址（因为spring security在认证服务中）
        //从eureka中获取认证服务的一个实例的地址
        ServiceInstance serviceInstance = loadBalancerClient.choose(XcServiceList.XC_SERVICE_UCENTER_AUTH);
        //此地址就是http://ip:port
        URI uri = serviceInstance.getUri();
        //令牌申请的地址 http://localhost:40400/auth/oauth/token
        String authUrl = uri+ "/auth/oauth/token";
        //定义header
        LinkedMultiValueMap<String, String> header = new LinkedMultiValueMap<>();
        String httpBasic = getHttpBasic(clientId,clientSecret);
        header.add("Authorization",httpBasic);

        //定义body
        LinkedMultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type","password");
        body.add("username",username);
        body.add("password",password);

        HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(body, header);
        //String url, HttpMethod method, @Nullable HttpEntity<?> requestEntity, Class<T> responseType, Object... uriVariables
        //设置restTemplate远程调用时候，对400和401不让报错，正确返回数据
        restTemplate.setErrorHandler(new DefaultResponseErrorHandler(){
            @Override
            public void handleError(ClientHttpResponse response) throws IOException {
                if(response.getRawStatusCode()!=400 && response.getRawStatusCode()!=401){
                    super.handleError(response);
                }
            }
        });
        //申请令牌信息
        ResponseEntity<Map> exchange = restTemplate.exchange(authUrl, HttpMethod.POST, httpEntity, Map.class);
        Map bodyMap = exchange.getBody();
        if (bodyMap!=null&&bodyMap.get("access_token")==null){
            if (bodyMap.get("error").equals("unauthorized")){
                ExceptionCast.cast(AuthCode.AUTH_ACCOUNT_NOTEXISTS);
            }
            if(bodyMap.get("error").equals("invalid_grant")) {
                ExceptionCast.cast(AuthCode.AUTH_CREDENTIAL_ERROR);
            }
        }
        return bodyMap;
    }
    //获取httpbasic的串
    private String getHttpBasic(String clientId,String clientSecret){
        String string = clientId+":"+clientSecret;
        //将串进行base64编码
        byte[] encode = Base64Utils.encode(string.getBytes());
        return "Basic "+new String(encode);
    }
    //存储数据到Redis
    public boolean saveRedis(String key,String value,int time){
        //存储数据
        stringRedisTemplate.boundValueOps(key).set(value,time,TimeUnit.SECONDS);
        //判断是否存成功 用过期时间
        Long expire1 = stringRedisTemplate.getExpire(key);
        if (expire1>0){
            return true;
        }
        return false;
    }
    //登录成功首页显示用户名
    public JwtResult getRedis(String jwt) {
        String jwtToken = stringRedisTemplate.opsForValue().get("token:" + jwt);
        if (StringUtils.isBlank(jwtToken)){
            ExceptionCast.cast(CommonCode.INVALID_PARAM);
        }
        Map map = JSON.parseObject(jwtToken, Map.class);
        String access_token = (String) map.get("access_token");
        return new JwtResult(CommonCode.SUCCESS,access_token);
    }
    //退出登录
    public Boolean deleteRedis(String jwt) {
        //删除redis
        stringRedisTemplate.delete(jwt);
       //判断是否删除成功
        Long expire1 = stringRedisTemplate.getExpire(jwt);
        if (expire1<=0){
            return true;
        }
        return false;
    }

}
