package com.medical.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 药品信息实体类
 * 映射 medicine 表
 */
@Data
@Entity
@Table(name = "medicine")
public class Medicine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "medicine_id")
    private Long medicineId;

    @Column(name = "medicine_name", nullable = false, length = 100)
    private String medicineName;

    @Column(name = "specification", length = 100)
    private String specification;

    @Column(name = "dosage_form", length = 50)
    private String dosageForm;

    @Column(name = "manufacturer", length = 100)
    private String manufacturer;

    @Column(name = "indication", columnDefinition = "TEXT")
    private String indication;

    @Column(name = "contraindication", columnDefinition = "TEXT")
    private String contraindication;

    @Column(name = "precautions", columnDefinition = "TEXT")
    private String precautions;

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
