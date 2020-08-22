package com.xuecheng.learning.service.impl;

import com.xuecheng.framework.domain.course.TeachplanMediaPub;
import com.xuecheng.framework.domain.learning.XcLearningCourse;
import com.xuecheng.framework.domain.learning.response.GetMediaResult;
import com.xuecheng.framework.domain.learning.response.LearningCode;
import com.xuecheng.framework.domain.task.XcTask;
import com.xuecheng.framework.domain.task.XcTaskHis;
import com.xuecheng.framework.exception.CustomerException;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.learning.client.CourseSearchClient;
import com.xuecheng.learning.dao.XcLearningCourseRepository;
import com.xuecheng.learning.dao.XcTaskHisRepository;
import com.xuecheng.learning.service.ILearningService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;

@Service
public class LearningService implements ILearningService {


    @Autowired
    private CourseSearchClient courseSearchClient;
    @Autowired
    private XcLearningCourseRepository xcLearningCourseRepository;
    @Autowired
    private XcTaskHisRepository xcTaskHisRepository;

    /**
     * 获取课程学习地址
     *
     * @param courseId    课程id
     * @param teachplanId 课程计划id
     * @return 响应结果
     */
    @Override
    public GetMediaResult getmedia(String courseId, String teachplanId) {
        //校验学生的学习权限是否资费等

        //调用搜索服务查询
        if (StringUtils.isBlank(teachplanId)) {
            throw new CustomerException(CommonCode.INVALID_PARAM);
        }
        TeachplanMediaPub teachplanMediaPub = courseSearchClient.getmedia(teachplanId);
        if (teachplanMediaPub == null || StringUtils.isBlank(teachplanMediaPub.getMediaUrl())) {
            throw new CustomerException(LearningCode.LEARNING_GETMEDIA_ERROR);
        }
        //获取视频播放地址出错
        return new GetMediaResult(CommonCode.SUCCESS, teachplanMediaPub.getMediaUrl());
    }

    /**
     * 完成选课
     *
     * @param userId    用户id
     * @param courseId  课程id
     * @param valid     有效性
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @param xcTask    任务对象
     * @return 响应结果
     */
    @Override
    public ResponseResult addcourse(String userId, String courseId, String valid,String charge,
                                    Float price, Date startTime, Date endTime, XcTask xcTask) {
        //判断参数
        if (StringUtils.isBlank(courseId)) {
            throw new CustomerException(LearningCode.LEARNING_GETMEDIA_ERROR);
        }
        if (StringUtils.isBlank(userId)) {
            throw new CustomerException(LearningCode.CHOOSECOURSE_USERISNULL);
        }
        if(xcTask == null || StringUtils.isBlank(xcTask.getId())){
            throw new CustomerException(LearningCode.CHOOSECOURSE_TASKISNULL);
        }
        //先查询历史任务是否有，有就不添加课程
//        Optional<XcTaskHis> optional = xcTaskHisRepository.findById(xcTask.getId());
//        if(optional.isPresent()){
//            return new ResponseResult(CommonCode.SUCCESS);
//        }
        //查询选课
        XcLearningCourse xcLearningCourse = xcLearningCourseRepository.findByUserIdAndCourseId(userId, courseId);
        if (xcLearningCourse == null) {
            //添加课程
            xcLearningCourse = new XcLearningCourse();
            xcLearningCourse.setUserId(userId);
            xcLearningCourse.setCourseId(courseId);
            xcLearningCourse.setValid(valid);
            xcLearningCourse.setStartTime(startTime);
            xcLearningCourse.setCharge(charge);
            xcLearningCourse.setPrice(price);
            xcLearningCourse.setEndTime(endTime);
            xcLearningCourse.setStatus("501001");
            xcLearningCourseRepository.save(xcLearningCourse);
        } else {
            //更新课程
            xcLearningCourse.setValid(valid);
            xcLearningCourse.setStartTime(startTime);
            xcLearningCourse.setCharge(charge);
            xcLearningCourse.setPrice(price);
            xcLearningCourse.setEndTime(endTime);
            xcLearningCourse.setStatus("501001");
            xcLearningCourseRepository.save(xcLearningCourse);
        }
        //添加历史任务表
        Optional<XcTaskHis> optional = xcTaskHisRepository.findById(xcTask.getId());
        if (!optional.isPresent()) {
            XcTaskHis xcTaskHis = new XcTaskHis();
            BeanUtils.copyProperties(xcTask, xcTaskHis);
            xcTaskHisRepository.save(xcTaskHis);
        }
        return new ResponseResult(CommonCode.SUCCESS);
    }

}
