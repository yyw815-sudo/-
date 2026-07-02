package com.medical.controller;

import com.medical.dto.Result;
import com.medical.entity.AiMedicationPlan;
import com.medical.entity.OcrReview;
import com.medical.entity.PillRecognitionReview;
import com.medical.service.AiCenterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/ai")
public class AiCenterController {

    @Autowired
    private AiCenterService aiCenterService;

    @GetMapping("/statistics")
    public Result<Map<String, Object>> getStatistics() {
        Map<String, Object> statistics = aiCenterService.getStatistics();
        return Result.success("查询成功", statistics);
    }

    @GetMapping("/medication-plans")
    public Result<Map<String, Object>> getMedicationPlanList(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        Page<AiMedicationPlan> page = aiCenterService.getMedicationPlanPage(pageNum, pageSize, keyword, status);
        Map<String, Object> data = new HashMap<>();
        data.put("list", page.getContent());
        data.put("total", page.getTotalElements());
        data.put("pageNum", pageNum);
        data.put("pageSize", pageSize);
        return Result.success("查询成功", data);
    }

    @GetMapping("/medication-plans/{id}")
    public Result<AiMedicationPlan> getMedicationPlanDetail(@PathVariable Long id) {
        try {
            AiMedicationPlan plan = aiCenterService.getMedicationPlanById(id);
            return Result.success(plan);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/medication-plans")
    public Result<AiMedicationPlan> createMedicationPlan(@RequestBody AiMedicationPlan plan) {
        AiMedicationPlan saved = aiCenterService.createMedicationPlan(plan);
        return Result.success("创建成功", saved);
    }

    @PutMapping("/medication-plans/{id}")
    public Result<AiMedicationPlan> updateMedicationPlan(@PathVariable Long id, @RequestBody AiMedicationPlan plan) {
        try {
            AiMedicationPlan updated = aiCenterService.updateMedicationPlan(id, plan);
            return Result.success("更新成功", updated);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/medication-plans/{id}")
    public Result<String> deleteMedicationPlan(@PathVariable Long id) {
        try {
            aiCenterService.deleteMedicationPlan(id);
            return Result.success("删除成功");
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/ocr-reviews")
    public Result<Map<String, Object>> getOcrReviewList(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        Page<OcrReview> page = aiCenterService.getOcrReviewPage(pageNum, pageSize, keyword, status);
        Map<String, Object> data = new HashMap<>();
        data.put("list", page.getContent());
        data.put("total", page.getTotalElements());
        data.put("pageNum", pageNum);
        data.put("pageSize", pageSize);
        return Result.success("查询成功", data);
    }

    @GetMapping("/ocr-reviews/{id}")
    public Result<OcrReview> getOcrReviewDetail(@PathVariable Long id) {
        try {
            OcrReview ocr = aiCenterService.getOcrReviewById(id);
            return Result.success(ocr);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/ocr-reviews/{id}/review")
    public Result<OcrReview> reviewOcr(@PathVariable Long id, @RequestBody Map<String, Object> reviewData) {
        try {
            Integer status = (Integer) reviewData.get("status");
            Long reviewerId = reviewData.get("reviewerId") != null ? ((Number) reviewData.get("reviewerId")).longValue() : null;
            String comment = (String) reviewData.get("comment");
            OcrReview updated = aiCenterService.reviewOcr(id, status, reviewerId, comment);
            return Result.success("审核成功", updated);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/ocr-reviews/{id}")
    public Result<String> deleteOcrReview(@PathVariable Long id) {
        try {
            aiCenterService.deleteOcrReview(id);
            return Result.success("删除成功");
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/pill-reviews")
    public Result<Map<String, Object>> getPillReviewList(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        Page<PillRecognitionReview> page = aiCenterService.getPillReviewPage(pageNum, pageSize, keyword, status);
        Map<String, Object> data = new HashMap<>();
        data.put("list", page.getContent());
        data.put("total", page.getTotalElements());
        data.put("pageNum", pageNum);
        data.put("pageSize", pageSize);
        return Result.success("查询成功", data);
    }

    @GetMapping("/pill-reviews/{id}")
    public Result<PillRecognitionReview> getPillReviewDetail(@PathVariable Long id) {
        try {
            PillRecognitionReview pill = aiCenterService.getPillReviewById(id);
            return Result.success(pill);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/pill-reviews/{id}/review")
    public Result<PillRecognitionReview> reviewPill(@PathVariable Long id, @RequestBody Map<String, Object> reviewData) {
        try {
            Integer status = (Integer) reviewData.get("status");
            Long reviewerId = reviewData.get("reviewerId") != null ? ((Number) reviewData.get("reviewerId")).longValue() : null;
            String comment = (String) reviewData.get("comment");
            PillRecognitionReview updated = aiCenterService.reviewPill(id, status, reviewerId, comment);
            return Result.success("审核成功", updated);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/pill-reviews/{id}")
    public Result<String> deletePillReview(@PathVariable Long id) {
        try {
            aiCenterService.deletePillReview(id);
            return Result.success("删除成功");
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }
}