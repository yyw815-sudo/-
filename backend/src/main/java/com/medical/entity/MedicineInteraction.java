package com.medical.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 药品冲突检测实体类
 * 映射 medicineinteraction 表
 */
@Data
@Entity
@Table(name = "medicineinteraction")
public class MedicineInteraction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "interaction_id")
    private Long interactionId;

    @Column(name = "medicine_id_a", nullable = false)
    private Long medicineIdA;

    @Column(name = "medicine_id_b", nullable = false)
    private Long medicineIdB;

    @Column(name = "severity", length = 20)
    private String severity;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "create_time", nullable = false, updatable = false)
    private LocalDateTime createTime;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
    }
}
