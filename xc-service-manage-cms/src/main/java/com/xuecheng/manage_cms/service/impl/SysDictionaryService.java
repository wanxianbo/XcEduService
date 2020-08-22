package com.xuecheng.manage_cms.service.impl;

import com.xuecheng.framework.domain.system.SysDictionary;
import com.xuecheng.framework.exception.CustomerException;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.manage_cms.dao.CmsSysDictionaryRepository;
import com.xuecheng.manage_cms.service.ISysDictionaryService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SysDictionaryService implements ISysDictionaryService {

    @Autowired
    private CmsSysDictionaryRepository cmsSysDictionaryRepository;

    //根据字典分类type查询自带你信息
    @Override
    public SysDictionary findDictionaryByType(String type) {
        if (StringUtils.isBlank(type)) {
            throw new CustomerException(CommonCode.INVALID_PARAM);
        }
        return cmsSysDictionaryRepository.findBydType(type);
    }

}
