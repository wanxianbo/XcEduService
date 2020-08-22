package com.xuecheng.auth.controller;

import com.xuecheng.api.auth.AuthControllerApi;
import com.xuecheng.auth.config.AuthInfoConfig;
import com.xuecheng.auth.service.AuthService;
import com.xuecheng.framework.domain.ucenter.ext.AuthToken;
import com.xuecheng.framework.domain.ucenter.request.LoginRequest;
import com.xuecheng.framework.domain.ucenter.response.AuthCode;
import com.xuecheng.framework.domain.ucenter.response.JwtResult;
import com.xuecheng.framework.domain.ucenter.response.LoginResult;
import com.xuecheng.framework.exception.CustomerException;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.framework.utils.CookieUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@RestController
@RequestMapping("/")
@EnableConfigurationProperties(AuthInfoConfig.class)
public class AuthController implements AuthControllerApi {

    @Autowired
    private AuthService authService;
    @Autowired
    private AuthInfoConfig prop;

    //登录
    @Override
    @PostMapping("/userlogin")
    public LoginResult login(LoginRequest loginRequest, HttpServletResponse response) {
        //判断是否输入账号
        if (loginRequest == null || StringUtils.isBlank(loginRequest.getUsername())) {
            throw new CustomerException(AuthCode.AUTH_PASSWORD_NONE);
        }
        //判断是否输入密码
        if (StringUtils.isBlank(loginRequest.getPassword())) {
            throw new CustomerException(AuthCode.AUTH_PASSWORD_NONE);
        }
        AuthToken authToken = authService.login(loginRequest.getUsername(), loginRequest.getPassword(), prop.getClientId(), prop.getClientSecret());
        //访问token 身份令牌
        String access_token = authToken.getAccess_token();
        //将令牌写入cookie
        saveCookie(access_token,response);
        //将访问令牌存储到cookie
        return new LoginResult(CommonCode.SUCCESS, access_token);
    }

    @Override
    @PostMapping("/userlogout")
    public ResponseResult logout(HttpServletRequest request,HttpServletResponse response) {
        //删除redis中的token
        String access_token = getTokenFormCookie(request);
        authService.delToken(access_token);
        //删除cookie中的token
        clearCookie(access_token,response);
        return new ResponseResult(CommonCode.SUCCESS);
    }

    //从redis查询用户jwt令牌

    @Override
    @GetMapping("/userjwt")
    public JwtResult userjwt(HttpServletRequest request) {
        //从cookie中获取身份令牌
        String access_token = getTokenFormCookie(request);
        //redis中key 为 user_token:
        //根据key获取value，令牌
        AuthToken authToken = authService.getUserToken(access_token);
        if(authToken == null){
            return new JwtResult(CommonCode.FAIL,null);
        }
        return new JwtResult(CommonCode.SUCCESS,authToken.getJwt_token());
    }

    private void saveCookie(String access_token, HttpServletResponse response) {
        CookieUtil.addCookie(response, prop.getCookieDomain(), "/", "uid", access_token, prop.getCookieMaxAge(), false);
    }
    //推出登录

    private String getTokenFormCookie(HttpServletRequest request) {
        Map<String, String> map = CookieUtil.readCookie(request, "uid");
        String access_token = map.get("uid");
        return access_token;
    }
    //清除cookie
    private void clearCookie(String token,HttpServletResponse response){
        CookieUtil.addCookie(response, prop.getCookieDomain(), "/", "uid", token, 0, false);
    }

}
