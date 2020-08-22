package com.xuecheng.manage_cms.service.impl;

import com.alibaba.fastjson.JSON;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.CmsSite;
import com.xuecheng.framework.domain.cms.request.QueryPageRequest;
import com.xuecheng.framework.domain.cms.response.CmsCode;
import com.xuecheng.framework.domain.cms.response.CmsPageResult;
import com.xuecheng.framework.domain.cms.response.CmsPostPageResult;
import com.xuecheng.framework.exception.CustomerException;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_cms.config.RabbitmqConfig;
import com.xuecheng.manage_cms.dao.CmsPageRepository;
import com.xuecheng.manage_cms.dao.CmsSiteRepository;
import com.xuecheng.manage_cms.service.ICmsTemplateService;
import com.xuecheng.manage_cms.service.IPageService;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class PageService implements IPageService {

    @Autowired
    private CmsPageRepository cmsPageRepository;

    @Autowired
    private ICmsTemplateService cmsTemplateService;

    @Autowired
    private CmsSiteRepository cmsSiteRepository;

    @Autowired
    private GridFsTemplate gridFsTemplate;
    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 页面列表分页查询
     *
     * @param page             当前页码
     * @param size             每页数量
     * @param queryPageRequest 查询条件
     * @return
     */
    @Override
    public QueryResponseResult findList(int page, int size, QueryPageRequest queryPageRequest) {
        if (queryPageRequest == null) {
            queryPageRequest = new QueryPageRequest();
        }
        //创建查询条件对象
        CmsPage cmsPage = new CmsPage();
        if (StringUtils.isNotBlank(queryPageRequest.getSiteId())) {
            cmsPage.setSiteId(queryPageRequest.getSiteId());
        }
        if (StringUtils.isNotBlank(queryPageRequest.getTemplateId())) {
            cmsPage.setTemplateId(queryPageRequest.getTemplateId());
        }
        if (StringUtils.isNotBlank(queryPageRequest.getPageAliase())) {
            cmsPage.setPageAliase(queryPageRequest.getPageAliase());
        }
        if (StringUtils.isNotBlank(queryPageRequest.getPageName())) {
            cmsPage.setPageName(queryPageRequest.getPageName());
        }
        if (StringUtils.isNotBlank(queryPageRequest.getPageType())) {
            cmsPage.setPageType(queryPageRequest.getPageType());
        }
        //条件匹配器
        //页面名称模糊查询，需要自定义字符串的匹配器实现模糊查询
        ExampleMatcher exampleMatcher = ExampleMatcher.matching()
                .withMatcher("pageAliase", ExampleMatcher.GenericPropertyMatchers.contains())
                .withMatcher("pageName", ExampleMatcher.GenericPropertyMatchers.contains());
        Example<CmsPage> example = Example.of(cmsPage, exampleMatcher);

        if (page <= 0) {
            page = 1;
        }
        if (size <= 0) {
            size = 10;
        }
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<CmsPage> all = cmsPageRepository.findAll(example, pageable);
        QueryResult<CmsPage> queryResult = new QueryResult<>();
        queryResult.setList(all.getContent());
        queryResult.setTotal(all.getTotalElements());
        //返回结果
        return new QueryResponseResult(CommonCode.SUCCESS, queryResult);
    }

    /**
     * 添加新页面
     *
     * @param cmsPage 页面model
     * @return CmsPageResult
     */
    @Override
    public CmsPageResult add(CmsPage cmsPage) {
        if (cmsPage == null) {
            throw new CustomerException(CommonCode.INVALID_PARAM);
        }
        //判断新添加的页面是否已存在
        CmsPage page = cmsPageRepository.findByPageNameAndSiteIdAndPageWebPath(cmsPage.getPageName(), cmsPage.getSiteId(), cmsPage.getPageWebPath());
        if (page != null) {
            throw new CustomerException(CmsCode.CMS_ADDPAGE_EXISTSNAME);
        }
        cmsPage.setPageId(null);//让页面id自动生成
        cmsPageRepository.save(cmsPage);
        //返回结果
        CmsPageResult cmsPageResult = new CmsPageResult(CommonCode.SUCCESS, cmsPage);
        return cmsPageResult;
    }

    /**
     * 通过id查询页面
     *
     * @param id 页面Id
     * @return
     */
    @Override
    public CmsPage findById(String id) {
        Optional<CmsPage> optional = cmsPageRepository.findById(id);
        if (optional.isPresent()) {
            return optional.get();
        }
        return null;
    }

    /**
     * 保存页面
     * @param cmsPage
     * @return
     */
    @Override
    public CmsPageResult save(CmsPage cmsPage) {
        CmsPage one = cmsPageRepository.findByPageNameAndSiteIdAndPageWebPath(cmsPage.getPageName(), cmsPage.getSiteId(), cmsPage.getPageWebPath());
        if (one != null) {
            //页面已存在,更新
            return edit(one.getPageId(), cmsPage);
        } else {
            //添加
            return add(cmsPage);
        }
    }

    /**
     * 修改页面
     *
     * @param id      页面id
     * @param cmsPage 页面model
     * @return
     */
    @Override
    public CmsPageResult edit(String id, CmsPage cmsPage) {
        //1.先查询页面
        CmsPage oldPage = this.findById(id);
        if (oldPage != null) {
            oldPage.setTemplateId(cmsPage.getTemplateId());
            //更新所属站点
            oldPage.setSiteId(cmsPage.getSiteId());
            //更新页面别名
            oldPage.setPageAliase(cmsPage.getPageAliase());
            //更新页面名称
            oldPage.setPageName(cmsPage.getPageName());
            //更新访问路径
            oldPage.setPageWebPath(cmsPage.getPageWebPath());
            //更新物理路径
            oldPage.setPagePhysicalPath(cmsPage.getPagePhysicalPath());
            //更新数据url
            oldPage.setDataUrl(cmsPage.getDataUrl());
            //执行更新
            CmsPage newPage = cmsPageRepository.save(oldPage);
            //返回成功
            return new CmsPageResult(CommonCode.SUCCESS, newPage);
        }
        //返回失败
        return new CmsPageResult(CommonCode.FAIL, null);
    }

    /**
     * 先查询再删除
     * 通过id删除页面
     *
     * @param id 页面id
     * @return
     */
    @Override
    public ResponseResult delete(String id) {
        CmsPage one = this.findById(id);
        if (one != null) {
            //删除页面
            cmsPageRepository.deleteById(id);
            return new ResponseResult(CommonCode.SUCCESS);
        }
        return new ResponseResult(CommonCode.FAIL);
    }

    /**
     * 发布页面
     *
     * @param pageId
     * @return
     */
    @Override
    public ResponseResult postPage(String pageId) {
        //执行静态化
        String pageHtml = cmsTemplateService.getPageHtml(pageId);
        if (StringUtils.isBlank(pageHtml)) {
            throw new CustomerException(CmsCode.CMS_GENERATEHTML_HTMLISNULL);
        }
        //保存静态页面html
        CmsPage cmsPage = saveHtml(pageId, pageHtml);
        //发送信息
        sendPostPage(pageId);
        return new ResponseResult(CommonCode.SUCCESS);
    }

    /**
     * 一键发布
     *
     * @param cmsPage
     * @return
     */
    @Override
    public CmsPostPageResult postPageQuick(CmsPage cmsPage) {
        //添加页面
        if (cmsPage == null) {
            throw new CustomerException(CommonCode.INVALID_PARAM);
        }
        CmsPageResult cmsPageResult = this.save(cmsPage);
        if (!cmsPageResult.isSuccess()) {
            return new CmsPostPageResult(CommonCode.FAIL, null);
        }
        CmsPage one = cmsPageResult.getCmsPage();
        //得到pageId
        String pageId = one.getPageId();
        //发布页面
        ResponseResult responseResult = this.postPage(pageId);
        if (!responseResult.isSuccess()) {
            return new CmsPostPageResult(CommonCode.FAIL, null);
        }
        //页面url=站点域名+站点webpath+页面webpath+页面名称
        String siteId = one.getSiteId();
        CmsSite cmsSite = findCmsSiteById(siteId);
        String siteDomain = cmsSite.getSiteDomain();
        String siteWebPath = cmsSite.getSiteWebPath();
        String pageWebPath = one.getPageWebPath();
        String pageName = one.getPageName();
        String pageUrl = siteDomain + siteWebPath + pageWebPath + pageName;
        return new CmsPostPageResult(CommonCode.SUCCESS, pageUrl);
    }

    private CmsSite findCmsSiteById(String siteId) {
        Optional<CmsSite> optional = cmsSiteRepository.findById(siteId);
        return optional.orElse(null);
    }

    /**
     * 发送消息到rabbitmq
     *
     * @param pageId
     */
    private void sendPostPage(String pageId) {
        CmsPage cmsPage = findById(pageId);
        if (cmsPage == null) {
            throw new CustomerException(CmsCode.CMS_PAGE_NOTEXISTS);
        }
        Map<String, String> map = new HashMap<>();
        map.put("pageId", pageId);
        //消息内容
        String msg = JSON.toJSONString(map);
        System.out.println(msg);
        //routingKey
        String siteId = cmsPage.getSiteId();
        //发布消息
        rabbitTemplate.convertAndSend(RabbitmqConfig.EX_ROUTING_CMS_POSTPAGE, siteId, msg);
    }

    /**
     * 保存静态文件
     *
     * @param pageId  页面id
     * @param content 静态化文件
     * @return
     */
    private CmsPage saveHtml(String pageId, String content) {
        Optional<CmsPage> optional = cmsPageRepository.findById(pageId);
        if (!optional.isPresent()) {
            throw new CustomerException(CmsCode.CMS_PAGE_NOTEXISTS);
        }
        CmsPage cmsPage = optional.get();
        //存储之前先删除
        String htmlFileId = cmsPage.getHtmlFileId();
        if (StringUtils.isNotBlank(htmlFileId)) {
            gridFsTemplate.delete(Query.query(Criteria.where("_id").is(htmlFileId)));
        }
        //保存html文件到GridFS
        try {
            InputStream inputStream = IOUtils.toInputStream(content, "utf-8");
            ObjectId objectId = gridFsTemplate.store(inputStream, cmsPage.getPageName());
            //将文件id存储到cmspage中
            cmsPage.setHtmlFileId(objectId.toString());
            cmsPageRepository.save(cmsPage);
            return cmsPage;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
