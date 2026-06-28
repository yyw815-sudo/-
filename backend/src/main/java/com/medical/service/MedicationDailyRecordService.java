package com.medical.service;

import com.medical.entity.MedicationDailyRecord;
import com.medical.repository.MedicationDailyRecordRepository;
import com.medical.repository.MedicationPlanRepository;
import com.medical.repository.MedicineRepository;
import com.medical.dto.MedicationDailyRecordDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MedicationDailyRecordService {

    private final MedicationDailyRecordRepository dailyRecordRepository;
    private final MedicationPlanRepository planRepository;
    private final MedicineRepository medicineRepository;

    public MedicationDailyRecordService(MedicationDailyRecordRepository dailyRecordRepository,
                                        MedicationPlanRepository planRepository,
                                        MedicineRepository medicineRepository) {
        this.dailyRecordRepository = dailyRecordRepository;
        this.planRepository = planRepository;
        this.medicineRepository = medicineRepository;
    }

    /** 根据计划ID获取每日记录 */
    public List<MedicationDailyRecordDTO> getByPlanId(Long planId) {
        return dailyRecordRepository.findByPlanIdOrderByScheduledTimeAsc(planId).stream()
                .map(this::toDTO).collect(Collectors.toList());
    }

    /** 获取用户某天的记录 */
    public List<MedicationDailyRecordDTO> getByUserIdAndDate(Long userId, LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(LocalTime.MAX);
        return dailyRecordRepository.findByUserIdAndScheduledTimeBetween(userId, start, end).stream()
                .map(this::toDTO).collect(Collectors.toList());
    }

    /** 获取用户日期范围内的记录 */
    public List<MedicationDailyRecordDTO> getByUserIdAndDateRange(Long userId, LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);
        return dailyRecordRepository.findByUserIdAndScheduledTimeBetween(userId, start, end).stream()
                .map(this::toDTO).collect(Collectors.toList());
    }

    /** 标记为已服药 */
    @Transactional
    public MedicationDailyRecordDTO markAsTaken(Long takeId, String photoUrl, String aiResult, BigDecimal aiAccuracy) {
        MedicationDailyRecord record = dailyRecordRepository.findById(takeId)
                .orElseThrow(() -> new RuntimeException("记录不存在"));
        record.setStatus(1);
        record.setTakeTime(LocalDateTime.now());
        if (photoUrl != null) record.setPhotoUrl(photoUrl);
        if (aiResult != null) record.setAiResult(aiResult);
        if (aiAccuracy != null) record.setAiAccuracy(aiAccuracy);
        dailyRecordRepository.save(record);
        return toDTO(record);
    }

    /** 标记为漏服 */
    @Transactional
    public MedicationDailyRecordDTO markAsMissed(Long takeId) {
        MedicationDailyRecord record = dailyRecordRepository.findById(takeId)
                .orElseThrow(() -> new RuntimeException("记录不存在"));
        record.setStatus(2);
        dailyRecordRepository.save(record);
        return toDTO(record);
    }

    /** 过期已完成计划（TODO：定时任务） */
    public void expireCompletedPlans() {
        // TODO: 定时检查并过期已完成计划
    }

    private MedicationDailyRecordDTO toDTO(MedicationDailyRecord record) {
        MedicationDailyRecordDTO dto = new MedicationDailyRecordDTO();
        dto.setTakeId(record.getTakeId());
        dto.setPlanId(record.getPlanId());
        dto.setScheduledTime(record.getScheduledTime());
        dto.setTakeTime(record.getTakeTime());
        dto.setStatus(record.getStatus());
        dto.setPhotoUrl(record.getPhotoUrl());
        dto.setAiResult(record.getAiResult());
        dto.setAiAccuracy(record.getAiAccuracy());

        if (record.getStatus() != null) {
            switch (record.getStatus()) {
                case 0: dto.setStatusText("待服药"); break;
                case 1: dto.setStatusText("已服药"); break;
                case 2: dto.setStatusText("漏服"); break;
            }
        }

        // 填充药品名称
        if (record.getMedicineId() != null) {
            medicineRepository.findById(record.getMedicineId()).ifPresent(m ->
                    dto.setMedicineName(m.getMedicineName()));
        }

        // 填充剂量
        planRepository.findById(record.getPlanId()).ifPresent(plan -> {
            if (plan.getDosage() != null) dto.setDosage(plan.getDosage());
            if (dto.getMedicineName() == null || dto.getMedicineName().isEmpty()) {
                dto.setMedicineName(""); // 由调用方补充
            }
        });

        return dto;
    }
}
