package com.xuecheng.service.impl;

import com.xuecheng.framework.domain.task.XcTask;
import com.xuecheng.framework.domain.task.XcTaskHis;
import com.xuecheng.order.dao.XcTaskHisRepository;
import com.xuecheng.order.dao.XcTaskRepository;
import com.xuecheng.service.TaskService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class TaskServiceImpl implements TaskService {
    @Autowired
    private XcTaskRepository xcTaskRepository;

    @Autowired
    private XcTaskHisRepository xcTaskHisRepository;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Override
    public List<XcTask> findTaskList(Date updateTime, int size) {
        Pageable pageable = PageRequest.of(0, size);
        Page<XcTask> page = xcTaskRepository.findByUpdateTimeBefore(pageable, updateTime);
        return page.getContent();
    }

    //发送消息
    @Transactional
    @Override
    public void publish(XcTask xcTask, String ex, String routingKey) {
        //先查询任务
        Optional<XcTask> optional = xcTaskRepository.findById(xcTask.getId());
        if (optional.isPresent()) {
            rabbitTemplate.convertAndSend(ex, routingKey, xcTask);
            //更新时间
            xcTask.setUpdateTime(new Date());
            xcTaskRepository.save(xcTask);
        }
    }

    @Transactional
    @Override
    public int getTask(String id, int version) {
        return xcTaskRepository.updateTaskVersion(id, version);
    }

    @Transactional
    @Override
    public void finishTask(String taskId) {
        //查询任务对象
        Optional<XcTask> optional = xcTaskRepository.findById(taskId);
        if (optional.isPresent()) {
            XcTask xcTask = optional.get();
            xcTask.setDeleteTime(new Date());
            XcTaskHis xcTaskHis = new XcTaskHis();
            BeanUtils.copyProperties(xcTask, xcTaskHis);
            //添加历史任务
            xcTaskHisRepository.save(xcTaskHis);
            //删除任务
            xcTaskRepository.delete(xcTask);
        }
    }
}
