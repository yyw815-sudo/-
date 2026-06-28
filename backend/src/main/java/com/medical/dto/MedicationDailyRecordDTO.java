package com.medical.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class MedicationDailyRecordDTO {
    private Long takeId;
    private Long planId;
    private Long userId;
    private Long medicineId;
    private String medicineName;
    private String dosage;
    private LocalDateTime scheduledTime;
    private LocalDateTime takeTime;
    private Integer status;
    private String statusText;
    private String photoUrl;
    private String aiResult;
    private BigDecimal aiAccuracy;
}
