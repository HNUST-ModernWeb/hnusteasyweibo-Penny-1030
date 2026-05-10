package com.social;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.social.mapper")
public class SocialApplication {
    public static void main(String[] args) {
        SpringApplication.run(SocialApplication.class, args);
        System.out.println("=========================================");
        System.out.println("社交分享平台后端启动成功！");
        System.out.println("API文档: http://localhost:8080/swagger-ui.html");
        System.out.println("=========================================");
    }
}