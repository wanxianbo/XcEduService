package com.xuecheng;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ManageOrderApplication {
    public static void main(String[] args) {
        SpringApplication.run(ManageOrderApplication.class, args);
    }
}
