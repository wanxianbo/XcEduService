package com.xuecheng.auth.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "auth")
@Data
public class AuthInfoConfig {
    private int tokenValiditySeconds;  //token存储到redis的过期时间
    private String clientId;
    private String clientSecret;
    private String cookieDomain;
    private int cookieMaxAge;
}
