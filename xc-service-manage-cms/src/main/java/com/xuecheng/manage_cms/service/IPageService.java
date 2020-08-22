package com.xuecheng.manage_cms.service;

import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.request.QueryPageRequest;
import com.xuecheng.framework.domain.cms.response.CmsPageResult;
import com.xuecheng.framework.domain.cms.response.CmsPostPageResult;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.ResponseResult;

public interface IPageService {
    //查询全部
    QueryResponseResult findList(int page, int size, QueryPageRequest queryPageRequest);

    //添加新页面
    CmsPageResult add(CmsPage cmsPage);

    //通过Id查询页面
    CmsPage findById(String id);

    //修改页面
    CmsPageResult edit(String id,CmsPage cmsPage);

    //删除页面（id）
    ResponseResult delete(String id);

    //发布页面
    ResponseResult postPage(String pageId);

    //保存页面
    CmsPageResult save(CmsPage cmsPage);

    //一键发布
    CmsPostPageResult postPageQuick(CmsPage cmsPage);
}
