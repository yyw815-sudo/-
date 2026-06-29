package com.medical.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "familymember")
public class FamilyMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long memberId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "family_user_id", nullable = false)
    private Long familyUserId;

    @Column(name = "realname", length = 50)
    private String realname;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "relation", length = 50)
    private String relation;

    @Column(name = "permission_level", length = 20)
    private String permissionLevel = "basic";

    @Column(name = "status")
    private Integer status = 1;

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