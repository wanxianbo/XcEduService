package com.xuecheng.framework.advice;

import com.google.common.collect.ImmutableMap;
import com.xuecheng.framework.exception.CustomerException;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.framework.model.response.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@Slf4j
@ControllerAdvice//增强启动器
public class ExceptionCatch {

    private static ImmutableMap<Class<? extends Throwable>, ResultCode> EXCEPTION;

    protected static ImmutableMap.Builder<Class<? extends Throwable>, ResultCode> builder = ImmutableMap.builder();


    //捕获 CustomException异常
    @ExceptionHandler(CustomerException.class)
    @ResponseBody//返回json串
    public ResponseResult handlerRunTimeException(CustomerException e) {
        log.error("catch exception:{}", e.getResultCode().message());
        return new ResponseResult(e.getResultCode());
    }

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public ResponseResult handlerException(Exception e) {
        log.error("catch exception:{}", e.getMessage());
        if (EXCEPTION == null) {
            EXCEPTION = builder.build();
        }
        ResultCode resultCode = EXCEPTION.get(e.getClass());
        final ResponseResult responseResult;
        if (resultCode != null) {
            responseResult = new ResponseResult(resultCode);
        } else {
            responseResult = new ResponseResult(CommonCode.SERVER_ERROR);
        }
        return responseResult;
    }

    static {
        //在这里添加一些基础的异常类型判断
        builder.put(HttpMessageNotReadableException.class, CommonCode.INVALID_PARAM);
    }

}
