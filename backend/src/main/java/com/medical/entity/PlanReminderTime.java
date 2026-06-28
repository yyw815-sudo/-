package com.medical.entity;

import jakarta.persistence.*;
import java.time.LocalTime;
import java.time.LocalDateTime;

@Entity
@Table(name = "plan_reminder_time")
public class PlanReminderTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "time_id")
    private Long timeId;

    @Column(name = "plan_id")
    private Long planId;

    @Column(name = "suggested_time")
    private LocalTime suggestedTime;

    @Column(name = "reason", length = 100)
    private String reason;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    public PlanReminderTime() {}

    public Long getTimeId() {
        return timeId;
    }

    public void setTimeId(Long timeId) {
        this.timeId = timeId;
    }

    public Long getPlanId() {
        return planId;
    }

    public void setPlanId(Long planId) {
        this.planId = planId;
    }

    public LocalTime getSuggestedTime() {
        return suggestedTime;
    }

    public void setSuggestedTime(LocalTime suggestedTime) {
        this.suggestedTime = suggestedTime;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}
