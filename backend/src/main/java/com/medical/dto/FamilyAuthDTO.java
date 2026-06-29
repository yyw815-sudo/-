package com.medical.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FamilyAuthDTO {

    @NotNull(message = "成员ID不能为空")
    private Long memberId;

    private Integer viewMedicalRecord = 0;

    private Integer viewMedication = 1;

    private Integer viewStatistics = 1;

    private Integer receiveMissAlert = 1;

    private Integer receiveEmergency = 1;

    private Integer disconnAlert = 0;
}