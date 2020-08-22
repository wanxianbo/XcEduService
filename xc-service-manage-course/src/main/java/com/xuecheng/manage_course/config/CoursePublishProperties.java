package com.xuecheng.manage_course.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "course.publish")
public class CoursePublishProperties {
    private String dataUrlPre;
    private String pagePhysicalPath;
    private String pageWebPath;
    private String siteId;
    private String templateId;
    private String previewUrl;

}
