package com.xuecheng.framework.domain.learning.response;

import com.xuecheng.framework.model.response.ResultCode;

public enum  LearningCode implements ResultCode {


    LEARNING_GETMEDIA_ERROR(false,23001,"！获取学习地址失败！"),
    CHOOSECOURSE_USERISNULL(false,23002,"选课的用户id为空"),
    CHOOSECOURSE_TASKISNULL(false,23003,"选课的任务对象为空");
    //操作代码
    boolean success;
    //操作代码
    int code;
    //提示信息
    String message;
    LearningCode(boolean success, int code, String message){
        this.success = success;
        this.code = code;
        this.message = message;
    }



    @Override
    public boolean success() {
        return false;
    }

    @Override
    public int code() {
        return 0;
    }

    @Override
    public String message() {
        return null;
    }
}
