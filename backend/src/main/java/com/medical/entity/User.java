package com.medical.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;

/**
 * 用户实体类
 */
@Data
@Entity
@Table(name = "user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "user_name", nullable = false, unique = true, length = 50)
    private String userName;

    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Column(name = "realname", length = 50)
    @JsonProperty("realName")
    private String realname;

    @Column(name = "gender")
    private Integer gender;

    @Column(name = "birthday")
    private LocalDate birthday;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "create_time", nullable = false, updatable = false)
    private LocalDateTime createTime;

    @Column(name = "update_time", nullable = false)
    private LocalDateTime updateTime;

    @JsonProperty("age")
    public Integer getAge() {
        if (birthday == null) {
            return null;
        }
        return Period.between(birthday, LocalDate.now()).getYears();
    }

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
