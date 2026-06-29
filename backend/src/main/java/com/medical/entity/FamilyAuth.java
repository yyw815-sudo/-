package com.medical.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "familyauth")
public class FamilyAuth {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "auth_id")
    private Long authId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "view_medical_record")
    private Integer viewMedicalRecord = 0;

    @Column(name = "view_medication")
    private Integer viewMedication = 1;

    @Column(name = "view_statistics")
    private Integer viewStatistics = 1;

    @Column(name = "receive_miss_alert")
    private Integer receiveMissAlert = 1;

    @Column(name = "receive_emergency")
    private Integer receiveEmergency = 1;

    @Column(name = "disconn_alert")
    private Integer disconnAlert = 0;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @Column(name = "update_time")
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