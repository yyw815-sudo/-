package com.medical.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 每日服药记录实体类
 * 映射 medicationrecord 表
 * 状态：0:待服药 1:已服药 2:漏服
 */
@Data
@Entity
@Table(name = "medicationrecord")
public class MedicationDailyRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "take_id")
    private Long takeId;

    @Column(name = "plan_id", nullable = false)
    private Long planId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "medicine_id")
    private Long medicineId;

    @Column(name = "scheduled_time", nullable = false)
    private LocalDateTime scheduledTime;

    @Column(name = "take_time")
    private LocalDateTime takeTime;

    @Column(name = "photo_url", length = 500)
    private String photoUrl;

    @Column(name = "ai_result", length = 500)
    private String aiResult;

    @Column(name = "ai_accuracy", precision = 5, scale = 2)
    private BigDecimal aiAccuracy;

    @Column(name = "status")
    private Integer status = 0;

    @Column(name = "create_time", nullable = false, updatable = false)
    private LocalDateTime createTime;

    @Column(name = "update_time", nullable = false)
    private LocalDateTime updateTime;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        updateTime = LocalDateTime.now();
        if (status == null) status = 0;
    }

    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }
}
