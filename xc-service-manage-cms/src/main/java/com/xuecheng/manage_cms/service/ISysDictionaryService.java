package com.xuecheng.manage_cms.service;

import com.xuecheng.framework.domain.system.SysDictionary;

public interface ISysDictionaryService {
    SysDictionary findDictionaryByType(String type);
}
