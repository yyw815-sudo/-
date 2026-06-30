package com.medical.service.ai;

import lombok.Data;

@Data
public class Recommendation {
    private RecommendationType type;
    private Priority priority;
    private String title;
    private String content;
    private String medicineName;
    private String sourceRule;

    public enum RecommendationType {
        REQUIREMENT, WARNING, ADVICE, SUGGESTION
    }

    public enum Priority {
        CRITICAL, HIGH, MEDIUM, LOW
    }
}
