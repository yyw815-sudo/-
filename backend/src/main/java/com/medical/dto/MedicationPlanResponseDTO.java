package com.medical.dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class MedicationPlanResponseDTO {
    private Long planId;
    private Long userId;
    private String userName;
    private Long recordId;
    private String diseaseName;
    private Long medicineId;
    private String medicineName;
    private String specification;
    private String dosage;
    private String frequency;
    private Integer timesPerDay;
    private Integer intervalHours;
    private LocalDate startDate;
    private LocalDate endDate;
    private String purpose;
    private Integer status;
    private String statusDescription;
    private String dailyProgress;
    private List<String> reminderTimes;
    private LocalDateTime createTime;
}
