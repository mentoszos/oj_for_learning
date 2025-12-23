package com.codecollab.oj.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Knife4jConfig {
    @Bean
    public OpenAPI springShopOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("CodeCollab OJ 接口文档")
                        .description("实时协作面试与AI辅助评测系统 API")
                        .version("v1.0"));
    }
}