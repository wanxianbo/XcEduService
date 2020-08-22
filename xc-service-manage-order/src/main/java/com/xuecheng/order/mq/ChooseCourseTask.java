package com.xuecheng.order.mq;

import com.xuecheng.framework.domain.task.XcTask;
import com.xuecheng.order.config.RabbitMQConfig;
import com.xuecheng.service.TaskService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;


@Component
@Slf4j
public class ChooseCourseTask {

    @Autowired
    private TaskService taskService;
    //每隔1分钟扫描消息表，向mq发送消息
    @Scheduled(cron = "0/3 * * * * *")//测试用3秒，实际用一分钟
    //@Scheduled(cron = "* 0/1 * * * *")//测试用3秒，实际用一分钟
    public void sendChoosecourseTask() {
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(new Date());
        calendar.add(GregorianCalendar.MINUTE,-1);
        Date updateTime = calendar.getTime();
        List<XcTask> taskList = taskService.findTaskList(updateTime, 100);
        for (XcTask xcTask : taskList) {
            //使用乐观锁解决集群部署时消息的重复发送
            if (taskService.getTask(xcTask.getId(), xcTask.getVersion()) > 0) {
                //发送选课消息
                taskService.publish(xcTask, xcTask.getMqExchange(), xcTask.getMqRoutingkey());
                log.info("send choose course task id:{}",xcTask.getId());
            }
        }
    }

    @RabbitListener(queues = RabbitMQConfig.XC_LEARNING_FINISHADDCHOOSECOURSE)
    public void receiveFinishChoosecourseTask(XcTask xcTask) {
        log.info("receiveChoosecourseTask...{}",xcTask.getId());
        if (xcTask != null && StringUtils.isNotBlank(xcTask.getId())) {
            //添加历史任务
            //删除任务
            taskService.finishTask(xcTask.getId());
        }
    }
}
