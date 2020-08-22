package com.xuecheng.pojo;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.Date;

@Data
@Document(indexName = "xc_course",type = "doc",shards = 1)
public class Course {
    @Id
    private Long id;
    @Field(type = FieldType.Text,analyzer = "ik_max_word",searchAnalyzer = "ik_smart")
    private String name;
    @Field(type = FieldType.Text,analyzer = "ik_max_word",searchAnalyzer = "ik_smart")
    private String description;
    @Field(type = FieldType.Keyword)
    private String studymodel;
    @Field(type = FieldType.Text,index = false)
    private String pic;
    @Field(type = FieldType.Float)
    private Float price;
    @Field(type = FieldType.Date,format = DateFormat.year_month_day)
    private Date timestamp;

}
