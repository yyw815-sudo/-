package com.medical.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "reminderlog")
public class ReminderLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Long logId;

    @Column(name = "reminder_id", nullable = false)
    private Long reminderId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "plan_id")
    private Long planId;

    @Column(name = "channel", length = 20)
    private String channel;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "send_time")
    private LocalDateTime sendTime;

    @Column(name = "receive_time")
    private LocalDateTime receiveTime;

    @Column(name = "status", length = 20)
    private String status = "pending";

    @Column(name = "response", columnDefinition = "TEXT")
    private String response;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @Transient
    private String remark;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
    }
}
