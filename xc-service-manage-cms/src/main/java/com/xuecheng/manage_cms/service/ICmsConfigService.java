package com.xuecheng.manage_cms.service;

import com.xuecheng.framework.domain.cms.CmsConfig;

public interface ICmsConfigService {
    //根据id查询模板数据
    CmsConfig getConfigById(String id);
}
