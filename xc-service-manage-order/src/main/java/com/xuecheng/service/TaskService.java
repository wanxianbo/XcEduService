package com.xuecheng.service;

import com.xuecheng.framework.domain.task.XcTask;

import java.util.Date;
import java.util.List;

public interface TaskService {
    List<XcTask> findTaskList(Date updateTime, int size);

    void publish(XcTask xcTask, String ex, String routingKey);

    int getTask(String id, int version);

    void finishTask(String taskId);
}
