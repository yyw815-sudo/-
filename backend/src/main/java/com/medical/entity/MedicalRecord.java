package com.medical.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 电子病历实体类
 * 映射 medicalrecord 表
 */
@Data
@Entity
@Table(name = "medicalrecord")
public class MedicalRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "record_id")
    private Long recordId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "disease_name", nullable = false, length = 100)
    private String diseaseName;

    @Column(name = "diagnosis", columnDefinition = "TEXT")
    private String diagnosis;

    @Column(name = "prescription", columnDefinition = "TEXT")
    private String prescription;

    @Column(name = "doctor", length = 50)
    private String doctor;

    @Column(name = "hospital", length = 100)
    private String hospital;

    @Column(name = "record_date")
    private LocalDate recordDate;

    // ====== 新增字段 2026-06-28 ======
    @Column(name = "gender", length = 10)
    private String gender;           // 性别

    @Column(name = "age")
    private Integer age;             // 年龄

    @Column(name = "past_history", columnDefinition = "TEXT")
    private String pastHistory;      // 既往病史

    @Column(name = "present_history", columnDefinition = "TEXT")
    private String presentHistory;   // 现病史

    @Column(name = "chief_complaint", columnDefinition = "TEXT")
    private String chiefComplaint;   // 主诉

    @Column(name = "treatment", columnDefinition = "TEXT")
    private String treatment;        // 处理意见

    @Column(name = "create_time", nullable = false, updatable = false)
    private LocalDateTime createTime;

    @Column(name = "update_time", nullable = false)
    private LocalDateTime updateTime;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        updateTime = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }
}
