package com.medical.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class MedicationPlanUpdateDTO {
    private String dosage;
    private String frequency;
    private Integer timesPerDay;
    private Integer intervalHours;
    private LocalDate startDate;
    private LocalDate endDate;
    private String purpose;
    private Integer status;
}
