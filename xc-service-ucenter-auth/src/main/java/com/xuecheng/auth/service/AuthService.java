package com.xuecheng.auth.service;

import com.alibaba.fastjson.JSON;
import com.xuecheng.framework.domain.ucenter.ext.AuthToken;
import com.xuecheng.framework.domain.ucenter.response.AuthCode;
import com.xuecheng.framework.exception.CustomerException;
import lombok.extern.slf4j.Slf4j;
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
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class AuthService {

    @Value("${auth.tokenValiditySeconds}")
    private int tokenValiditySeconds;

    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private LoadBalancerClient loadBalancerClient;

    /**
     * 认证登录
     *
     * @param username     用户输入的账号
     * @param password     用户输入的密码
     * @param clientId     客服端id
     * @param clientSecret 客服端密码
     * @return
     */
    public AuthToken login(String username, String password, String clientId, String clientSecret) {
        //申请令牌
        AuthToken authToken = applyToken(username, password, clientId, clientSecret);
        if (authToken == null) {
            throw new CustomerException(AuthCode.AUTH_LOGIN_AUTHSERVER_NOTFOUND);
        }
        // 将token存储到redis
        //key
        String access_token = authToken.getAccess_token();
        //value
        String content = JSON.toJSONString(authToken);
        boolean result = saveToken(access_token, content, tokenValiditySeconds);
        if (!result) {
            throw new CustomerException(AuthCode.AUTH_LOGIN_TOKEN_SAVEFAIL);
        }
        return authToken;
    }

    private boolean saveToken(String access_token, String content, int tokenValiditySeconds) {
        String name = "user_token:" + access_token;
        redisTemplate.boundValueOps(name).set(content, tokenValiditySeconds, TimeUnit.SECONDS);
        Long expire = redisTemplate.getExpire(name, TimeUnit.SECONDS);
        if (expire == null) {
            return false;
        }
        return expire > 0;
    }

    //申请令牌
    private AuthToken applyToken(String username, String password, String clientId, String clientSecret) {
        //选中认证服务的地址 xc-service-ucenter-auth
        ServiceInstance serviceInstance = loadBalancerClient.choose("xc-service-ucenter-auth");
        if (serviceInstance == null) {
            throw new CustomerException(AuthCode.AUTH_LOGIN_ERROR);
        }
        String path = serviceInstance.getUri().toString() + "/auth/oauth/token";
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "password");
        formData.add("username", username);
        formData.add("password", password);
        //定义头
        MultiValueMap<String, String> header = new LinkedMultiValueMap<>();
        header.add("Authorization", httpbasic(clientId, clientSecret));
        restTemplate.setErrorHandler(new DefaultResponseErrorHandler() {
            @Override
            public void handleError(URI url, HttpMethod method, ClientHttpResponse response) throws IOException {
                //当响应的值为400或401时候也要正常响应，不要抛出异常
                if (response.getRawStatusCode() != 400 && response.getRawStatusCode() != 401) {
                    super.handleError(response);
                }
            }
        });
        Map map = null;
        try {
            ResponseEntity<Map> mapResponseEntity = restTemplate.exchange(path, HttpMethod.POST, new HttpEntity<>(formData, header), Map.class);
            map = mapResponseEntity.getBody();
        } catch (RestClientException e) {
            //e.printStackTrace();
            log.error("request oauth_token_password error: {}", e.getMessage());
            throw new CustomerException(AuthCode.AUTH_LOGIN_APPLYTOKEN_FAIL);
        }
        if (CollectionUtils.isEmpty(map)
                || map.get("access_token") == null
                || map.get("refresh_token") == null
                || map.get("jti") == null) {
            //error=invalid_grant, error_description=用户名或密码错误 error=unauthorized, error_description=null
            //获取spring security返回的错误信息
            String error_description = (String) map.get("error_description");
            if (StringUtils.equals(error_description, "用户名或密码错误")) {
                throw new CustomerException(AuthCode.AUTH_CREDENTIAL_ERROR);
            } else if (StringUtils.isBlank(error_description)) {
                throw new CustomerException(AuthCode.AUTH_ACCOUNT_NOTEXISTS);
            }
            throw new CustomerException(AuthCode.AUTH_LOGIN_APPLYTOKEN_FAIL);
        }
        AuthToken authToken = new AuthToken();
        authToken.setJwt_token((String) map.get("access_token"));
        authToken.setRefresh_token((String) map.get("refresh_token"));
        authToken.setAccess_token((String) map.get("jti"));
        return authToken;
    }

    //获取httpbasic认证串
    private String httpbasic(String clientId, String clientSecret) {
        //将客户端id和客户端密码拼接，按“客户端id:客户端密码”
        String string = clientId + ":" + clientSecret;
        //进行base64编码
        byte[] encode = Base64Utils.encode(string.getBytes());
        return "Basic " + new String(encode);
    }

    //从redis查询jwt令牌
    public AuthToken getUserToken(String access_token) {
        String userToken = "user_token:" + access_token;
        String userTokenString = redisTemplate.boundValueOps(userToken).get();
        AuthToken authToken = null;
        if (StringUtils.isNotBlank(userTokenString)) {
            try {
                authToken = JSON.parseObject(userTokenString, AuthToken.class);
            } catch (Exception e) {
                log.error("getUserToken from redis and execute JSON.parseObject error {}", e.getMessage());
                e.printStackTrace();
            }
        }
        return authToken;
    }

    //再redis删除token

    public boolean delToken(String access_token) {
        String userToken = "user_token:" + access_token;
        try {
            redisTemplate.delete(userToken);
        } catch (Exception e) {
            log.error("delete token fail:{}",e.getMessage());
            e.printStackTrace();
        }
        return true;
    }
}
