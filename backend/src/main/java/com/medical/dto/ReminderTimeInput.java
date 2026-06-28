package com.medical.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ReminderTimeInput {

    @NotBlank(message = "建议时间不能为空")
    @Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d(:[0-5]\\d)?$", message = "时间格式必须为 HH:mm 或 HH:mm:ss")
    private String suggestedTime;

    private String reason;
}
