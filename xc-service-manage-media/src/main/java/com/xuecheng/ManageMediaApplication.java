package com.xuecheng;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class ManageMediaApplication {
    public static void main(String[] args) {
        SpringApplication.run(ManageMediaApplication.class, args);
    }
}
