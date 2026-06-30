package com.medical.dto;

import com.medical.service.ai.RuleEngine;
import lombok.Data;
import java.util.List;

@Data
public class AiAnalysisResultDTO {
    private List<MedicationPlanResponseDTO> plans;
    private List<String> warnings;
    private List<String> errors;
    private Integer riskScore;
    private String riskLevel;
    private Integer version;
    private Integer planCount;
    private Integer totalDrugs;
    private Boolean hasConflict;
    private List<RuleEngine.ConflictInfo> conflicts;
}
