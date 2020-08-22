package com.xuecheng.manage_media.service.impl;


import com.xuecheng.framework.domain.media.MediaFile;
import com.xuecheng.framework.domain.media.request.QueryMediaFileRequest;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import com.xuecheng.manage_media.dao.MediaFileRepository;
import com.xuecheng.manage_media.service.IMediaFileService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class MediaFileService implements IMediaFileService {

    @Autowired
    private MediaFileRepository mediaFileRepository;

    /**
     * 查询媒资列表
     *
     * @param page                  当前页
     * @param size                  每页大小
     * @param queryMediaFileRequest 查询条件
     * @return
     */
    @Override
    public QueryResponseResult<MediaFile> findList(int page, int size, QueryMediaFileRequest queryMediaFileRequest) {
        if (page <= 0) {
            page = 1;
        }
        if (size <= 0) {
            size = 10;
        }
        if (queryMediaFileRequest == null) {
            queryMediaFileRequest = new QueryMediaFileRequest();
        }

        MediaFile mediaFile = new MediaFile();
        //查询条件对象
        if (StringUtils.isNotBlank(queryMediaFileRequest.getFileOriginalName())) {
            //查询条件
            mediaFile.setFileOriginalName(queryMediaFileRequest.getFileOriginalName());
        }
        if (StringUtils.isNotBlank(queryMediaFileRequest.getProcessStatus())) {
            //查询条件
            mediaFile.setProcessStatus(queryMediaFileRequest.getProcessStatus());
        }
        //查询条件匹配器
        ExampleMatcher exampleMatcher = ExampleMatcher.matching()
                .withMatcher("fileOriginalName", ExampleMatcher.GenericPropertyMatchers.contains())
                .withMatcher("tag", ExampleMatcher.GenericPropertyMatchers.contains());
        //定义example对象
        Example<MediaFile> example = Example.of(mediaFile, exampleMatcher);
        //定义分页对象
        PageRequest pageRequest = PageRequest.of(page-1, size);
        //执行查询
        Page<MediaFile> all = mediaFileRepository.findAll(example, pageRequest);
        //总记录数
        long totalElements = all.getTotalElements();
        //总页数
        int totalPages = all.getTotalPages();
        //查询内容
        List<MediaFile> content = all.getContent();

        QueryResult<MediaFile> queryResult = new QueryResult<>();
        queryResult.setTotal(totalElements);
        queryResult.setTotalPage(totalPages);
        queryResult.setList(content);
        return new QueryResponseResult<>(CommonCode.SUCCESS,queryResult);
    }
}
