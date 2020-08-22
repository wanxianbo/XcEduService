package com.xuecheng.manage_cms_client.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "xuecheng.mq")
public class RabbitmqProperties {

    private String queue;
    private String routingKey;
}
