package com.xuecheng.framework.domain.course.ext;

import com.xuecheng.framework.domain.course.CourseBase;
import com.xuecheng.framework.domain.course.CourseMarket;
import com.xuecheng.framework.domain.course.CoursePic;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@ToString
public class CourseView {
    private CourseBase courseBase; //课程基础信息
    private CoursePic coursePic; //课程营销
    private CourseMarket courseMarket; //课程图片
    private TeachplanNode teachplanNode; //教学计划
}
