package com.medical.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 计划提醒时间实体类
 * 映射 plan_reminder_time 表
 */
@Data
@Entity
@Table(name = "plan_reminder_time")
public class PlanReminderTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "time_id")
    private Long timeId;

    @Column(name = "plan_id", nullable = false)
    private Long planId;

    @Column(name = "suggested_time", nullable = false)
    private LocalTime suggestedTime;

    @Column(name = "reason", length = 255)
    private String reason;

    @Column(name = "create_time", nullable = false, updatable = false)
    private LocalDateTime createTime;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
    }
}
