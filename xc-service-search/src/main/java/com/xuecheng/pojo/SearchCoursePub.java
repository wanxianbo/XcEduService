package com.xuecheng.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.io.Serializable;
import java.util.Date;

@Data
@Document(indexName = "xc_course",type = "doc",shards = 1,replicas = 0)
public class SearchCoursePub implements Serializable {
    @Id
    @Field(type = FieldType.Keyword)
    private String id;
    @Field(type = FieldType.Text,analyzer = "ik_max_word",searchAnalyzer = "ik_smart")
    private String name;
    @Field(type = FieldType.Text,index = false)
    private String users;
    @Field(type = FieldType.Keyword)
    private String mt;
    @Field(type = FieldType.Keyword)
    private String st;
    @Field(type = FieldType.Keyword)
    private String grade;
    @Field(type = FieldType.Keyword)
    private String studymodel;
    @Field(type = FieldType.Keyword)
    private String teachmode;
    @Field(type = FieldType.Text,analyzer = "ik_max_word",searchAnalyzer = "ik_smart")
    private String description;
    @Field(type = FieldType.Keyword,index = false)
    private String pic;//图片
    @Field(type = FieldType.Date)
    //@JsonFormat(shape = JsonFormat.Shape.STRING, pattern ="yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private Date timestamp;//时间戳
    @Field(type = FieldType.Keyword)
    private String charge;
    @Field(type = FieldType.Keyword)
    private String valid;
    @Field(type = FieldType.Keyword,index = false)
    private String qq;
    @Field(type = FieldType.Float)
    private Float price;
    @Field(type = FieldType.Float)
    private Float price_old;
    @Field(type = FieldType.Date)
    //@JsonFormat(shape = JsonFormat.Shape.STRING, pattern ="yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private Date expires;
    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String teachplan;//课程计划
    @Field(type = FieldType.Date)
    //@JsonFormat(shape = JsonFormat.Shape.STRING, pattern ="yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private Date pubTime;//课程发布时间
}
