package com.medical.service.ai;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class RuleResult {
    private int riskScore;
    private RiskLevel riskLevel;
    private List<String> warnings;
    private List<RuleEngine.ConflictInfo> conflicts;
    private List<String> riskTags;
    private boolean hasConflict;
    private boolean hasAllergyRisk;
    private boolean hasDiseaseRisk;

    public enum RiskLevel {
        LOW(0, 25), MEDIUM(26, 50), HIGH(51, 75), CRITICAL(76, 100);
        private final int minScore, maxScore;
        RiskLevel(int minScore, int maxScore) { this.minScore = minScore; this.maxScore = maxScore; }
        public static RiskLevel fromScore(int score) {
            if (score <= 25) return LOW; if (score <= 50) return MEDIUM; if (score <= 75) return HIGH;
            return CRITICAL;
        }
    }

    public RuleResult() {
        this.warnings = new ArrayList<>();
        this.conflicts = new ArrayList<>();
        this.riskTags = new ArrayList<>();
        this.riskScore = 0;
        this.hasConflict = false;
        this.hasAllergyRisk = false;
        this.hasDiseaseRisk = false;
    }

    public void addRiskScore(int score) {
        this.riskScore = Math.min(100, this.riskScore + score);
        this.riskLevel = RiskLevel.fromScore(this.riskScore);
    }

    public void addWarning(String warning) { this.warnings.add(warning); }
    public void addConflict(RuleEngine.ConflictInfo conflict) { this.conflicts.add(conflict); this.hasConflict = true; }
    public void addRiskTag(String tag) { this.riskTags.add(tag); }
}
