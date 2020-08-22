package com.xuecheng.test.search;

import com.xuecheng.framework.domain.course.TeachplanMediaPub;
import com.xuecheng.pojo.Course;
import com.xuecheng.pojo.SearchCoursePub;
import com.xuecheng.pojo.SearchTeachplanMediaPub;
import com.xuecheng.repository.CoursePubESRepository;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchResultMapper;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.aggregation.impl.AggregatedPageImpl;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.GetQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;


@SpringBootTest
@RunWith(SpringRunner.class)
public class TestSearch {


//    @Autowired
//    private ElasticsearchTemplate template;

    @Autowired
    private ElasticsearchTemplate template;

//    @Autowired
//    private RestHighLevelClient highLevelClient;

    @Autowired
    private CoursePubESRepository coursePubRepository;

    @Test
    public void testAddDocument() {
        SearchCoursePub searchCoursePub = new SearchCoursePub();
        searchCoursePub.setId("1");
        searchCoursePub.setName("6666666666");
        searchCoursePub.setUsers("java爱好者d");
        searchCoursePub.setMt("1-3");
        searchCoursePub.setSt("1-3-3");
        searchCoursePub.setGrade("200002");
        searchCoursePub.setStudymodel("201002");
        searchCoursePub.setDescription("test_java基础33test_java基础33");
        searchCoursePub.setTimestamp(new Date());
        searchCoursePub.setCharge("203002");
        searchCoursePub.setValid("204002");
        searchCoursePub.setQq("21212121");
        searchCoursePub.setPrice(55f);
        searchCoursePub.setPic("group1/M00/00/02/wKhlQFrQfNqAL0d_AALDG1Ia4xE439.png");
        searchCoursePub.setPubTime(new Date());
        SearchCoursePub save = coursePubRepository.save(searchCoursePub);
        System.out.println(save);
    }

    @Test
    public void testCreateMapping() {
        //template.createIndex(SearchTeachplanMediaPub.class);
        //boolean b = template.putMapping(SearchTeachplanMediaPub.class);
        //System.out.println(b);
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        queryBuilder.withQuery(QueryBuilders.termsQuery("teachplan_id", "40280981739aea0f01739af623590001"));
        List<SearchTeachplanMediaPub> list = template.queryForList(queryBuilder.build(), SearchTeachplanMediaPub.class);
        for (SearchTeachplanMediaPub searchTeachplanMediaPub : list) {
            System.out.println(searchTeachplanMediaPub);
        }
    }

    @Test
    public void testSearchTerm() {
        int page = 0;
        int size = 10;
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        MatchAllQueryBuilder matchAllQuery = QueryBuilders.matchAllQuery();
        queryBuilder.withQuery(matchAllQuery);
        //queryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{}, new String[]{}));
        queryBuilder.withPageable(PageRequest.of(page, size));
        AggregatedPage<SearchCoursePub> SearchCoursePub = template.queryForPage(queryBuilder.build(), SearchCoursePub.class);
        List<SearchCoursePub> content = SearchCoursePub.getContent();
        for (SearchCoursePub course : content) {
            System.out.println(course);
        }
    }

    @Test
    public void testGetQuery() {
        SearchCoursePub coursePub = template.queryForObject(GetQuery.getById("4028e581617f945f01617f9dabc40000"), SearchCoursePub.class);
        System.out.println(coursePub);
    }
    @Test
    public void testSearchTerms() {
        int page = 0;
        int size = 5;
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        MultiMatchQueryBuilder multiMatchQueryBuilder = QueryBuilders
                .multiMatchQuery("spring框架", "name", "description")
                .minimumShouldMatch("50%")
                .field("name", 10);
        queryBuilder.withQuery(multiMatchQueryBuilder);
        queryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{"name", "studymodel"}, new String[]{}));
        queryBuilder.withPageable(PageRequest.of(page, size));
        AggregatedPage<Course> courses = template.queryForPage(queryBuilder.build(), Course.class);
        List<Course> content = courses.getContent();
        for (Course course : content) {
            System.out.println(course);
        }
    }

    @Test
    public void testBoolQuery() {
        int page = 0;
        int size = 2;
        //构建原生的查询构造器
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        //布尔查询器
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.must(QueryBuilders.multiMatchQuery("spring框架","name","description"));
        //boolQueryBuilder.must(QueryBuilders.termQuery("studymodel", "201001"));
        // 过滤查询
        boolQueryBuilder.filter(QueryBuilders.termQuery("studymodel", "201001"));
        boolQueryBuilder.filter(QueryBuilders.rangeQuery("price").gte(60).lte(100));
        queryBuilder.withQuery(boolQueryBuilder);
        queryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{"name", "description","studymodel"}, new String[]{}));
        queryBuilder.withPageable(PageRequest.of(page, size));
        //rest执行查询
        AggregatedPage<Course> courses = template.queryForPage(queryBuilder.build(), Course.class);
        //遍历结果
//        StringTerms aggregation = (StringTerms) courses.getAggregation("");
//        List<StringTerms.Bucket> buckets = aggregation.getBuckets();

        List<Course> content = courses.getContent();
        for (Course course : content) {
            System.out.println(course);
        }
    }

    @Test
    public void testBoolQueryAndRange() {
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        //布尔查询
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        //boolQueryBuilder.must(QueryBuilders.multiMatchQuery("spring框架","name","description"));
        //boolQueryBuilder.must(QueryBuilders.termQuery("studymodel", "201001"));
        //boolQueryBuilder.filter(QueryBuilders.termQuery("studymodel", "201001"));
        //过滤查询
        boolQueryBuilder.filter(QueryBuilders.rangeQuery("price").gte(0).lte(100));
        //搜索排序
        queryBuilder.withSort(new FieldSortBuilder("studymodel").order(SortOrder.DESC));
        queryBuilder.withSort(new FieldSortBuilder("price").order(SortOrder.ASC));
        queryBuilder.withQuery(boolQueryBuilder);
        queryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{"name", "description", "studymodel", "price"}, new String[]{}));
        //执行查询
        AggregatedPage<Course> courses = template.queryForPage(queryBuilder.build(), Course.class);
        List<Course> content = courses.getContent();
        for (Course course : content) {
            System.out.println(course);
        }
    }


    @Test
    public void testHighLightSearch() {
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        //布尔查询
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.must(QueryBuilders.multiMatchQuery("基础", "name", "description")
                .minimumShouldMatch("50%")
                .field("name", 10)
                .field("description")
                .type(MultiMatchQueryBuilder.Type.BEST_FIELDS));
       //过滤查询
        boolQueryBuilder.filter(QueryBuilders.rangeQuery("price").gte(0).lte(100));
        //搜索排序
        queryBuilder.withSort(new FieldSortBuilder("price").order(SortOrder.ASC));

        queryBuilder.withQuery(boolQueryBuilder);
        queryBuilder.withPageable(PageRequest.of(0, 10));

        queryBuilder.withHighlightBuilder(new HighlightBuilder()
                .preTags("<font class = 'eslight'>")
                .postTags("</font>")
                .field("name"));
        queryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{"name", "description", "studymodel", "price"}, new String[]{}));
        AggregatedPage<SearchCoursePub> courses = template.queryForPage(queryBuilder.build(), SearchCoursePub.class, new SearchResultMapper() {
            @Override
            public <T> AggregatedPage<T> mapResults(SearchResponse response, Class<T> clazz, Pageable pageable) {
                SearchHits hits = response.getHits();
                List<SearchCoursePub> list = new ArrayList<>();
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
                    list.add(coursePub);
                }
                if (list.size() > 0) {
                    return new AggregatedPageImpl<>((List<T>) list);
                }
                return null;
            }

            @Override
            public <T> T mapSearchHit(SearchHit searchHit, Class<T> type) {
                return null;
            }
        });
        List<SearchCoursePub> content = courses.getContent();
        for (SearchCoursePub coursePub : content) {
            System.out.println(coursePub);
        }

    }
}
