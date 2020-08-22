package com.xuecheng.manage_cms.api;

import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.response.CmsPageResult;
import com.xuecheng.framework.domain.cms.response.CmsPostPageResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/cms/page")
public interface CmsPageApi {
    @PostMapping("/save")
    CmsPageResult save(@RequestBody CmsPage cmsPage);

    @PostMapping("/postPageQuick")
    CmsPostPageResult postPageQuick(CmsPage cmsPage);
}
