package com.medical;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 慢性病用药智能提醒系统 - 主启动类
 * 
 * @author Medical Team
 */
@SpringBootApplication
@EnableScheduling
public class MedicalApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(MedicalApplication.class, args);
        System.out.println("==========================================");
        System.out.println("  慢性病用药智能提醒系统启动成功！");
        System.out.println("  访问地址: http://localhost:8080/api");
        System.out.println("  Swagger文档: http://localhost:8080/api/swagger-ui.html");
        System.out.println("==========================================");
    }
}