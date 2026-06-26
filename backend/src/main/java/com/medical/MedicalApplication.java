package com.medical;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 慢性病用药智能提醒系统 - 启动类
 */
@SpringBootApplication
@EnableScheduling  // 启用定时任务（用于提醒、统计等）
public class MedicalApplication {

    public static void main(String[] args) {
        SpringApplication.run(MedicalApplication.class, args);
        System.out.println("========================================");
        System.out.println("慢性病用药智能提醒系统启动成功！");
        System.out.println("访问地址：http://localhost:8080/api");
        System.out.println("========================================");
    }
}
