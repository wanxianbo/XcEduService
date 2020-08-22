package com.xuecheng.learning.service;

import com.xuecheng.framework.domain.learning.response.GetMediaResult;
import com.xuecheng.framework.domain.task.XcTask;
import com.xuecheng.framework.model.response.ResponseResult;

import java.util.Date;

public interface ILearningService {
    GetMediaResult getmedia(String courseId, String teachplanId);

    ResponseResult addcourse(String userId, String courseId, String valid, String charge,
                             Float price, Date startTime, Date endTime, XcTask xcTask);
}
