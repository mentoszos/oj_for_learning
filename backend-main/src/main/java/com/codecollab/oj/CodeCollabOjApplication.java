package com.codecollab.oj;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.codecollab.oj"})
@MapperScan("com.codecollab.oj.mapper")
public class CodeCollabOjApplication {
    public static void main(String[] args) {
        SpringApplication.run(CodeCollabOjApplication.class, args);
        
    }
}

