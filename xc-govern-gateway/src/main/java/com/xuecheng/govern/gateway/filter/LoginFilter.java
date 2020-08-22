package com.xuecheng.govern.gateway.filter;

import com.alibaba.fastjson.JSON;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.govern.gateway.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
@Slf4j
public class LoginFilter extends ZuulFilter {

    @Autowired
    private AuthService authService;

    @Override
    public String filterType() {
        return FilterConstants.PRE_TYPE;
    }

    @Override
    public int filterOrder() {
        return FilterConstants.PRE_DECORATION_FILTER_ORDER - 1;
    }

    @Override
    public boolean shouldFilter() {
        return true;
    }

    @Override
    public Object run() throws ZuulException {
        //获取请求上下文对象
        RequestContext requestContext = RequestContext.getCurrentContext();
        //请求对象
        HttpServletRequest request = requestContext.getRequest();
        //从cookie查询身份令牌
        String access_token = authService.getTokenFromCookie(request);
        if (StringUtils.isBlank(access_token)) {
            access_denied(requestContext);
            return null;
        }
        //判断请求头是否有 Authorization
        String jwt = authService.getJwtFromHeader(request);
        if (StringUtils.isBlank(jwt)) {
            access_denied(requestContext);
            return null;
        }
        //从redis中校验身份令牌是否过期
        long expire = authService.getExpire(access_token);
        if (expire < 0) {
            access_denied(requestContext);
            return null;
        }
        return null;
    }

    private void access_denied(RequestContext requestContext) {
        //获取response对象
        HttpServletResponse response = requestContext.getResponse();
        //拒接访问
        requestContext.setSendZuulResponse(false);
        //设置响应信息
        ResponseResult responseResult =new ResponseResult(CommonCode.UNAUTHENTICATED);
        String responseResultString = JSON.toJSONString(responseResult);
        requestContext.setResponseBody(responseResultString);
        //设置状态码
        requestContext.setResponseStatusCode(200);
        response.setContentType("application/json;charset=utf-8");
    }
}
