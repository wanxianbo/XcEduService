package com.xuecheng.repository;

import com.xuecheng.pojo.SearchCoursePub;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface CoursePubESRepository extends ElasticsearchRepository<SearchCoursePub,String> {
}
