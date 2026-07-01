package com.medical.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "api_config")
public class ApiConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "config_id")
    private Long configId;

    @Column(name = "api_name", nullable = false, length = 100)
    private String apiName;

    @Column(name = "api_type", nullable = false, length = 50)
    private String apiType;

    @Column(name = "endpoint", length = 255)
    private String endpoint;

    @Column(name = "app_key", length = 255)
    private String appKey;

    @Column(name = "app_secret", length = 255)
    private String appSecret;

    @Column(name = "status")
    private Integer status;

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

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