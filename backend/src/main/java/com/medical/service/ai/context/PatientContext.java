package com.medical.service.ai.context;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class PatientContext {
    private Integer age;
    private String gender;
    private List<String> diagnoses;
    private String medicines; // 药品信息（与处理意见分离）
    private String presentHistory; // 现病史
    private String pastHistory;    // 既往病史
    private List<RiskTag> riskTags;
    private RiskLevel riskLevel;

    public enum RiskLevel {
        LOW, MEDIUM, HIGH, CRITICAL
    }

    public PatientContext() {
        this.riskTags = new ArrayList<>();
        this.riskLevel = RiskLevel.LOW;
    }

    public void addRiskTag(RiskTag tag) {
        this.riskTags.add(tag);
    }

    public List<RiskTag> getDiseaseRiskTags() {
        return riskTags.stream()
                .filter(t -> "疾病".equals(t.getType()))
                .collect(Collectors.toList());
    }

    public List<RiskTag> getAllergyTags() {
        return riskTags.stream()
                .filter(t -> "过敏".equals(t.getType()))
                .collect(Collectors.toList());
    }

    public boolean isEmpty() {
        return age == null && gender == null && diagnoses == null;
    }
}
