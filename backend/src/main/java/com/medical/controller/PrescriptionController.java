package com.medical.controller;

import com.medical.dto.Result;
import com.medical.dto.MedicationPlanResponseDTO;
import com.medical.entity.MedicalRecord;
import com.medical.repository.MedicalRecordRepository;
import com.medical.service.MedicationPlanService;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/prescription")
public class PrescriptionController {

    private final MedicationPlanService planService;
    private final MedicalRecordRepository medicalRecordRepository;

    public PrescriptionController(MedicationPlanService planService,
                                  MedicalRecordRepository medicalRecordRepository) {
        this.planService = planService;
        this.medicalRecordRepository = medicalRecordRepository;
    }

    /** AI分析处方入口：读取病历处方文本 → AI分析 → 创建计划 */
    @PostMapping("/analyze")
    public Result<List<MedicationPlanResponseDTO>> analyzePrescription(@RequestBody Map<String, Object> params) {
        Object userIdObj = params.get("userId");
        Object recordIdObj = params.get("recordId");
        if (userIdObj == null || recordIdObj == null) {
            return Result.error(400, "缺少必填参数：userId、recordId");
        }

        Long userId;
        Long recordId;
        try {
            userId = Long.valueOf(userIdObj.toString());
            recordId = Long.valueOf(recordIdObj.toString());
        } catch (NumberFormatException e) {
            return Result.error(400, "参数格式错误：userId、recordId 必须为数字");
        }

        MedicalRecord record = medicalRecordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("病历不存在"));

        if (record.getPrescription() == null || record.getPrescription().isEmpty()) {
            return Result.error(400, "该病历没有处方信息");
        }

        // 检查是否已有计划
        long existingCount = planService.countPlansByRecordId(recordId);
        if (existingCount > 0) {
            return Result.error(400, "该病历已有 " + existingCount + " 个用药计划，请勿重复分析");
        }

        List<MedicationPlanResponseDTO> plans = planService.createPlansFromAi(userId, recordId, record.getPrescription());
        return Result.success(plans);
    }
}
