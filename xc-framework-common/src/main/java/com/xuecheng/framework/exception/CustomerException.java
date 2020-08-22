package com.xuecheng.framework.exception;

import com.xuecheng.framework.model.response.ResultCode;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class CustomerException extends RuntimeException {
    private ResultCode resultCode;

    public ResultCode getResultCode() {
        return this.resultCode;
    }
}
