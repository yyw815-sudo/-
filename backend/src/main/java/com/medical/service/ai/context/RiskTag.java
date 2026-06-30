package com.medical.service.ai.context;

import lombok.Data;

@Data
public class RiskTag {
    private String type;
    private String name;
    private RiskLevel level;
    private String affectsDrugCategory;
    private String remark;

    public enum RiskLevel {
        CRITICAL, HIGH, MEDIUM, LOW
    }
}
