package com.medical.controller;

import com.medical.dto.AiAnalysisResultDTO;
import com.medical.dto.ConflictCheckResultDTO;
import com.medical.dto.Result;
import com.medical.dto.MedicationPlanResponseDTO;
import com.medical.entity.MedicalRecord;
import com.medical.repository.MedicalRecordRepository;
import com.medical.service.MedicationPlanService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@Slf4j
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

    /** AI分析处方入口 */
    @PostMapping("/analyze")
    public Result<AiAnalysisResultDTO> analyzePrescription(@RequestBody Map<String, Object> params) {
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

        AiAnalysisResultDTO result = planService.createPlansFromAi(userId, recordId, null, null);
        return Result.success(result);
    }

    /** 获取病历的最新AI分析结果 */
    @GetMapping("/analysis/{recordId}")
    public Result<AiAnalysisResultDTO> getAnalysisResult(@PathVariable Long recordId) {
        AiAnalysisResultDTO result = planService.getLatestAnalysisResult(recordId);
        if (result == null) return Result.error(404, "该病历暂无AI分析结果");
        return Result.success(result);
    }

    /** 获取用户所有病历的AI分析结果 */
    @GetMapping("/analysis/user/{userId}")
    public Result<List<AiAnalysisResultDTO>> getAnalysisResultsByUserId(@PathVariable Long userId) {
        List<AiAnalysisResultDTO> results = planService.getAllAnalysisResultsByUserId(userId);
        return Result.success(results);
    }

    /** 多病历汇总后的冲突检测接口 */
    @PostMapping("/checkConflicts")
    public Result<ConflictCheckResultDTO> checkConflicts(@RequestBody Map<String, Object> params) {
        log.info("收到冲突检测请求: {}", params);
        Object recordIdsObj = params.get("recordIds");
        if (recordIdsObj == null) return Result.error(400, "缺少必填参数：recordIds");

        List<Long> recordIds;
        try {
            if (recordIdsObj instanceof List) {
                recordIds = ((List<?>) recordIdsObj).stream().map(id -> Long.valueOf(id.toString())).toList();
            } else {
                return Result.error(400, "参数格式错误：recordIds 必须为数组");
            }
        } catch (NumberFormatException e) {
            return Result.error(400, "参数格式错误：recordIds 必须为数字数组");
        }

        ConflictCheckResultDTO result = planService.checkCrossRecordConflicts(recordIds);
        return Result.success(result);
    }
}
