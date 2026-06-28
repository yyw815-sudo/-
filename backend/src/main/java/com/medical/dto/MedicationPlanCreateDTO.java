package com.medical.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class MedicationPlanCreateDTO {

    @NotNull(message = "用户ID不能为空")
    private Long userId;

    private Long recordId;
    private Long medicineId;

    @NotBlank(message = "用量不能为空")
    private String dosage;

    @NotBlank(message = "频率不能为空")
    private String frequency;

    @NotNull(message = "每日次数不能为空")
    private Integer timesPerDay;

    private Integer intervalHours;
    private LocalDate startDate;
    private LocalDate endDate;
    private String purpose;

    private List<ReminderTimeInput> reminderTimes;
}
