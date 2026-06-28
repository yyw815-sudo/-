package com.medical.service;

import com.medical.entity.MedicationPlan;
import com.medical.entity.Medicine;
import com.medical.entity.MedicineInteraction;
import com.medical.entity.PlanReminderTime;
import com.medical.repository.MedicationPlanRepository;
import com.medical.repository.MedicineInteractionRepository;
import com.medical.repository.MedicineRepository;
import com.medical.repository.PlanReminderTimeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MedicationPlanService {

    @Autowired
    private MedicationPlanRepository medicationPlanRepository;

    @Autowired
    private MedicineRepository medicineRepository;

    @Autowired
    private MedicineInteractionRepository medicineInteractionRepository;

    @Autowired
    private PlanReminderTimeRepository planReminderTimeRepository;

    public List<MedicationPlan> getPlansByUserId(Long userId) {
        return medicationPlanRepository.findByUserIdOrderByCreateTimeDesc(userId);
    }

    public MedicationPlan getPlanById(Long planId) {
        return medicationPlanRepository.findById(planId).orElse(null);
    }

    @Transactional
    public MedicationPlan createPlan(MedicationPlan plan, List<Map<String, Object>> reminderTimes) {
        plan.setCreateTime(LocalDateTime.now());
        plan.setUpdateTime(LocalDateTime.now());
        if (plan.getStatus() == null) {
            plan.setStatus(1);
        }
        MedicationPlan saved = medicationPlanRepository.save(plan);
        
        if (reminderTimes != null && !reminderTimes.isEmpty()) {
            for (Map<String, Object> rt : reminderTimes) {
                PlanReminderTime time = new PlanReminderTime();
                time.setPlanId(saved.getPlanId());
                time.setSuggestedTime(java.time.LocalTime.parse((String) rt.get("suggestedTime")));
                time.setReason((String) rt.get("reason"));
                time.setCreateTime(LocalDateTime.now());
                planReminderTimeRepository.save(time);
            }
        }
        
        return saved;
    }

    @Transactional
    public MedicationPlan updatePlan(Long planId, MedicationPlan plan) {
        MedicationPlan existing = medicationPlanRepository.findById(planId)
                .orElseThrow(() -> new RuntimeException("用药计划不存在"));
        
        if (plan.getMedicineName() != null) {
            existing.setMedicineName(plan.getMedicineName());
        }
        if (plan.getDosage() != null) {
            existing.setDosage(plan.getDosage());
        }
        if (plan.getFrequency() != null) {
            existing.setFrequency(plan.getFrequency());
        }
        if (plan.getStartDate() != null) {
            existing.setStartDate(plan.getStartDate());
        }
        if (plan.getEndDate() != null) {
            existing.setEndDate(plan.getEndDate());
        }
        if (plan.getPurpose() != null) {
            existing.setPurpose(plan.getPurpose());
        }
        if (plan.getNotes() != null) {
            existing.setNotes(plan.getNotes());
        }
        if (plan.getStatus() != null) {
            existing.setStatus(plan.getStatus());
        }
        if (plan.getMedicineId() != null) {
            existing.setMedicineId(plan.getMedicineId());
        }
        
        existing.setUpdateTime(LocalDateTime.now());
        return medicationPlanRepository.save(existing);
    }

    @Transactional
    public void deletePlan(Long planId) {
        planReminderTimeRepository.deleteByPlanId(planId);
        medicationPlanRepository.deleteById(planId);
    }

    public List<Medicine> getAllMedicines() {
        return medicineRepository.findAll();
    }

    public List<Medicine> searchMedicines(String keyword) {
        return medicineRepository.findByMedicineNameContaining(keyword);
    }

    public List<Map<String, Object>> detectConflicts(List<Long> medicineIds) {
        List<Map<String, Object>> conflicts = new ArrayList<>();
        
        for (int i = 0; i < medicineIds.size(); i++) {
            for (int j = i + 1; j < medicineIds.size(); j++) {
                List<MedicineInteraction> interactions = medicineInteractionRepository
                        .findByMedicinePair(medicineIds.get(i), medicineIds.get(j));
                
                for (MedicineInteraction interaction : interactions) {
                    Map<String, Object> conflict = new HashMap<>();
                    conflict.put("interactionId", interaction.getInteractionId());
                    conflict.put("medicineA", interaction.getMedicineA());
                    conflict.put("medicineB", interaction.getMedicineB());
                    conflict.put("conflictLevel", interaction.getConflictLevel());
                    conflict.put("description", interaction.getDescription());
                    conflict.put("suggestion", interaction.getSuggestion());
                    conflicts.add(conflict);
                }
            }
        }
        
        return conflicts;
    }

    public List<PlanReminderTime> getReminderTimesByPlanId(Long planId) {
        return planReminderTimeRepository.findByPlanId(planId);
    }
}
