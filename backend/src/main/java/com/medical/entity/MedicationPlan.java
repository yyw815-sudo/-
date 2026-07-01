package com.medical.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "medicationplan")
public class MedicationPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "plan_id")
    private Long planId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "record_id", nullable = false)
    private Long recordId;

    @Column(name = "medicine_id", nullable = false)
    private Long medicineId;

    @Column(name = "dosage", nullable = false, length = 50)
    private String dosage;

    @Column(name = "frequency", nullable = false, length = 50)
    private String frequency;

    @Column(name = "times_per_day", nullable = false)
    private Integer timesPerDay;

    @Column(name = "interval_hours")
    private Integer intervalHours;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "purpose", columnDefinition = "TEXT")
    private String purpose;

    @Column(name = "status")
    private Integer status;

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