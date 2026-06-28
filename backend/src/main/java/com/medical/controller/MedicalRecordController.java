package com.medical.controller;

import com.medical.dto.MedicationPlanResponseDTO;
import com.medical.dto.Result;
import com.medical.entity.MedicalRecord;
import com.medical.repository.MedicalRecordRepository;
import com.medical.service.MedicalRecordService;
import com.medical.service.MedicationPlanService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/record")
public class MedicalRecordController {

    private final MedicalRecordService medicalRecordService;
    private final MedicationPlanService medicationPlanService;
    private final MedicalRecordRepository medicalRecordRepository;

    public MedicalRecordController(MedicalRecordService medicalRecordService,
                                   MedicationPlanService medicationPlanService,
                                   MedicalRecordRepository medicalRecordRepository) {
        this.medicalRecordService = medicalRecordService;
        this.medicationPlanService = medicationPlanService;
        this.medicalRecordRepository = medicalRecordRepository;
    }

    @GetMapping("/user/{userId}")
    public Result<List<MedicalRecord>> getRecordsByUserId(@PathVariable Long userId) {
        return Result.success(medicalRecordRepository.findByUserIdOrderByRecordDateDesc(userId));
    }

    @GetMapping("/{recordId}")
    public Result<MedicalRecord> getRecordById(@PathVariable Long recordId) {
        return medicalRecordRepository.findById(recordId)
                .map(Result::success)
                .orElse(Result.error(404, "病历不存在"));
    }

    @GetMapping("/user/{userId}/page")
    public Result<Page<MedicalRecord>> getRecordsPage(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        LocalDate start = startDate != null && !startDate.isBlank() ? LocalDate.parse(startDate) : null;
        LocalDate end = endDate != null && !endDate.isBlank() ? LocalDate.parse(endDate) : null;
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "recordDate"));

        Page<MedicalRecord> result = medicalRecordService.searchRecords(
                userId,
                keyword.isBlank() ? null : keyword,
                start, end,
                pageable
        );
        return Result.success(result);
    }

    @PostMapping
    public Result<MedicalRecord> createRecord(@RequestBody MedicalRecord record) {
        if (record.getUserId() == null) {
            return Result.error(400, "用户ID不能为空");
        }
        if (record.getDiseaseName() == null || record.getDiseaseName().isBlank()) {
            return Result.error(400, "疾病名称不能为空");
        }
        MedicalRecord saved = medicalRecordService.createRecord(record);
        return Result.success(saved);
    }

    @PutMapping("/{recordId}")
    public Result<MedicalRecord> updateRecord(@PathVariable Long recordId,
                                               @RequestBody MedicalRecord record) {
        try {
            MedicalRecord updated = medicalRecordService.updateRecord(recordId, record);
            return Result.success(updated);
        } catch (RuntimeException e) {
            return Result.error(404, e.getMessage());
        }
    }

    @DeleteMapping("/{recordId}")
    public Result<Map<String, Object>> deleteRecord(@PathVariable Long recordId) {
        try {
            List<MedicationPlanResponseDTO> affectedPlans = medicationPlanService.getPlansByRecordId(recordId);
            if (!affectedPlans.isEmpty()) {
                return Result.error(409, "该病历有关联用药计划，请先删除用药计划");
            }
            medicalRecordService.deleteRecord(recordId);
            return Result.success(Map.of("deleted", recordId));
        } catch (RuntimeException e) {
            return Result.error(404, e.getMessage());
        }
    }

    @DeleteMapping("/{recordId}/cascade")
    public Result<Map<String, Object>> deleteRecordWithPlans(@PathVariable Long recordId) {
        try {
            List<MedicationPlanResponseDTO> affectedPlans = medicationPlanService.deletePlansByRecordId(recordId);
            medicalRecordService.deleteRecord(recordId);
            return Result.success(Map.of(
                    "deletedRecordId", recordId,
                    "deletedPlans", affectedPlans.size()
            ));
        } catch (RuntimeException e) {
            return Result.error(404, e.getMessage());
        }
    }

    @PostMapping("/ocr/recognize")
    public Result<Map<String, Object>> recognizePrescription(@RequestParam("file") MultipartFile file) {
        try {
            Map<String, Object> result = medicalRecordService.recognizeTextAndParseFull(file.getBytes());
            MedicalRecord record = (MedicalRecord) result.get("record");
            record.setRecordId(null);
            result.put("record", record);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(500, "文字识别失败: " + e.getMessage());
        }
    }
}
