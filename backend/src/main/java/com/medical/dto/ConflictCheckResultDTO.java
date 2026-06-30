package com.medical.dto;

import lombok.Data;
import java.util.List;

@Data
public class ConflictCheckResultDTO {
    private Integer recordCount;
    private Integer totalDrugs;
    private List<String> allMedicines;
    private Boolean hasConflict;
    private List<ConflictItem> conflicts;
    private List<String> warnings;
    private String riskLevel;
    private Integer riskScore;

    @Data
    public static class ConflictItem {
        private String drugA;
        private String drugB;
        private String level;
        private String description;
        private String suggestion;
    }
}
