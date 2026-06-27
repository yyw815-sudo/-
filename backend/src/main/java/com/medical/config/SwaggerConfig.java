package com.medical.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;

/**
 * Swagger配置类
 */
@Configuration
public class SwaggerConfig {
    
    @Bean
    public OpenAPI medicalOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("慢性病用药智能提醒系统API")
                        .description("慢性病用药智能提醒系统后端接口文档")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("Medical Team")
                                .email("medical@example.com")));
    }
}