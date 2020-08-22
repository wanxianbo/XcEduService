package com.xuecheng.search;

import com.xuecheng.framework.domain.course.CoursePub;
import com.xuecheng.framework.domain.course.TeachplanMediaPub;
import com.xuecheng.framework.domain.search.CourseSearchParam;
import com.xuecheng.framework.exception.CustomerException;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import com.xuecheng.pojo.SearchCoursePub;
import com.xuecheng.pojo.SearchTeachplanMediaPub;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchResultMapper;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.aggregation.impl.AggregatedPageImpl;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.GetQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SourceFilter;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;

@Service
public class EsCourseService {

    @Autowired
    private ElasticsearchTemplate template;

    public QueryResponseResult<SearchCoursePub> list(int page, int size, CourseSearchParam courseSearchParam) {
        //分页参数设置
        if (page <= 0) {
            page = 1;
        }
        if (size <= 0) {
            size = 20;
        }
        //构建查询器
        NativeSearchQueryBuilder searchQueryBuilder = new NativeSearchQueryBuilder();
        //创建查询条件
        //分类和难度等级过滤
        BoolQueryBuilder boolQueryBuilder = buildBoolQueryBuilderFilter(courseSearchParam);
        //根据关键字查询
        if (StringUtils.isNotBlank(courseSearchParam.getKeyword())) {
            boolQueryBuilder.must(QueryBuilders.multiMatchQuery(courseSearchParam.getKeyword(), "name","teachplan", "description")
                    .minimumShouldMatch("70%")
                    .field("name", 10));
        }
        //source源字段过滤
        searchQueryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{}, new String[]{"description"}));
        //分页查询
        searchQueryBuilder.withPageable(PageRequest.of(page - 1, size));
        //执行查询
        searchQueryBuilder.withQuery(boolQueryBuilder);
        //分页与高亮
        //设置高清字段
        searchQueryBuilder.withHighlightBuilder(new HighlightBuilder()
                .preTags("<font class = 'eslight'>")
                .postTags("</font>")
                .field("name"));
        //结果处理对象
        SearchResultMapper searchResultMapper = buildResultMapper();
        AggregatedPage<SearchCoursePub> pubAggregatedPage = template.queryForPage(searchQueryBuilder.build(), SearchCoursePub.class,searchResultMapper);

        //得到数据
        long total = pubAggregatedPage.getTotalElements();
        int totalPages = pubAggregatedPage.getTotalPages();
        List<SearchCoursePub> searchCoursePubList = pubAggregatedPage.getContent();

        //取出高亮字段
        QueryResult<SearchCoursePub> queryResult = new QueryResult<>();
        //封装数据
        queryResult.setTotal(total);
        queryResult.setTotalPage(totalPages);
        queryResult.setList(searchCoursePubList);
        return new QueryResponseResult<>(CommonCode.SUCCESS, queryResult);
    }

    private SearchResultMapper buildResultMapper() {
        return new SearchResultMapper() {
            @Override
            public <T> AggregatedPage<T> mapResults(SearchResponse response, Class<T> clazz, Pageable pageable) {
                List<SearchCoursePub> coursePubList = new ArrayList<>();
                SearchHits hits = response.getHits();
                long total = hits.getTotalHits();
                SearchHit[] searchHits = hits.getHits();
                for (SearchHit hit : searchHits) {
                    Map<String, Object> sourceAsMap = hit.getSourceAsMap();
                    String name = (String) sourceAsMap.get("name");
                    Map<String, HighlightField> highlightFields = hit.getHighlightFields();
                    SearchCoursePub coursePub = new SearchCoursePub();
                    if (!CollectionUtils.isEmpty(highlightFields)) {
                        HighlightField nameField = highlightFields.get("name");
                        if (nameField != null) {
                            Text[] fragments = nameField.getFragments();
                            StringBuffer buffer = new StringBuffer();
                            for (Text fragment : fragments) {
                                buffer.append(fragment.toString());
                            }
                            name = buffer.toString();
                        }
                    }
                    coursePub.setName(name);
                    //设置id
                    String id = (String) sourceAsMap.get("id");
                    coursePub.setId(id);
                    //图片
                    String pic = (String) sourceAsMap.get("pic");
                    coursePub.setPic(pic);
                    //价格
                    Double price = (Double) sourceAsMap.get("price");
                    if (price != null) {
                        coursePub.setPrice(price.floatValue());
                    }
                    //原来价格
                    Double price_old = (Double) sourceAsMap.get("price_old");
                    if (price_old != null) {
                        coursePub.setPrice(price_old.floatValue());
                    }
                    coursePubList.add(coursePub);
                }
                if (coursePubList.size() == 0) {
                    return null;
                }
                return new AggregatedPageImpl<>((List<T>) coursePubList,pageable,total);
            }
            @Override
            public <T> T mapSearchHit(SearchHit searchHit, Class<T> type) {
                return null;
            }
        };
    }

    //搜索过滤
    private BoolQueryBuilder buildBoolQueryBuilderFilter(CourseSearchParam courseSearchParam) {
        //构建布尔查询
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        if (StringUtils.isNotBlank(courseSearchParam.getMt())) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("mt", courseSearchParam.getMt()));
        }
        if (StringUtils.isNotBlank(courseSearchParam.getSt())) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("st", courseSearchParam.getSt()));
        }
        if (StringUtils.isNotBlank(courseSearchParam.getGrade())) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("grade", courseSearchParam.getGrade()));
        }
        return boolQueryBuilder;
    }

    /**
     * 根据id查询课程信息
     *
     * @param id 课程id
     * @return
     */
    public Map<String, CoursePub> getall(String id) {
        SearchCoursePub searchCoursePub = template.queryForObject(GetQuery.getById(id), SearchCoursePub.class);
        if (searchCoursePub == null || StringUtils.isBlank(id)) {
            throw new CustomerException(CommonCode.INVALID_PARAM);
        }
        Map<String, CoursePub> map = new HashMap<>();
        CoursePub coursePub = new CoursePub();
        BeanUtils.copyProperties(searchCoursePub, coursePub);
        map.put(id, coursePub);
        return map;
    }

    /**
     * 根据课程计划查询媒资信息
     *
     * @param teachplanIds 课程计划id
     * @return
     */
    public QueryResponseResult<TeachplanMediaPub> getmedia(String[] teachplanIds) {
        //构建原始查询器
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        //精准配备
        queryBuilder.withQuery(QueryBuilders.termsQuery("teachplan_id", teachplanIds));
        //结果过滤
        queryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{"teachplan_id", "media_id", "media_fileoriginalname", "media_url", "courseid"}, new String[]{}));
        //执行查询
        List<SearchTeachplanMediaPub> teachplanMediaPubList = template.queryForList(queryBuilder.build(), SearchTeachplanMediaPub.class);
        //数据列表
        List<TeachplanMediaPub> list = new ArrayList<>();
        for (SearchTeachplanMediaPub searchTeachplanMediaPub : teachplanMediaPubList) {
            TeachplanMediaPub teachplanMediaPub = new TeachplanMediaPub();
            //数据拷贝
            teachplanMediaPub.setTeachplanId(searchTeachplanMediaPub.getTeachplan_id());
            teachplanMediaPub.setMediaId(searchTeachplanMediaPub.getMedia_id());
            teachplanMediaPub.setCourseId(searchTeachplanMediaPub.getCourseid());
            teachplanMediaPub.setMediaFileOriginalName(searchTeachplanMediaPub.getMedia_fileoriginalname());
            teachplanMediaPub.setMediaUrl(searchTeachplanMediaPub.getMedia_url());
            teachplanMediaPub.setTimestamp(new Date());
            list.add(teachplanMediaPub);
        }
        //构建返回课程媒资信息对象
        QueryResult<TeachplanMediaPub> queryResult = new QueryResult<>();
        queryResult.setList(list);
        queryResult.setTotal(list.size());
        return new QueryResponseResult<>(CommonCode.SUCCESS,queryResult);
    }
}
