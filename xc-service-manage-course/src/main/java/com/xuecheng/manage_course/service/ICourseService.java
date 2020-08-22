package com.xuecheng.manage_course.service;

import com.xuecheng.framework.domain.course.*;
import com.xuecheng.framework.domain.course.ext.CourseInfo;
import com.xuecheng.framework.domain.course.ext.CoursePublishResult;
import com.xuecheng.framework.domain.course.ext.CourseView;
import com.xuecheng.framework.domain.course.ext.TeachplanNode;
import com.xuecheng.framework.domain.course.request.CourseListRequest;
import com.xuecheng.framework.domain.course.response.AddCourseResult;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.ResponseResult;

public interface ICourseService {
    TeachplanNode findTeachplanList(String courseId);

    ResponseResult addTeachplan(Teachplan teachplan);

    ResponseResult updateTeachplan(Teachplan teachplan);

    Teachplan findTeachplanById(String id);

    ResponseResult deleteTeachplan(String id);

    QueryResponseResult<CourseInfo> findCourseList(int page, int size, CourseListRequest courseListRequest);

    AddCourseResult addCourseBase(CourseBase courseBase);

    CourseBase getCoursebaseById(String courseId);

    ResponseResult updateCourseBase(String id, CourseBase courseBase);

    CourseMarket getCourseMarketById(String courseId);

    CourseMarket updateCourseMarket(String id, CourseMarket courseMarket);

    ResponseResult saveCoursePic(String courseId, String pic);

    CoursePic findCoursepic(String courseId);

    ResponseResult deleteCoursePic(String courseId);

    CourseView getCourseView(String id);

    CoursePublishResult preview(String id);

    CoursePublishResult publish(String id);

    ResponseResult saveMedia(TeachplanMedia teachplanMedia);
}
