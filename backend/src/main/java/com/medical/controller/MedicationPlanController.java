package com.medical.controller;

import com.medical.dto.*;
import com.medical.service.MedicationPlanService;
import com.medical.service.MedicationDailyRecordService;
import com.medical.service.BaiduImageService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/plan")
public class MedicationPlanController {

    private final MedicationPlanService planService;
    private final MedicationDailyRecordService dailyRecordService;
    private final BaiduImageService baiduImageService;

    public MedicationPlanController(MedicationPlanService planService,
                                    MedicationDailyRecordService dailyRecordService,
                                    BaiduImageService baiduImageService) {
        this.planService = planService;
        this.dailyRecordService = dailyRecordService;
        this.baiduImageService = baiduImageService;
    }

    // ==================== 计划管理 ====================

    @GetMapping("/{planId}")
    public Result<MedicationPlanResponseDTO> getPlanById(@PathVariable Long planId) {
        MedicationPlanResponseDTO plan = planService.getPlanById(planId);
        return plan != null ? Result.success(plan) : Result.error(404, "计划不存在");
    }

    @GetMapping("/list")
    public Result<List<MedicationPlanResponseDTO>> getAllPlans(@RequestParam(required = false) Long userId) {
        if (userId == null) return Result.error(400, "缺少 userId 参数");
        return Result.success(planService.getPlansByUserId(userId));
    }

    @GetMapping("/user/{userId}")
    public Result<List<MedicationPlanResponseDTO>> getPlansByUserId(@PathVariable Long userId) {
        return Result.success(planService.getPlansByUserId(userId));
    }

    @GetMapping("/record/{recordId}")
    public Result<List<MedicationPlanResponseDTO>> getPlansByRecordId(@PathVariable Long recordId) {
        return Result.success(planService.getPlansByRecordId(recordId));
    }

    @GetMapping("/record/{recordId}/count")
    public Result<Long> countPlansByRecordId(@PathVariable Long recordId) {
        return Result.success(planService.countPlansByRecordId(recordId));
    }

    @GetMapping("/user/{userId}/status/{status}")
    public Result<List<MedicationPlanResponseDTO>> getPlansByStatus(@PathVariable Long userId, @PathVariable Integer status) {
        return Result.success(planService.getHistoryPlansByStatus(userId, status));
    }

    @GetMapping("/user/{userId}/history/{status}")
    public Result<List<MedicationPlanResponseDTO>> getHistoryPlans(@PathVariable Long userId, @PathVariable Integer status) {
        return Result.success(planService.getHistoryPlansByStatus(userId, status));
    }

    @PutMapping("/{planId}")
    public Result<MedicationPlanResponseDTO> updatePlan(@PathVariable Long planId,
                                                         @RequestBody MedicationPlanUpdateDTO updateDTO) {
        return Result.success(planService.updatePlan(planId, updateDTO));
    }

    @DeleteMapping("/{planId}")
    public Result<?> deletePlan(@PathVariable Long planId) {
        planService.deletePlan(planId);
        return Result.success("删除成功");
    }

    // ==================== 每日记录 ====================

    @GetMapping("/daily/plan/{planId}")
    public Result<List<MedicationDailyRecordDTO>> getDailyByPlanId(@PathVariable Long planId) {
        return Result.success(dailyRecordService.getByPlanId(planId));
    }

    @GetMapping("/daily/user/{userId}/date/{date}")
    public Result<List<MedicationDailyRecordDTO>> getDailyByUserIdAndDate(
            @PathVariable Long userId, @PathVariable String date) {
        LocalDate localDate = LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE);
        return Result.success(dailyRecordService.getByUserIdAndDate(userId, localDate));
    }

    @GetMapping("/daily/user/{userId}/range")
    public Result<List<MedicationDailyRecordDTO>> getDailyByUserIdAndDateRange(
            @PathVariable Long userId,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        LocalDate start = LocalDate.parse(startDate, DateTimeFormatter.ISO_LOCAL_DATE);
        LocalDate end = LocalDate.parse(endDate, DateTimeFormatter.ISO_LOCAL_DATE);
        return Result.success(dailyRecordService.getByUserIdAndDateRange(userId, start, end));
    }

    @PostMapping("/daily/{takeId}/missed")
    public Result<MedicationDailyRecordDTO> markAsMissed(@PathVariable Long takeId) {
        return Result.success(dailyRecordService.markAsMissed(takeId));
    }

    /** 上传药片照片并确认服药 */
    @PostMapping(value = "/daily/{takeId}/photo", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<MedicationDailyRecordDTO> uploadPhoto(
            @PathVariable Long takeId,
            @RequestParam("file") MultipartFile file) {

        if (file.isEmpty()) return Result.error(400, "请选择图片");

        try {
            byte[] imageBytes = file.getBytes();

            // 保存文件
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            String uploadDir = System.getProperty("user.dir") + "/uploads/";
            java.io.File dir = new java.io.File(uploadDir);
            if (!dir.exists()) dir.mkdirs();
            java.nio.file.Files.write(java.nio.file.Paths.get(uploadDir + fileName), imageBytes);

            String photoUrl = "/uploads/" + fileName;

            // AI识别
            Map<String, Object> aiResult = baiduImageService.checkIsMedicine(imageBytes);
            String aiResultText = (String) aiResult.get("keyword");
            BigDecimal aiAccuracy = BigDecimal.valueOf((Double) aiResult.get("accuracy"));

            return Result.success(dailyRecordService.markAsTaken(takeId, photoUrl, aiResultText, aiAccuracy));
        } catch (Exception e) {
            return Result.error(500, "上传或识别失败: " + e.getMessage());
        }
    }
}
