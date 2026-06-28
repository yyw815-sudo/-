package com.medical.service;

import com.medical.entity.MedicationPlan;
import com.medical.entity.MedicationDailyRecord;
import com.medical.entity.MedicineInteraction;
import com.medical.entity.PlanReminderTime;
import com.medical.repository.MedicalRecordRepository;
import com.medical.repository.MedicineInteractionRepository;
import com.medical.repository.MedicineRepository;
import com.medical.repository.MedicationPlanRepository;
import com.medical.repository.MedicationDailyRecordRepository;
import com.medical.repository.PlanReminderTimeRepository;
import com.medical.dto.MedicationPlanResponseDTO;
import com.medical.dto.MedicationPlanUpdateDTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MedicationPlanService {

    private final MedicationPlanRepository planRepository;
    private final MedicationDailyRecordRepository dailyRecordRepository;
    private final PlanReminderTimeRepository reminderTimeRepository;
    private final MedicineService medicineService;
    private final MedicalRecordRepository medicalRecordRepository;
    private final MedicineInteractionRepository interactionRepository;
    private final BaiduAiService baiduAiService;
    private final MedicineRepository medicineRepository;

    public MedicationPlanService(MedicationPlanRepository planRepository,
                                 MedicationDailyRecordRepository dailyRecordRepository,
                                 PlanReminderTimeRepository reminderTimeRepository,
                                 MedicineService medicineService,
                                 MedicalRecordRepository medicalRecordRepository,
                                 MedicineInteractionRepository interactionRepository,
                                 BaiduAiService baiduAiService,
                                 MedicineRepository medicineRepository) {
        this.planRepository = planRepository;
        this.dailyRecordRepository = dailyRecordRepository;
        this.reminderTimeRepository = reminderTimeRepository;
        this.medicineService = medicineService;
        this.medicalRecordRepository = medicalRecordRepository;
        this.interactionRepository = interactionRepository;
        this.baiduAiService = baiduAiService;
        this.medicineRepository = medicineRepository;
    }

    /** AI分析处方并创建用药计划（主入口） */
    @Transactional
    public List<MedicationPlanResponseDTO> createPlansFromAi(Long userId, Long recordId, String prescriptionText) {
        // 1. 调用百度AI分析处方
        String aiResult = baiduAiService.analyzePrescription(prescriptionText);
        ObjectMapper mapper = new ObjectMapper();
        List<MedicationPlan> allNewPlans = new ArrayList<>();

        try {
            JsonNode root = mapper.readTree(aiResult);
            JsonNode medications = root.get("medications");
            if (medications == null || !medications.isArray()) {
                throw new RuntimeException("AI返回格式错误：缺少 medications 数组");
            }

            // 2. 解析每个药品并创建计划
            for (JsonNode med : medications) {
                String medicineName = getNodeText(med, "medicineName");
                String dosage = getNodeText(med, "dosage");
                String frequency = getNodeText(med, "frequency");
                int timesPerDay = med.has("timesPerDay") ? med.get("timesPerDay").asInt(1) : 1;
                int intervalHours = med.has("intervalHours") ? med.get("intervalHours").asInt(0) : 0;
                String startDateStr = getNodeText(med, "startDate");
                String endDateStr = getNodeText(med, "endDate");

                LocalDate startDate = startDateStr != null ? LocalDate.parse(startDateStr) : LocalDate.now();
                LocalDate endDate = endDateStr != null ? LocalDate.parse(endDateStr) : startDate.plusDays(30);

                // 自动查找或创建药品
                Long medicineId = medicineService.findOrCreateMedicine(medicineName).getMedicineId();

                MedicationPlan plan = new MedicationPlan();
                plan.setUserId(userId);
                plan.setRecordId(recordId);
                plan.setMedicineId(medicineId);
                plan.setDosage(dosage);
                plan.setFrequency(frequency);
                plan.setTimesPerDay(timesPerDay);
                plan.setIntervalHours(intervalHours);
                plan.setStartDate(startDate);
                plan.setEndDate(endDate);
                plan.setPurpose(getNodeText(med, "purpose"));
                plan.setStatus(1);
                allNewPlans.add(plan);
            }

        } catch (Exception e) {
            throw new RuntimeException("AI处方解析失败: " + e.getMessage(), e);
        }

        // 3. 先全部保存
        List<MedicationPlan> savedPlans = planRepository.saveAll(allNewPlans);

        // 4. 检查是否存在其他进行中的计划（多病例冲突检测）
        List<MedicationPlan> existingActivePlans = planRepository.findByUserIdAndStatus(userId, 1);
        existingActivePlans.removeIf(p -> savedPlans.stream().anyMatch(sp -> sp.getPlanId().equals(p.getPlanId())));

        if (!existingActivePlans.isEmpty()) {
            // 存在其他进行中计划，进行冲突检测
            List<Long> allMedicineIds = new ArrayList<>();
            for (MedicationPlan p : existingActivePlans) allMedicineIds.add(p.getMedicineId());
            for (MedicationPlan p : savedPlans) allMedicineIds.add(p.getMedicineId());

            boolean hasConflict = false;
            StringBuilder conflictInfo = new StringBuilder();
            for (int i = 0; i < allMedicineIds.size() && !hasConflict; i++) {
                for (int j = i + 1; j < allMedicineIds.size() && !hasConflict; j++) {
                    List<MedicineInteraction> conflicts = interactionRepository
                            .findByMedicineIdAAndMedicineIdB(allMedicineIds.get(i), allMedicineIds.get(j));
                    if (!conflicts.isEmpty()) {
                        hasConflict = true;
                        for (MedicineInteraction ci : conflicts) {
                            conflictInfo.append("药品冲突：").append(ci.getDescription()).append("\n");
                        }
                    }
                }
            }

            if (hasConflict) {
                // 需要AI校正：先删掉刚保存的计划，校正后重建
                planRepository.deleteAll(savedPlans);

                // 调用AI校正
                String correctedResult = baiduAiService.correctPlanWithConflict(prescriptionText, conflictInfo.toString());
                return rebuildPlansFromCorrection(userId, recordId, correctedResult);
            }
        }

        // 5. 生成每日记录（无冲突情况）
        for (MedicationPlan plan : savedPlans) {
            generateDailyRecords(plan);
        }

        // 6. 生成提醒时间（TODO: 基于AI建议）
        // for (MedicationPlan plan : savedPlans) { ... }

        return savedPlans.stream().map(this::buildResponseDTO).collect(Collectors.toList());
    }

    /** AI校正后重建计划 */
    @Transactional
    public List<MedicationPlanResponseDTO> rebuildPlansFromCorrection(Long userId, Long recordId, String correctedResult) {
        ObjectMapper mapper = new ObjectMapper();
        List<MedicationPlan> correctedPlans = new ArrayList<>();

        try {
            JsonNode root = mapper.readTree(correctedResult);
            JsonNode medications = root.get("medications");
            if (medications == null || !medications.isArray()) {
                throw new RuntimeException("AI校正返回格式错误");
            }

            for (JsonNode med : medications) {
                String medicineName = getNodeText(med, "medicineName");
                String dosage = getNodeText(med, "dosage");
                String frequency = getNodeText(med, "frequency");
                int timesPerDay = med.has("timesPerDay") ? med.get("timesPerDay").asInt(1) : 1;
                int intervalHours = med.has("intervalHours") ? med.get("intervalHours").asInt(0) : 0;
                String startDateStr = getNodeText(med, "startDate");
                String endDateStr = getNodeText(med, "endDate");

                LocalDate startDate = startDateStr != null ? LocalDate.parse(startDateStr) : LocalDate.now();
                LocalDate endDate = endDateStr != null ? LocalDate.parse(endDateStr) : startDate.plusDays(30);
                Long medicineId = medicineService.findOrCreateMedicine(medicineName).getMedicineId();

                MedicationPlan plan = new MedicationPlan();
                plan.setUserId(userId);
                plan.setRecordId(recordId);
                plan.setMedicineId(medicineId);
                plan.setDosage(dosage);
                plan.setFrequency(frequency);
                plan.setTimesPerDay(timesPerDay);
                plan.setIntervalHours(intervalHours);
                plan.setStartDate(startDate);
                plan.setEndDate(endDate);
                plan.setPurpose(getNodeText(med, "purpose"));
                plan.setStatus(1);
                correctedPlans.add(plan);
            }
        } catch (Exception e) {
            throw new RuntimeException("AI校正解析失败: " + e.getMessage(), e);
        }

        List<MedicationPlan> saved = planRepository.saveAll(correctedPlans);
        for (MedicationPlan plan : saved) generateDailyRecords(plan);
        return saved.stream().map(this::buildResponseDTO).collect(Collectors.toList());
    }

    /** 生成每日服药记录 */
    @Transactional
    public void generateDailyRecords(MedicationPlan plan) {
        LocalDate start = plan.getStartDate();
        LocalDate end = plan.getEndDate() != null ? plan.getEndDate() : start.plusDays(30);
        int timesPerDay = plan.getTimesPerDay() != null && plan.getTimesPerDay() > 0 ? plan.getTimesPerDay() : 1;
        int intervalHours = plan.getIntervalHours() != null && plan.getIntervalHours() > 0
                ? plan.getIntervalHours() : (24 / timesPerDay);

        List<MedicationDailyRecord> records = new ArrayList<>();
        LocalDate current = start;

        while (!current.isAfter(end)) {
            for (int i = 0; i < timesPerDay; i++) {
                LocalTime time = LocalTime.of(8 + (i * intervalHours), 0);
                if (time.getHour() >= 24) break;

                MedicationDailyRecord record = new MedicationDailyRecord();
                record.setPlanId(plan.getPlanId());
                record.setUserId(plan.getUserId());
                record.setMedicineId(plan.getMedicineId());
                record.setScheduledTime(LocalDateTime.of(current, time));
                record.setStatus(0); // 待服药
                records.add(record);
            }
            current = current.plusDays(1);
        }

        dailyRecordRepository.saveAll(records);
    }

    /** 构建响应DTO */
    public MedicationPlanResponseDTO buildResponseDTO(MedicationPlan plan) {
        MedicationPlanResponseDTO dto = new MedicationPlanResponseDTO();
        BeanUtils.copyProperties(plan, dto);

        // 药品名称
        if (plan.getMedicineId() != null) {
            medicineRepository.findById(plan.getMedicineId()).ifPresent(m -> {
                dto.setMedicineName(m.getMedicineName());
                dto.setSpecification(m.getSpecification());
            });
        }

        // 病历名称
        if (plan.getRecordId() != null) {
            medicalRecordRepository.findById(plan.getRecordId()).ifPresent(r ->
                    dto.setDiseaseName(r.getDiseaseName()));
        }

        // 状态文案
        if (plan.getStatus() != null) {
            switch (plan.getStatus()) {
                case 0: dto.setStatusDescription("已停用"); break;
                case 1: dto.setStatusDescription("进行中"); break;
                case 2: dto.setStatusDescription("已完成"); break;
                default: dto.setStatusDescription("未知");
            }
        }

        // 每日进度
        List<MedicationDailyRecord> dailyRecords = dailyRecordRepository.findByPlanIdOrderByScheduledTimeAsc(plan.getPlanId());
        long total = dailyRecords.size();
        long taken = dailyRecords.stream().filter(r -> r.getStatus() == 1).count();
        dto.setDailyProgress(taken + "/" + total);

        // 提醒时间
        List<PlanReminderTime> reminderTimes = reminderTimeRepository.findByPlanId(plan.getPlanId());
        if (!reminderTimes.isEmpty()) {
            dto.setReminderTimes(reminderTimes.stream()
                    .map(t -> t.getSuggestedTime().format(DateTimeFormatter.ofPattern("HH:mm")))
                    .collect(Collectors.toList()));
        }

        return dto;
    }

    /** 删除计划（级联删除每日记录 + 提醒时间） */
    @Transactional
    public void deletePlan(Long planId) {
        dailyRecordRepository.deleteByPlanId(planId);
        reminderTimeRepository.deleteByPlanId(planId);
        planRepository.deleteById(planId);
    }

    /**
     * 删除病历关联的所有计划（级联删除每日记录 + 提醒时间）
     * 返回被删除的计划摘要列表
     */
    @Transactional
    public List<MedicationPlanResponseDTO> deletePlansByRecordId(Long recordId) {
        List<MedicationPlan> plans = planRepository.findByRecordId(recordId);
        for (MedicationPlan plan : plans) {
            dailyRecordRepository.deleteByPlanId(plan.getPlanId());
            reminderTimeRepository.deleteByPlanId(plan.getPlanId());
            planRepository.deleteById(plan.getPlanId());
        }
        return plans.stream().map(this::buildResponseDTO).collect(Collectors.toList());
    }

    /** 获取用户的所有计划 */
    public List<MedicationPlanResponseDTO> getPlansByUserId(Long userId) {
        return planRepository.findByUserId(userId).stream()
                .map(this::buildResponseDTO).collect(Collectors.toList());
    }

    /** 获取病历的所有计划 */
    public List<MedicationPlanResponseDTO> getPlansByRecordId(Long recordId) {
        return planRepository.findByRecordId(recordId).stream()
                .map(this::buildResponseDTO).collect(Collectors.toList());
    }

    /** 获取历史计划（按状态筛选） */
    public List<MedicationPlanResponseDTO> getHistoryPlansByStatus(Long userId, Integer status) {
        return planRepository.findByUserIdAndStatus(userId, status).stream()
                .map(this::buildResponseDTO).collect(Collectors.toList());
    }

    /** 获取单个计划 */
    public MedicationPlanResponseDTO getPlanById(Long planId) {
        return planRepository.findById(planId).map(this::buildResponseDTO).orElse(null);
    }

    /** 统计病历下的计划数 */
    public long countPlansByRecordId(Long recordId) {
        return planRepository.countByRecordId(recordId);
    }

    /** 更新计划 */
    @Transactional
    public MedicationPlanResponseDTO updatePlan(Long planId, MedicationPlanUpdateDTO updateDTO) {
        MedicationPlan plan = planRepository.findById(planId)
                .orElseThrow(() -> new RuntimeException("计划不存在"));

        if (updateDTO.getDosage() != null) plan.setDosage(updateDTO.getDosage());
        if (updateDTO.getFrequency() != null) plan.setFrequency(updateDTO.getFrequency());
        if (updateDTO.getTimesPerDay() != null) plan.setTimesPerDay(updateDTO.getTimesPerDay());
        if (updateDTO.getIntervalHours() != null) plan.setIntervalHours(updateDTO.getIntervalHours());
        if (updateDTO.getStartDate() != null) plan.setStartDate(updateDTO.getStartDate());
        if (updateDTO.getEndDate() != null) plan.setEndDate(updateDTO.getEndDate());
        if (updateDTO.getPurpose() != null) plan.setPurpose(updateDTO.getPurpose());
        if (updateDTO.getStatus() != null) plan.setStatus(updateDTO.getStatus());

        planRepository.save(plan);
        return buildResponseDTO(plan);
    }

    /** 获取JsonNode文本，为null时返回null */
    private String getNodeText(JsonNode node, String field) {
        JsonNode fieldNode = node.get(field);
        if (fieldNode == null || fieldNode.isNull()) return null;
        String text = fieldNode.asText();
        return text.isEmpty() ? null : text;
    }
}
