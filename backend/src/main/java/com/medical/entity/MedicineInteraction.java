package com.medical.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "medicineinteraction")
public class MedicineInteraction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "interaction_id")
    private Long interactionId;

    @Column(name = "medicine_a")
    private Long medicineA;

    @Column(name = "medicine_b")
    private Long medicineB;

    @Column(name = "conflict_level", length = 20)
    private String conflictLevel;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "suggestion", columnDefinition = "TEXT")
    private String suggestion;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    public MedicineInteraction() {}

    public Long getInteractionId() {
        return interactionId;
    }

    public void setInteractionId(Long interactionId) {
        this.interactionId = interactionId;
    }

    public Long getMedicineA() {
        return medicineA;
    }

    public void setMedicineA(Long medicineA) {
        this.medicineA = medicineA;
    }

    public Long getMedicineB() {
        return medicineB;
    }

    public void setMedicineB(Long medicineB) {
        this.medicineB = medicineB;
    }

    public String getConflictLevel() {
        return conflictLevel;
    }

    public void setConflictLevel(String conflictLevel) {
        this.conflictLevel = conflictLevel;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSuggestion() {
        return suggestion;
    }

    public void setSuggestion(String suggestion) {
        this.suggestion = suggestion;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}
