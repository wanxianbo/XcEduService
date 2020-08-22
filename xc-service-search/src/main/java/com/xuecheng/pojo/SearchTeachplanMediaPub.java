package com.xuecheng.pojo;


import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.io.Serializable;

@Data
@Document(indexName = "xc_course_media",type = "doc",shards = 1,replicas = 0)
public class SearchTeachplanMediaPub implements Serializable {

    @Id
    @Field(type = FieldType.Keyword)
    private String teachplan_id;
    @Field(type = FieldType.Keyword)
    private String media_id;
    @Field(type = FieldType.Text,index = false)
    private String media_fileoriginalname;
    @Field(type = FieldType.Text,index = false)
    private String media_url;
    @Field(type = FieldType.Keyword)
    private String courseid;

}
