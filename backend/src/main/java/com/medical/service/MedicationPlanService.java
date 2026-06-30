package com.medical.service;

import com.medical.entity.*;
import com.medical.repository.*;
import com.medical.dto.*;
import com.medical.service.ai.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class MedicationPlanService {

    // 常用药物分类知识库（用于替代方案建议）
    private static final Map<String, List<String>> DRUG_CATEGORIES = new LinkedHashMap<>();
    static {
        DRUG_CATEGORIES.put("降糖药", Arrays.asList("二甲双胍", "格列美脲", "阿卡波糖", "胰岛素", "西格列汀", "沙格列汀", "利拉鲁肽"));
        DRUG_CATEGORIES.put("降压药", Arrays.asList("硝苯地平", "卡托普利", "依那普利", "氯沙坦", "缬沙坦", "美托洛尔"));
        DRUG_CATEGORIES.put("降脂药", Arrays.asList("阿托伐他汀", "瑞舒伐他汀", "辛伐他汀", "非诺贝特"));
        DRUG_CATEGORIES.put("抗血小板药", Arrays.asList("阿司匹林", "氯吡格雷", "替格瑞洛"));
        DRUG_CATEGORIES.put("抗生素", Arrays.asList("青霉素", "阿莫西林", "头孢", "左氧氟沙星", "阿奇霉素"));
    }

    private final MedicationPlanRepository planRepository;
    private final MedicationDailyRecordRepository dailyRecordRepository;
    private final PlanReminderTimeRepository reminderTimeRepository;
    private final MedicineService medicineService;
    private final MedicalRecordRepository medicalRecordRepository;
    private final MedicineInteractionRepository interactionRepository;
    private final BaiduAiService baiduAiService;
    private final MedicineRepository medicineRepository;
    private final MedicationAIService medicationAIService;
    private final AiAnalysisResultRepository analysisResultRepository;
    private final RuleEngine ruleEngine;
    private final DrugInteractionRepository drugInteractionRepository;

    public MedicationPlanService(MedicationPlanRepository planRepository,
                                 MedicationDailyRecordRepository dailyRecordRepository,
                                 PlanReminderTimeRepository reminderTimeRepository,
                                 MedicineService medicineService,
                                 MedicalRecordRepository medicalRecordRepository,
                                 MedicineInteractionRepository interactionRepository,
                                 BaiduAiService baiduAiService,
                                 MedicineRepository medicineRepository,
                                 MedicationAIService medicationAIService,
                                 AiAnalysisResultRepository analysisResultRepository,
                                 RuleEngine ruleEngine,
                                 DrugInteractionRepository drugInteractionRepository) {
        this.planRepository = planRepository;
        this.dailyRecordRepository = dailyRecordRepository;
        this.reminderTimeRepository = reminderTimeRepository;
        this.medicineService = medicineService;
        this.medicalRecordRepository = medicalRecordRepository;
        this.interactionRepository = interactionRepository;
        this.baiduAiService = baiduAiService;
        this.medicineRepository = medicineRepository;
        this.medicationAIService = medicationAIService;
        this.analysisResultRepository = analysisResultRepository;
        this.ruleEngine = ruleEngine;
        this.drugInteractionRepository = drugInteractionRepository;
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

    // ========== 新AI引擎方法 ==========

    /** AI分析处方并创建用药计划（新AI引擎） */
    @Transactional
    public AiAnalysisResultDTO createPlansFromAi(Long userId, Long recordId, String medicinesText, String patientInfo) {
        MedicalRecord record = medicalRecordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("病历不存在"));

        MedicationAIService.AnalysisResult result = medicationAIService.analyzeAndGenerate(userId, record);

        AiAnalysisResultDTO dto = new AiAnalysisResultDTO();
        dto.setWarnings(result.getWarnings());
        dto.setErrors(result.getErrors());

        if (result.getRuleResult() != null) {
            dto.setRiskScore(result.getRuleResult().getRiskScore());
            dto.setRiskLevel(result.getRuleResult().getRiskLevel() != null ? result.getRuleResult().getRiskLevel().name() : null);
            dto.setHasConflict(result.getRuleResult().isHasConflict());
            dto.setConflicts(result.getRuleResult().getConflicts());
        }

        if (!result.isSuccess()) throw new RuntimeException("AI分析失败: " + String.join("; ", result.getErrors()));
        if (result.getPlans() == null || result.getPlans().isEmpty()) throw new RuntimeException("AI未能生成任何用药计划");

        List<MedicationPlanResponseDTO> planDTOs = result.getPlans().stream().map(this::buildResponseDTO).collect(Collectors.toList());
        dto.setPlans(planDTOs);
        dto.setPlanCount(planDTOs.size());

        saveAnalysisResult(userId, recordId, dto);
        return dto;
    }

    private void saveAnalysisResult(Long userId, Long recordId, AiAnalysisResultDTO dto) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            AiAnalysisResult entity = new AiAnalysisResult();
            entity.setUserId(userId);
            entity.setRecordId(recordId);
            entity.setRiskScore(dto.getRiskScore());
            entity.setRiskLevel(dto.getRiskLevel());
            entity.setTotalDrugs(dto.getPlanCount());
            entity.setHasConflict(dto.getHasConflict() != null && dto.getHasConflict() ? 1 : 0);
            entity.setAnalysisTime(LocalDateTime.now());
            if (dto.getConflicts() != null && !dto.getConflicts().isEmpty())
                entity.setConflicts(mapper.writeValueAsString(dto.getConflicts()));
            if (dto.getWarnings() != null && !dto.getWarnings().isEmpty())
                entity.setWarnings(mapper.writeValueAsString(dto.getWarnings()));
            analysisResultRepository.save(entity);
        } catch (Exception e) { System.err.println("保存AI分析结果失败: " + e.getMessage()); }
    }

    public AiAnalysisResultDTO getLatestAnalysisResult(Long recordId) {
        Optional<AiAnalysisResult> result = analysisResultRepository.findFirstByRecordIdOrderByVersionDesc(recordId);
        return result.map(this::convertToDTO).orElse(null);
    }

    public List<AiAnalysisResultDTO> getAllAnalysisResultsByUserId(Long userId) {
        return analysisResultRepository.findByUserIdOrderByAnalysisTimeDesc(userId).stream().map(this::convertToDTO).toList();
    }

    private AiAnalysisResultDTO convertToDTO(AiAnalysisResult entity) {
        AiAnalysisResultDTO dto = new AiAnalysisResultDTO();
        dto.setRiskScore(entity.getRiskScore());
        dto.setRiskLevel(entity.getRiskLevel());
        dto.setVersion(entity.getVersion());
        dto.setTotalDrugs(entity.getTotalDrugs());
        dto.setHasConflict(entity.getHasConflict() != null && entity.getHasConflict() == 1);
        ObjectMapper mapper = new ObjectMapper();
        try {
            if (entity.getConflicts() != null && !entity.getConflicts().isEmpty())
                dto.setConflicts(mapper.readValue(entity.getConflicts(), mapper.getTypeFactory().constructCollectionType(List.class, RuleEngine.ConflictInfo.class)));
            if (entity.getWarnings() != null && !entity.getWarnings().isEmpty())
                dto.setWarnings(mapper.readValue(entity.getWarnings(), mapper.getTypeFactory().constructCollectionType(List.class, String.class)));
        } catch (Exception e) { System.err.println("解析AI分析结果JSON失败: " + e.getMessage()); }
        return dto;
    }

    /** 跨病历冲突检测 */
    public ConflictCheckResultDTO checkCrossRecordConflicts(List<Long> recordIds) {
        ConflictCheckResultDTO dto = new ConflictCheckResultDTO();
        dto.setRecordCount(recordIds.size());
        List<String> allMedicines = new ArrayList<>();
        Set<String> medicineSet = new HashSet<>();
        List<String> warnings = new ArrayList<>();
        for (Long recordId : recordIds) {
            List<MedicationPlan> plans = planRepository.findByRecordId(recordId);
            for (MedicationPlan p : plans) {
                if (p.getStatus() == 1 && p.getMedicineId() != null) {
                    String name = medicineRepository.findById(p.getMedicineId()).map(com.medical.entity.Medicine::getMedicineName).orElse(null);
                    if (name != null && medicineSet.add(name)) allMedicines.add(name);
                }
            }
            // 收集该病历的AI分析风险提示
            List<AiAnalysisResult> analysisResults = analysisResultRepository.findByRecordId(recordId);
            for (AiAnalysisResult ar : analysisResults) {
                if (ar.getWarnings() != null && !ar.getWarnings().isEmpty()) {
                    try {
                        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                        List<String> ws = mapper.readValue(ar.getWarnings(), List.class);
                        for (String w : ws) {
                            if (!warnings.contains(w)) warnings.add(w);
                        }
                    } catch (Exception e) {
                        log.warn("解析风险提示JSON失败: {}", e.getMessage());
                    }
                }
            }
        }
        dto.setAllMedicines(allMedicines);
        dto.setTotalDrugs(allMedicines.size());
        dto.setWarnings(warnings);

        // 计算综合风险等级
        if (!warnings.isEmpty()) {
            // 从warning中推断风险等级
            boolean hasHigh = warnings.stream().anyMatch(w -> w.contains("HIGH") || w.contains("高") || w.contains("严重"));
            boolean hasMedium = warnings.stream().anyMatch(w -> w.contains("MEDIUM") || w.contains("中"));
            if (hasHigh) { dto.setRiskLevel("HIGH"); dto.setRiskScore(70); }
            else if (hasMedium) { dto.setRiskLevel("MEDIUM"); dto.setRiskScore(50); }
            else { dto.setRiskLevel("LOW"); dto.setRiskScore(30); }
        } else {
            dto.setRiskLevel("LOW");
            dto.setRiskScore(0);
        }

        List<ConflictCheckResultDTO.ConflictItem> conflicts = new ArrayList<>();
        for (int i = 0; i < allMedicines.size(); i++) {
            for (int j = i + 1; j < allMedicines.size(); j++) {
                RuleEngine.ConflictInfo ci = findConflict(allMedicines.get(i), allMedicines.get(j));
                if (ci != null) {
                    ConflictCheckResultDTO.ConflictItem item = new ConflictCheckResultDTO.ConflictItem();
                    item.setDrugA(ci.getDrugA()); item.setDrugB(ci.getDrugB());
                    item.setLevel(ci.getLevel()); item.setDescription(ci.getDescription());
                    item.setSuggestion(ci.getSuggestion() != null ? ci.getSuggestion() : "建议咨询医生调整用药方案");
                    conflicts.add(item);
                }
            }
        }
        dto.setHasConflict(!conflicts.isEmpty());
        dto.setConflicts(conflicts);
        return dto;
    }

    /**
     * 冲突检测后自动替换冲突药物为建议的替代药
     * 解析suggestion文本中的"建议将A替换为B"模式，自动更新计划
     */
    @Transactional
    public void applyConflictReplacements(List<Long> recordIds, List<ConflictCheckResultDTO.ConflictItem> conflicts) {
        if (conflicts == null || conflicts.isEmpty()) return;
        // 获取所有涉及的计划
        List<MedicationPlan> allPlans = new ArrayList<>();
        for (Long rid : recordIds) {
            allPlans.addAll(planRepository.findByRecordId(rid));
        }
        for (ConflictCheckResultDTO.ConflictItem conflict : conflicts) {
            if (conflict.getSuggestion() == null) continue;
            // 解析 "建议将A替换为B" 或 "建议将A或B替换为..." 模式
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("建议将(.+?)替换为(.+?)（");
            java.util.regex.Matcher matcher = pattern.matcher(conflict.getSuggestion());
            if (!matcher.find()) continue;
            String oldDrug = matcher.group(1).trim();
            String newDrug = matcher.group(2).trim();
            // 处理 "A或B" 的情况，取第一个
            if (oldDrug.contains("或")) {
                String[] parts = oldDrug.split("或");
                for (String part : parts) {
                    applySingleReplacement(allPlans, part.trim(), newDrug);
                }
            } else {
                applySingleReplacement(allPlans, oldDrug, newDrug);
            }
        }
    }

    private void applySingleReplacement(List<MedicationPlan> allPlans, String oldDrugName, String newDrugName) {
        if (newDrugName.contains("如") || newDrugName.contains("等")) {
            // 是通用建议如"阿卡波糖、胰岛素等"，跳过具体替换
            return;
        }
        // 查找要替换的计划
        for (MedicationPlan plan : allPlans) {
            if (plan.getMedicineId() == null) continue;
            String planDrugName = medicineRepository.findById(plan.getMedicineId())
                .map(Medicine::getMedicineName).orElse("");
            if (!planDrugName.equals(oldDrugName)) continue;
            // 查找或创建替代药品
            Medicine newMedicine = medicineService.findOrCreateMedicine(newDrugName);
            if (newMedicine.getMedicineId().equals(plan.getMedicineId())) continue;
            // 删除旧的每日记录
            dailyRecordRepository.deleteByPlanId(plan.getPlanId());
            // 更新计划
            plan.setMedicineId(newMedicine.getMedicineId());
            plan.setPurpose((plan.getPurpose() != null ? plan.getPurpose() + "；" : "")
                + "原" + oldDrugName + "因冲突已替换为" + newDrugName);
            planRepository.save(plan);
            // 重新生成每日记录
            generateDailyRecords(plan);
            log.info("已自动替换药物: {} -> {} (planId={})", oldDrugName, newDrugName, plan.getPlanId());
        }
    }

    private RuleEngine.ConflictInfo findConflict(String nameA, String nameB) {
        Medicine medicineA = medicineRepository.findByMedicineName(nameA).stream().findFirst().orElse(null);
        Medicine medicineB = medicineRepository.findByMedicineName(nameB).stream().findFirst().orElse(null);
        if (medicineA != null && medicineB != null) {
            List<MedicineInteraction> list = interactionRepository.findByMedicineIdAAndMedicineIdB(medicineA.getMedicineId(), medicineB.getMedicineId());
            if (!list.isEmpty()) {
                RuleEngine.ConflictInfo ci = new RuleEngine.ConflictInfo();
                ci.setDrugA(nameA); ci.setDrugB(nameB);
                ci.setLevel(list.get(0).getSeverity());
                ci.setDescription(list.get(0).getDescription());
                ci.setSuggestion(suggestAlternativeDrug(medicineA, medicineB));
                return ci;
            }
            list = interactionRepository.findByMedicineIdAAndMedicineIdB(medicineB.getMedicineId(), medicineA.getMedicineId());
            if (!list.isEmpty()) {
                RuleEngine.ConflictInfo ci = new RuleEngine.ConflictInfo();
                ci.setDrugA(nameA); ci.setDrugB(nameB);
                ci.setLevel(list.get(0).getSeverity());
                ci.setDescription(list.get(0).getDescription());
                ci.setSuggestion(suggestAlternativeDrug(medicineB, medicineA));
                return ci;
            }
        }
        return null;
    }

    /**
     * 为冲突药品查找替代药品建议
     * 基于适应症关键词匹配，找到不冲突的替代药
     */
    private String suggestAlternativeDrug(Medicine drugA, Medicine drugB) {
        // 1. 尝试从drugA的适应症中提取关键词找替代药
        if (drugA.getIndication() != null && !drugA.getIndication().isEmpty()) {
            String[] keywords = drugA.getIndication().split("[，,、；;\\s]+");
            for (String keyword : keywords) {
                if (keyword.length() < 2) continue;
                List<Medicine> candidates = medicineRepository.findByIndicationContaining(keyword);
                for (Medicine candidate : candidates) {
                    if (candidate.getMedicineId().equals(drugA.getMedicineId())) continue;
                    if (candidate.getMedicineId().equals(drugB.getMedicineId())) continue;
                    // 检查候选药是否与drugB冲突
                    if (!hasConflict(candidate, drugB)) {
                        return "建议将" + drugA.getMedicineName() + "替换为" + candidate.getMedicineName() + "（适应症相似，无明显冲突）";
                    }
                }
            }
        }
        // 2. 尝试从drugB的适应症中提取关键词找替代药
        if (drugB.getIndication() != null && !drugB.getIndication().isEmpty()) {
            String[] keywords = drugB.getIndication().split("[，,、；;\\s]+");
            for (String keyword : keywords) {
                if (keyword.length() < 2) continue;
                List<Medicine> candidates = medicineRepository.findByIndicationContaining(keyword);
                for (Medicine candidate : candidates) {
                    if (candidate.getMedicineId().equals(drugA.getMedicineId())) continue;
                    if (candidate.getMedicineId().equals(drugB.getMedicineId())) continue;
                    if (!hasConflict(candidate, drugA)) {
                        return "建议将" + drugB.getMedicineName() + "替换为" + candidate.getMedicineName() + "（适应症相似，无明显冲突）";
                    }
                }
            }
        }
        // 3. 从drug_interaction表查找预设建议
        try {
            List<DrugInteraction> dis = drugInteractionRepository.findByDrugNameAAndDrugNameB(drugA.getMedicineName(), drugB.getMedicineName());
            if (dis.isEmpty()) {
                dis = drugInteractionRepository.findByDrugNameAAndDrugNameB(drugB.getMedicineName(), drugA.getMedicineName());
            }
            if (!dis.isEmpty() && dis.get(0).getSuggestion() != null && !dis.get(0).getSuggestion().isEmpty()) {
                return dis.get(0).getSuggestion();
            }
        } catch (Exception e) {
            log.warn("查询drug_interaction建议失败", e);
        }
        // 4. 基于药物分类知识库查找同类别替代药
        try {
            // 找到drugA所属的分类
            String categoryA = null, categoryB = null;
            for (Map.Entry<String, List<String>> entry : DRUG_CATEGORIES.entrySet()) {
                for (String keyword : entry.getValue()) {
                    if (drugA.getMedicineName().contains(keyword)) categoryA = entry.getKey();
                    if (drugB.getMedicineName().contains(keyword)) categoryB = entry.getKey();
                }
            }
            // 如果属于同一分类，尝试找该分类下其他不冲突的药品
            if (categoryA != null && categoryA.equals(categoryB)) {
                List<Medicine> categoryDrugs = medicineRepository.findAll().stream()
                    .filter(m -> !m.getMedicineId().equals(drugA.getMedicineId()))
                    .filter(m -> !m.getMedicineId().equals(drugB.getMedicineId()))
                    .filter(m -> !m.getMedicineName().equals(drugA.getMedicineName()))
                    .filter(m -> !m.getMedicineName().equals(drugB.getMedicineName()))
                    .collect(Collectors.toList());
                for (Medicine candidate : categoryDrugs) {
                    String name = candidate.getMedicineName();
                    // 排除原名包含关系（如"盐酸二甲双胍片"含"二甲双胍"，是同一药物不同命名）
                    if (drugA.getMedicineName().contains(name) || name.contains(drugA.getMedicineName())) continue;
                    if (drugB.getMedicineName().contains(name) || name.contains(drugB.getMedicineName())) continue;
                    for (String keyword : DRUG_CATEGORIES.get(categoryA)) {
                        if (name.contains(keyword) && !hasConflict(candidate, drugB)) {
                            return "建议将" + drugA.getMedicineName() + "替换为" + name + "（同类药物，无明显冲突）";
                        }
                    }
                }
            }
            // 尝试找drugB所在分类的替代药
            if (categoryB != null) {
                List<Medicine> categoryDrugs = medicineRepository.findAll().stream()
                    .filter(m -> !m.getMedicineId().equals(drugA.getMedicineId()))
                    .filter(m -> !m.getMedicineId().equals(drugB.getMedicineId()))
                    .filter(m -> !m.getMedicineName().equals(drugA.getMedicineName()))
                    .filter(m -> !m.getMedicineName().equals(drugB.getMedicineName()))
                    .collect(Collectors.toList());
                for (Medicine candidate : categoryDrugs) {
                    String name = candidate.getMedicineName();
                    // 排除原名包含关系
                    if (drugA.getMedicineName().contains(name) || name.contains(drugA.getMedicineName())) continue;
                    if (drugB.getMedicineName().contains(name) || name.contains(drugB.getMedicineName())) continue;
                    for (String keyword : DRUG_CATEGORIES.get(categoryB)) {
                        if (name.contains(keyword) && !hasConflict(candidate, drugA)) {
                            return "建议将" + drugB.getMedicineName() + "替换为" + name + "（同类药物，无明显冲突）";
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.warn("知识库匹配失败", e);
        }
        // 5. 基于药物分类知识库给出分类级建议（即使数据库中没有具体替代药）
        try {
            String categoryA = null, categoryB = null;
            for (Map.Entry<String, List<String>> entry : DRUG_CATEGORIES.entrySet()) {
                for (String keyword : entry.getValue()) {
                    if (drugA.getMedicineName().contains(keyword)) categoryA = entry.getKey();
                    if (drugB.getMedicineName().contains(keyword)) categoryB = entry.getKey();
                }
            }
            if (categoryA != null && categoryA.equals(categoryB)) {
                // 过滤掉与当前冲突药品同名的推荐
                List<String> alternatives = new ArrayList<>();
                for (String alt : DRUG_CATEGORIES.get(categoryA)) {
                    if (!drugA.getMedicineName().contains(alt) && !drugB.getMedicineName().contains(alt)) {
                        alternatives.add(alt);
                    }
                }
                if (!alternatives.isEmpty()) {
                    String altList = String.join("、", alternatives);
                    return "建议将" + drugA.getMedicineName() + "或" + drugB.getMedicineName()
                        + "替换为同类其他药物如" + altList + "等（需医生评估后调整）";
                }
            }
        } catch (Exception e) {
            log.warn("知识库分类建议生成失败", e);
        }
        // 6. 无匹配替代药，返回通用建议
        return "建议咨询医生，考虑调整用药方案或改用替代药物";
    }

    /** 检查两种药品是否存在冲突 */
    private boolean hasConflict(Medicine m1, Medicine m2) {
        if (!interactionRepository.findByMedicineIdAAndMedicineIdB(m1.getMedicineId(), m2.getMedicineId()).isEmpty()) return true;
        if (!interactionRepository.findByMedicineIdAAndMedicineIdB(m2.getMedicineId(), m1.getMedicineId()).isEmpty()) return true;
        return false;
    }

    /** 获取JsonNode文本，为null时返回null */
    private String getNodeText(JsonNode node, String field) {
        JsonNode fieldNode = node.get(field);
        if (fieldNode == null || fieldNode.isNull()) return null;
        String text = fieldNode.asText();
        return text.isEmpty() ? null : text;
    }
}
