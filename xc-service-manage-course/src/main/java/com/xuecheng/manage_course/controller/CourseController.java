package com.xuecheng.manage_course.controller;

import com.xuecheng.api.course.CourseControllerApi;
import com.xuecheng.framework.domain.course.*;
import com.xuecheng.framework.domain.course.ext.CourseInfo;
import com.xuecheng.framework.domain.course.ext.CoursePublishResult;
import com.xuecheng.framework.domain.course.ext.CourseView;
import com.xuecheng.framework.domain.course.ext.TeachplanNode;
import com.xuecheng.framework.domain.course.request.CourseListRequest;
import com.xuecheng.framework.domain.course.response.AddCourseResult;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_course.service.ICourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/course")
public class CourseController implements CourseControllerApi {

    @Autowired
    private ICourseService courseService;


    /**
     * 查询课程计划
     * @param courseId 课程id
     * @return
     */
    @Override
    @GetMapping("/teachplan/list/{courseId}")
    public TeachplanNode findTeachplanList(@PathVariable("courseId") String courseId) {
        return courseService.findTeachplanList(courseId);
    }

    /**
     * 通过id查询课程计划
     * @param id id
     * @return
     */
    @Override
    @GetMapping("/teachplan/{id}")
    public Teachplan findTeachplanById(@PathVariable("id") String id) {
        return courseService.findTeachplanById(id);
    }

    /**
     * 添加课程计划
     * @param teachplan json串
     * @return
     */
    @Override
    @PostMapping("/teachplan/add")
    public ResponseResult addTeachplan(@RequestBody Teachplan teachplan) {
        return courseService.addTeachplan(teachplan);
    }

    /**
     * 删除课程计划
     * @param id id
     * @return
     */
    @Override
    @DeleteMapping("/teachplan/delete/{id}")
    public ResponseResult deleteTeachplan(@PathVariable("id") String id) {
        return courseService.deleteTeachplan(id);
    }

    /**
     * 分页查询课程
     * @param page 当前页
     * @param size 每页显示条数
     * @param courseListRequest 查询条件
     * @return 课成集合
     */
    @Override
    @GetMapping("/coursebase/list/{page}/{size}")
    public QueryResponseResult<CourseInfo> findCourseList(@PathVariable("page") int page,
                                                          @PathVariable("size") int size,
                                                          CourseListRequest courseListRequest) {
        return courseService.findCourseList(page,size,courseListRequest);
    }

    /**
     * 修改课程计划
     * @param teachplan json串
     * @return
     */
    @Override
    @PutMapping("/teachplan/update")
    public ResponseResult updateTeachplan(@RequestBody Teachplan teachplan) {
        return courseService.updateTeachplan(teachplan);
    }

    /**
     * 添加新课程
     * @param courseBase json串
     * @return
     */
    @Override
    @PostMapping("/coursebase/add")
    public AddCourseResult addCourseBase(@RequestBody CourseBase courseBase) {
        return courseService.addCourseBase(courseBase);
    }

    /**
     * 根据id查询课程的基本信息
     * @param courseId 课程id
     * @return
     */
    @Override
    @GetMapping("/coursebase/get/{courseId}")
    public CourseBase getCourseBaseById(@PathVariable("courseId") String courseId) {
        return courseService.getCoursebaseById(courseId);
    }

    /**
     * 修改课程
     * @param courseBase json串
     * @param id 课程id
     * @return
     */
    @Override
    @PutMapping("/coursebase/update/{id}")
    public ResponseResult updateCourseBase(@RequestBody CourseBase courseBase, @PathVariable("id") String id) {
        return courseService.updateCourseBase(id,courseBase);
    }

    /**
     * 根据课程id查询课程营销数据
     * @param courseId 课程id
     * @return
     */
    @Override
    @GetMapping("/coursemarket/get/{courseId}")
    public CourseMarket getCourseMarketById(@PathVariable("courseId") String courseId) {
        return courseService.getCourseMarketById(courseId);
    }

    /**
     * 更新或添加课程营销数据
     *
     * @param id           课程id 主键
     * @param courseMarket json
     * @return
     */
    @Override
    @PostMapping("/coursemarket/update/{id}")
    public ResponseResult updateCourseMarket(@PathVariable("id") String id, @RequestBody CourseMarket courseMarket) {
        CourseMarket updateCourseMarket = courseService.updateCourseMarket(id, courseMarket);
        if (updateCourseMarket == null) {
            return new ResponseResult(CommonCode.FAIL);
        }else
            return new ResponseResult(CommonCode.SUCCESS);
    }

    /**
     * 保存图片
     * @param courseId 课程id
     * @param pic 图片id
     * @return
     */
    @Override
    @PostMapping("/coursepic/add")
    public ResponseResult addCoursePic(@RequestParam("courseId") String courseId, @RequestParam("pic") String pic) {
        return courseService.saveCoursePic(courseId, pic);
    }

    /**
     * 查询图片
     * @param courseId 课程id
     * @return
     */
    @Override
    @PreAuthorize("hasAuthority('course_find_pic')")
    @GetMapping("/coursepic/list/{courseId}")
    public CoursePic findCoursePic(@PathVariable("courseId") String courseId) {
        return courseService.findCoursepic(courseId);
    }

    /**
     * 删除图片
     * @param courseId 课程id
     * @return
     */
    @Override
    @DeleteMapping("/coursepic/delete")
    public ResponseResult deleteCoursePic(@RequestParam("courseId") String courseId) {
        return courseService.deleteCoursePic(courseId);
    }

    /**
     * 课程视图数据查询
     * @param id 课程id
     * @return
     */
    @Override
    @GetMapping("/courseview/{id}")
    public CourseView courseview(@PathVariable("id") String id) {
        return courseService.getCourseView(id);
    }

    /**
     * 预览课程
     * @param id
     * @return
     */
    @Override
    @PostMapping("/preview/{id}")
    public CoursePublishResult preview(@PathVariable("id") String id) {
        return courseService.preview(id);
    }

    @Override
    @PostMapping("/publish/{id}")
    public CoursePublishResult publish(@PathVariable String id) {
        return courseService.publish(id);
    }

    /**
     * 课程计划保存视频信息
     * @param teachplanMedia 保存的对象
     * @return
     */
    @Override
    @PostMapping("/savemedia")
    public ResponseResult saveMedia(@RequestBody TeachplanMedia teachplanMedia) {
        return courseService.saveMedia(teachplanMedia);
    }

}
