package com.example.userservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import jakarta.annotation.PostConstruct;
import java.util.TimeZone;

@SpringBootApplication
@EnableJpaAuditing
public class UserServiceApplication {
    
    @PostConstruct
    public void init() {
        // JVM 시간대를 한국 시간대로 설정
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
    }
    
    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
}