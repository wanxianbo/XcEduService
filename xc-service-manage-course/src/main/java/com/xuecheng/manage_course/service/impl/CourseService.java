package com.xuecheng.manage_course.service.impl;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.response.CmsPageResult;
import com.xuecheng.framework.domain.cms.response.CmsPostPageResult;
import com.xuecheng.framework.domain.course.*;
import com.xuecheng.framework.domain.course.ext.CourseInfo;
import com.xuecheng.framework.domain.course.ext.CoursePublishResult;
import com.xuecheng.framework.domain.course.ext.CourseView;
import com.xuecheng.framework.domain.course.ext.TeachplanNode;
import com.xuecheng.framework.domain.course.request.CourseListRequest;
import com.xuecheng.framework.domain.course.response.AddCourseResult;
import com.xuecheng.framework.domain.course.response.CourseCode;
import com.xuecheng.framework.domain.media.response.MediaCode;
import com.xuecheng.framework.exception.CustomerException;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_course.client.CmsPageClient;
import com.xuecheng.manage_course.config.CoursePublishProperties;
import com.xuecheng.manage_course.dao.*;
import com.xuecheng.manage_course.service.ICourseService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@EnableConfigurationProperties(CoursePublishProperties.class)
public class CourseService implements ICourseService {

    @Autowired
    private CoursePublishProperties prop;

    @Autowired
    private TeachplanMapper teachplanMapper;

    @Autowired
    private CourseBaseRepository courseBaseRepository;

    @Autowired
    private TeachplanRepository teachplanRepository;
    @Autowired
    private CourseMapper courseMapper;
    @Autowired
    private CourseMarketRepository courseMarketRepository;
    @Autowired
    private CoursePicRepository coursePicRepository;

    @Autowired
    private CmsPageClient cmsPageClient;
    @Autowired
    private CoursePubRepository coursePubRepository;
    @Autowired
    private TeachplanMediaRepository teachplanMediaRepository;
    @Autowired
    private TeachplanMediaPubRepository teachplanMediaPubRepository;

    @Override
    public TeachplanNode findTeachplanList(String courseId) {
        if (StringUtils.isBlank(courseId)) {
            throw new CustomerException(CommonCode.INVALID_PARAM);
        }
        TeachplanNode teachplanNode = teachplanMapper.selectList(courseId);
        if (teachplanNode == null) {
            throw new CustomerException(CourseCode.COURSE_TEACHPLANISNULL);
        }
        return teachplanMapper.selectList(courseId);
    }

    //添加课程计划
    @Override
    @Transactional
    public ResponseResult addTeachplan(Teachplan teachplan) {
        //检验参数
        if (teachplan == null ||
                StringUtils.isBlank(teachplan.getCourseid()) ||
                StringUtils.isBlank(teachplan.getPname())) {
            throw new CustomerException(CommonCode.INVALID_PARAM);
        }
        //取出课程id
        String courseid = teachplan.getCourseid();
        //取出parentid
        String parentid = teachplan.getParentid();
        //判断parentid是否为空
        if (StringUtils.isBlank(parentid)) {
            //为空，获取根节点（一级标题的id）
            parentid = getTeachplanRoot(courseid);
        }
        //取出父结点信息
        Optional<Teachplan> teachplanOptional = teachplanRepository.findById(parentid);
        if (!teachplanOptional.isPresent()) {
            throw new CustomerException(CommonCode.INVALID_PARAM);
        }
        //取出父节点
        Teachplan teachplanParent = teachplanOptional.get();
        //父节点级别
        String parentGrade = teachplanParent.getGrade();
        //子结点的级别，根据父结点来判断
        teachplan.setParentid(parentid);
        teachplan.setStatus("0");
        if (parentGrade.equals("1")) {
            teachplan.setGrade("2");
        } else {
            teachplan.setGrade("3");
        }
        //设置课程id
        teachplan.setCourseid(teachplanParent.getCourseid());
        teachplanRepository.save(teachplan);
        return new ResponseResult(CommonCode.SUCCESS);
    }

    private String getTeachplanRoot(String courseid) {
        //检验courseid
        Optional<CourseBase> optional = courseBaseRepository.findById(courseid);
        if (!optional.isPresent()) {
            throw new CustomerException(CourseCode.COURSE_PUBLISH_COURSEIDISNULL);
        }
        CourseBase courseBase = optional.get();
        //取出课程计划根节点
        List<Teachplan> teachplanList = teachplanRepository.findByCourseidAndParentid(courseid, "0");
        if (CollectionUtils.isEmpty(teachplanList)) {
            Teachplan teachplan = new Teachplan();
            teachplan.setCourseid(courseid);
            teachplan.setPname(courseBase.getName());
            teachplan.setGrade("1");
            teachplan.setParentid("0");
            teachplan.setStatus("0");// 未发布
            teachplanRepository.save(teachplan);
            return teachplan.getId();
        }
        return teachplanList.get(0).getId();
    }

    //修改课程计划
    @Override
    @Transactional
    public ResponseResult updateTeachplan(Teachplan teachplan) {
        //检验参数
        if (teachplan == null ||
                StringUtils.isBlank(teachplan.getCourseid()) ||
                StringUtils.isBlank(teachplan.getPname())) {
            throw new CustomerException(CommonCode.INVALID_PARAM);
        }
        String parentid = teachplan.getParentid();
        //取出父结点信息
        Optional<Teachplan> teachplanOptional = teachplanRepository.findById(parentid);
        if (!teachplanOptional.isPresent()) {
            throw new CustomerException(CommonCode.INVALID_PARAM);
        }
        //取出父节点
        Teachplan teachplanParent = teachplanOptional.get();
        //父节点级别
        String parentGrade = teachplanParent.getGrade();
        //子结点的级别，根据父结点来判断
        teachplan.setParentid(parentid);
        teachplan.setStatus("0");
        if (parentGrade.equals("1")) {
            teachplan.setGrade("2");
        } else {
            teachplan.setGrade("3");
        }
        //设置课程id
        teachplan.setCourseid(teachplanParent.getCourseid());
        teachplanRepository.save(teachplan);
        return new ResponseResult(CommonCode.SUCCESS);
    }

    //通过id查询课程计划
    @Override
    public Teachplan findTeachplanById(String id) {
        if (StringUtils.isBlank(id)) {
            throw new CustomerException(CommonCode.INVALID_PARAM);
        }
        Optional<Teachplan> optional = teachplanRepository.findById(id);
        if (!optional.isPresent()) {
            return null;
        }
        return optional.get();
    }

    //通过id删除课程计划
    @Override
    @Transactional
    public ResponseResult deleteTeachplan(String id) {
        if (StringUtils.isBlank(id)) {
            throw new CustomerException(CommonCode.INVALID_PARAM);
        }
        teachplanRepository.deleteById(id);
        return new ResponseResult(CommonCode.SUCCESS);
    }

    @Override
    public QueryResponseResult<CourseInfo> findCourseList(int page, int size, CourseListRequest courseListRequest) {
        if (courseListRequest == null) {
            courseListRequest = new CourseListRequest();
        }
        if (size <= 0) {
            size = 20;
        }
        //设置分页参数
        PageHelper.startPage(page, size);
        //分页查询
        List<CourseInfo> courseListPage = courseMapper.findCourseListPage(courseListRequest);
        if (CollectionUtils.isEmpty(courseListPage)) {
            throw new CustomerException(CourseCode.COURSE_PUBLISH_COURSEIDISNULL);
        }
        PageInfo<CourseInfo> pageInfo = PageInfo.of(courseListPage);
        QueryResult<CourseInfo> queryResult = new QueryResult<>();
        queryResult.setList(courseListPage);
        queryResult.setTotalPage(pageInfo.getPageSize());
        queryResult.setTotal(pageInfo.getTotal());
        return new QueryResponseResult<>(CommonCode.SUCCESS, queryResult);
    }

    /**
     * 添加课程
     *
     * @param courseBase 新课程
     * @return
     */
    @Override
    @Transactional
    public AddCourseResult addCourseBase(CourseBase courseBase) {
        courseBase.setStatus("202001");//未发布
        courseBaseRepository.save(courseBase);
        return new AddCourseResult(CommonCode.SUCCESS, courseBase.getId());
    }

    /**
     * 根据id查询课程
     *
     * @param courseId 课程id
     * @return
     */
    @Override
    public CourseBase getCoursebaseById(String courseId) {
        if (StringUtils.isBlank(courseId)) {
            throw new CustomerException(CommonCode.INVALID_PARAM);
        }
        Optional<CourseBase> optional = courseBaseRepository.findById(courseId);
        return optional.orElse(null);
    }

    /**
     * 更新课程
     *
     * @param id         主键
     * @param courseBase json串
     * @return
     */
    @Override
    @Transactional
    public ResponseResult updateCourseBase(String id, CourseBase courseBase) {
        if (StringUtils.isBlank(id)) {
            throw new CustomerException(CommonCode.INVALID_PARAM);
        }
        CourseBase one = this.getCoursebaseById(id);
        BeanUtils.copyProperties(courseBase, one);
        courseBaseRepository.save(one);
        return new ResponseResult(CommonCode.SUCCESS);
    }

    /**
     * 根据课程id查询课程营销数据
     *
     * @param courseId 课程id
     * @return 课程营销数据
     */
    @Override
    public CourseMarket getCourseMarketById(String courseId) {
        if (StringUtils.isBlank(courseId)) {
            throw new CustomerException(CommonCode.INVALID_PARAM);
        }
        Optional<CourseMarket> optional = courseMarketRepository.findById(courseId);
        return optional.orElse(null);
    }

    /**
     * 更新或添加课程营销数据
     *
     * @param id           主键 课程id
     * @param courseMarket json
     * @return 更新好的课程营销数据
     */
    @Override
    public CourseMarket updateCourseMarket(String id, CourseMarket courseMarket) {
        if (StringUtils.isBlank(id)) {
            throw new CustomerException(CommonCode.INVALID_PARAM);
        }
        CourseMarket one = this.getCourseMarketById(id);
        if (one != null) {
            //更新
            one.setCharge(courseMarket.getCharge());
            one.setStartTime(courseMarket.getStartTime());//课程有效期，开始时间
            one.setEndTime(courseMarket.getEndTime());//课程有效期，结束时间
            one.setPrice(courseMarket.getPrice());
            one.setQq(courseMarket.getQq());
            one.setValid(courseMarket.getValid());
            courseMarketRepository.save(one);
        } else {
            //添加
            one = new CourseMarket();
            BeanUtils.copyProperties(courseMarket, one);
            one.setId(id);
            courseMarketRepository.save(one);
        }
        return one;
    }

    /**
     * 保存图片
     *
     * @param courseId 课程id
     * @param pic      图片id
     * @return
     */
    @Override
    @Transactional
    public ResponseResult saveCoursePic(String courseId, String pic) {
        if (StringUtils.isBlank(courseId) || StringUtils.isBlank(pic)) {
            throw new CustomerException(CommonCode.INVALID_PARAM);
        }
        CoursePic coursePic = null;
        Optional<CoursePic> optional = coursePicRepository.findById(courseId);
        coursePic = optional.orElseGet(CoursePic::new);
        coursePic.setCourseid(courseId);
        coursePic.setPic(pic);
        coursePicRepository.save(coursePic);
        return new ResponseResult(CommonCode.SUCCESS);
    }

    /**
     * 查询图片
     *
     * @param courseId 课程id
     * @return
     */
    @Override
    public CoursePic findCoursepic(String courseId) {
        if (StringUtils.isBlank(courseId)) {
            throw new CustomerException(CommonCode.INVALID_PARAM);
        }
        Optional<CoursePic> optional = coursePicRepository.findById(courseId);
        return optional.orElse(null);
    }

    /**
     * 删除课程图片
     *
     * @param courseId 课程id
     * @return
     */
    @Override
    @Transactional
    public ResponseResult deleteCoursePic(String courseId) {
        if (StringUtils.isBlank(courseId)) {
            throw new CustomerException(CommonCode.INVALID_PARAM);
        }
        int count = coursePicRepository.deleteByCourseid(courseId);
        if (count == 0) {
            return new ResponseResult(CommonCode.FAIL);
        }
        return new ResponseResult(CommonCode.SUCCESS);
    }

    /**
     * 查询课程视图数据
     *
     * @param id 课程id
     * @return
     */
    @Override
    public CourseView getCourseView(String id) {
        if (StringUtils.isBlank(id)) {
            throw new CustomerException(CommonCode.INVALID_PARAM);
        }
        CourseView courseView = new CourseView();
        //查询课程基础信息
        Optional<CourseBase> baseOptional = courseBaseRepository.findById(id);
        if (baseOptional.isPresent()) {
            CourseBase courseBase = baseOptional.get();
            courseView.setCourseBase(courseBase);
        }
        //查询课程图片
        Optional<CoursePic> picOptional = coursePicRepository.findById(id);
        if (picOptional.isPresent()) {
            CoursePic coursePic = picOptional.get();
            courseView.setCoursePic(coursePic);
        }
        //查询课程营销信息
        Optional<CourseMarket> marketOptional = courseMarketRepository.findById(id);
        if (marketOptional.isPresent()) {
            CourseMarket courseMarket = marketOptional.get();
            courseView.setCourseMarket(courseMarket);
        }
        //查询课程计划
        TeachplanNode teachplanNode = teachplanMapper.selectList(id);
        courseView.setTeachplanNode(teachplanNode);
        return courseView;
    }

    /**
     * 预览课程
     *
     * @param id 课程id
     * @return
     */
    @Override
    public CoursePublishResult preview(String id) {
        if (StringUtils.isBlank(id)) {
            throw new CustomerException(CommonCode.INVALID_PARAM);
        }
        CourseBase one = this.getCoursebaseById(id);
        //发布课程预览页面
        CmsPage cmsPage = new CmsPage();
        cmsPage.setSiteId(prop.getSiteId());
        cmsPage.setPageName(id + ".html");
        cmsPage.setPageAliase(one.getName());
        cmsPage.setTemplateId(prop.getTemplateId());
        cmsPage.setPageWebPath(prop.getPageWebPath());
        cmsPage.setPagePhysicalPath(prop.getPagePhysicalPath());
        cmsPage.setDataUrl(prop.getDataUrlPre() + id);
        //远程请求cms保存页面信息
        CmsPageResult cmsPageResult = cmsPageClient.save(cmsPage);
        if (!cmsPageResult.isSuccess()) {
            return new CoursePublishResult(CommonCode.FAIL, null);
        }
        //页面id
        String pageId = cmsPageResult.getCmsPage().getPageId();
        //页面url
        String url = prop.getPreviewUrl() + pageId;
        return new CoursePublishResult(CommonCode.SUCCESS, url);
    }

    /**
     * 发布页面
     *
     * @param id 课程id
     * @return
     */
    @Override
    @Transactional
    public CoursePublishResult publish(String id) {
        //发布课程详情页面
        CmsPostPageResult cmsPostPageResult = publishPage(id);
        if (!cmsPostPageResult.isSuccess()) {
            throw new CustomerException(CommonCode.FAIL);
        }
        //更新课程状态
        CourseBase courseBase = saveCoursePubState(id);
        if (courseBase == null) {
            throw new CustomerException(CommonCode.FAIL);
        }
        //课程索引
        CoursePub coursePub = createCoursePub(id);
        //向数据库保存课程索引信息
        CoursePub newCoursePub = saveCoursePub(id, coursePub);
        if (newCoursePub == null) {
            throw new CustomerException(CourseCode.COURSE_PUBLISH_CREATE_INDEX_ERROR);
        }
        //保存课程计划媒资信息到待索引表
        saveTeachplanMediaPub(id);
        //课程缓存
        //页面url
        String pageUrl = cmsPostPageResult.getPageUrl();
        return new CoursePublishResult(CommonCode.SUCCESS, pageUrl);
    }

    //保存课程计划媒资信息
    private void saveTeachplanMediaPub(String courseId) {
        //查询课程媒资信息
        List<TeachplanMedia> mediaList = teachplanMediaRepository.findByCourseId(courseId);
        if (CollectionUtils.isEmpty(mediaList)) {
            throw new CustomerException(CourseCode.COURSE_MEDIA_LISTISNULL);
        }
        //将课程计划媒资信息存储待索引表
        //先删后插
        int count = teachplanMediaPubRepository.deleteByCourseId(courseId);
        List<TeachplanMediaPub> mediaPubList = new ArrayList<>();
        for (TeachplanMedia teachplanMedia : mediaList) {
            TeachplanMediaPub teachplanMediaPub = new TeachplanMediaPub();
            BeanUtils.copyProperties(teachplanMedia, teachplanMediaPub);
            teachplanMediaPub.setTimestamp(new Date());
            mediaPubList.add(teachplanMediaPub);
        }
        teachplanMediaPubRepository.saveAll(mediaPubList);
    }

    /**
     * 课程计划保存视频信息
     *
     * @param teachplanMedia 保存的对象
     * @return
     */
    @Override
    public ResponseResult saveMedia(TeachplanMedia teachplanMedia) {
        //判断参数
        if (teachplanMedia == null || StringUtils.isBlank(teachplanMedia.getTeachplanId())) {
            throw new CustomerException(CommonCode.INVALID_PARAM);
        }
        Optional<Teachplan> optional = teachplanRepository.findById(teachplanMedia.getTeachplanId());
        if (!optional.isPresent()) {
            throw new CustomerException(CourseCode.COURSE_MEDIA_TEACHPLAN_ISNULL);
        }
        //得到教学计划
        Teachplan teachplan = optional.get();
        //得到等级
        String grade = teachplan.getGrade();
        //判断等级
        if (!StringUtils.equals("3", grade)) {
            throw new CustomerException(CourseCode.COURSE_MEDIA_TEACHPLAN_GRADEERROR);
        }
        TeachplanMedia one = null;
        //查询媒资教学计划表
        Optional<TeachplanMedia> mediaOptional = teachplanMediaRepository.findById(teachplanMedia.getTeachplanId());
        if (!mediaOptional.isPresent()) {
            one = new TeachplanMedia();
        }else{
            one = mediaOptional.get();
        }
        one.setTeachplanId(teachplanMedia.getTeachplanId());
        one.setMediaId(teachplanMedia.getMediaId());
        one.setMediaFileOriginalName(teachplanMedia.getMediaFileOriginalName());
        one.setMediaUrl(teachplanMedia.getMediaUrl());
        one.setCourseId(teachplan.getCourseid());
        teachplanMediaRepository.save(one);
        return new ResponseResult(CommonCode.SUCCESS);
    }

    private CourseBase saveCoursePubState(String id) {
        CourseBase coursebase = this.getCoursebaseById(id);
        //更新发布状态
        coursebase.setStatus("202002");
        CourseBase save = courseBaseRepository.save(coursebase);
        return save;
    }

    private CmsPostPageResult publishPage(String id) {
        if (StringUtils.isBlank(id)) {
            throw new CustomerException(CommonCode.INVALID_PARAM);
        }
        CourseBase one = this.getCoursebaseById(id);
        //发布课程预览页面
        CmsPage cmsPage = new CmsPage();
        cmsPage.setSiteId(prop.getSiteId());
        cmsPage.setPageName(id + ".html");
        cmsPage.setPageAliase(one.getName());
        cmsPage.setTemplateId(prop.getTemplateId());
        cmsPage.setPageWebPath(prop.getPageWebPath());
        cmsPage.setPagePhysicalPath(prop.getPagePhysicalPath());
        cmsPage.setDataUrl(prop.getDataUrlPre() + id);
        //发布页面
        return cmsPageClient.postPageQuick(cmsPage);
    }

    public CoursePub saveCoursePub(String id, CoursePub coursePub) {
        if (StringUtils.isBlank(id)) {
            throw new CustomerException(CommonCode.INVALID_PARAM);
        }
        CoursePub coursePubNew = null;
        Optional<CoursePub> coursePubOptional = coursePubRepository.findById(id);
        coursePubNew = coursePubOptional.orElseGet(CoursePub::new);
        //复制
        BeanUtils.copyProperties(coursePub, coursePubNew);
        //设置主键
        coursePubNew.setId(id);
        //更新时间戳为最新时间
        coursePubNew.setTimestamp(new Date());
        //发布时间
        coursePubNew.setPubTime(new Date());
        //保存
        coursePubRepository.save(coursePubNew);
        return coursePubNew;
    }

    private CoursePub createCoursePub(String id){
        CoursePub coursePub = new CoursePub();
        coursePub.setId(id);
        //基础信息
        Optional<CourseBase> courseBaseOptional = courseBaseRepository.findById(id);
        if(courseBaseOptional.isPresent()){
            CourseBase courseBase = courseBaseOptional.get();
            BeanUtils.copyProperties(courseBase, coursePub);
        }
        //查询课程图片
        Optional<CoursePic> picOptional = coursePicRepository.findById(id);
        if(picOptional.isPresent()){
            CoursePic coursePic = picOptional.get();
            BeanUtils.copyProperties(coursePic, coursePub);
        }
        //课程营销信息
        Optional<CourseMarket> marketOptional = courseMarketRepository.findById(id);
        if(marketOptional.isPresent()){
            CourseMarket courseMarket = marketOptional.get();
            BeanUtils.copyProperties(courseMarket, coursePub);
        }
        //课程计划
        TeachplanNode teachplanNode = teachplanMapper.selectList(id);
        //将课程计划转成json
        String teachplanString = JSON.toJSONString(teachplanNode);
        coursePub.setTeachplan(teachplanString);
        return coursePub;
    }
}
