package com.xuecheng.manage_course.dao;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.xuecheng.framework.domain.course.CourseBase;
import com.xuecheng.framework.domain.course.ext.CourseInfo;
import com.xuecheng.framework.domain.course.request.CourseListRequest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.Optional;


@SpringBootTest
@RunWith(SpringRunner.class)
public class CourseMapperTest {

    @Autowired
    private CourseBaseRepository courseBaseRepository;
    @Autowired
    private CourseMapper courseMapper;

    @Test
    public void findCourseBaseById() {
        Optional<CourseBase> optional = courseBaseRepository.findById("297e7c7c62b888f00162b8a7dec20000");
        if (optional.isPresent()) {
            CourseBase courseBase = optional.get();
            System.out.println(courseBase);
        }
    }

    @Test
    public void findCourseBaseByIdMapper() {
        CourseBase courseBase = courseMapper.findCourseBaseById("297e7c7c62b888f00162b8a7dec20000");
        if (courseBase != null) {
            System.out.println(courseBase);
        }
    }

    //测试分页
    @Test
    public void testPageHelper(){
        PageHelper.startPage(2, 1);
        CourseListRequest courseListRequest = new CourseListRequest();
        List<CourseInfo> courseInfoList = courseMapper.findCourseListPage(courseListRequest);
        PageInfo<CourseInfo> pageInfo = PageInfo.of(courseInfoList);
        System.out.println(pageInfo.getTotal());
    }
}