package com.medical.controller;

import com.medical.dto.Result;
import com.medical.entity.MedicationPlan;
import com.medical.entity.Medicine;
import com.medical.entity.PlanReminderTime;
import com.medical.service.MedicationPlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/medicationplan")
public class MedicationPlanController {

    @Autowired
    private MedicationPlanService medicationPlanService;

    @GetMapping("/user/{userId}")
    public Result<List<MedicationPlan>> getPlansByUserId(@PathVariable Long userId) {
        List<MedicationPlan> plans = medicationPlanService.getPlansByUserId(userId);
        return Result.success(plans);
    }

    @GetMapping("/{planId}")
    public Result<MedicationPlan> getPlanById(@PathVariable Long planId) {
        MedicationPlan plan = medicationPlanService.getPlanById(planId);
        if (plan != null) {
            return Result.success(plan);
        }
        return Result.error("用药计划不存在");
    }

    @PostMapping("/create")
    public Result<MedicationPlan> createPlan(@RequestBody Map<String, Object> data) {
        try {
            MedicationPlan plan = new MedicationPlan();
            plan.setUserId(Long.valueOf(data.get("userId").toString()));
            if (data.get("recordId") != null) {
                plan.setRecordId(Long.valueOf(data.get("recordId").toString()));
            }
            if (data.get("medicineId") != null) {
                plan.setMedicineId(Long.valueOf(data.get("medicineId").toString()));
            }
            plan.setMedicineName((String) data.get("medicineName"));
            plan.setDosage((String) data.get("dosage"));
            plan.setFrequency((String) data.get("frequency"));
            
            if (data.get("startDate") != null) {
                plan.setStartDate(java.time.LocalDate.parse((String) data.get("startDate")));
            }
            if (data.get("endDate") != null) {
                plan.setEndDate(java.time.LocalDate.parse((String) data.get("endDate")));
            }
            
            plan.setPurpose((String) data.get("purpose"));
            plan.setNotes((String) data.get("notes"));
            
            if (data.get("status") != null) {
                plan.setStatus(Integer.valueOf(data.get("status").toString()));
            }
            
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> reminderTimes = (List<Map<String, Object>>) data.get("reminderTimes");
            
            MedicationPlan saved = medicationPlanService.createPlan(plan, reminderTimes);
            return Result.success("用药计划创建成功", saved);
        } catch (Exception e) {
            return Result.error("创建失败: " + e.getMessage());
        }
    }

    @PutMapping("/{planId}")
    public Result<MedicationPlan> updatePlan(@PathVariable Long planId, @RequestBody MedicationPlan plan) {
        try {
            MedicationPlan updated = medicationPlanService.updatePlan(planId, plan);
            return Result.success("用药计划更新成功", updated);
        } catch (Exception e) {
            return Result.error("更新失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/{planId}")
    public Result<Void> deletePlan(@PathVariable Long planId) {
        try {
            medicationPlanService.deletePlan(planId);
            return Result.success("用药计划已删除", null);
        } catch (Exception e) {
            return Result.error("删除失败: " + e.getMessage());
        }
    }

    @GetMapping("/medicines")
    public Result<List<Medicine>> getAllMedicines() {
        List<Medicine> medicines = medicationPlanService.getAllMedicines();
        return Result.success(medicines);
    }

    @GetMapping("/medicines/search")
    public Result<List<Medicine>> searchMedicines(@RequestParam String keyword) {
        List<Medicine> medicines = medicationPlanService.searchMedicines(keyword);
        return Result.success(medicines);
    }

    @PostMapping("/detect-conflicts")
    public Result<List<Map<String, Object>>> detectConflicts(@RequestBody List<Long> medicineIds) {
        List<Map<String, Object>> conflicts = medicationPlanService.detectConflicts(medicineIds);
        return Result.success(conflicts);
    }

    @GetMapping("/{planId}/reminder-times")
    public Result<List<PlanReminderTime>> getReminderTimesByPlanId(@PathVariable Long planId) {
        List<PlanReminderTime> times = medicationPlanService.getReminderTimesByPlanId(planId);
        return Result.success(times);
    }
}
