package com.medical.service;

import com.medical.entity.MedicationDailyRecord;
import com.medical.repository.MedicationDailyRecordRepository;
import com.medical.repository.MedicationPlanRepository;
import com.medical.repository.MedicineRepository;
import com.medical.dto.MedicationDailyRecordDTO;
import com.medical.dto.CombinedScheduleDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
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

    /**
     * 获取多个计划的合并日程（按天分组，按时间段展示药品）
     */
    public List<CombinedScheduleDTO> getCombinedSchedule(List<Long> planIds) {
        Map<LocalDate, Map<String, List<CombinedScheduleDTO.MedicineInfo>>> dayMap = new java.util.LinkedHashMap<>();

        for (Long planId : planIds) {
            List<MedicationDailyRecord> records = dailyRecordRepository.findByPlanIdOrderByScheduledTimeAsc(planId);
            for (MedicationDailyRecord rec : records) {
                LocalDate date = rec.getScheduledTime().toLocalDate();
                int hour = rec.getScheduledTime().getHour();
                String period;
                if (hour >= 6 && hour < 12) period = "上午";
                else if (hour >= 12 && hour < 14) period = "中午";
                else if (hour >= 14 && hour < 19) period = "下午";
                else period = "晚上";

                dayMap.putIfAbsent(date, new java.util.LinkedHashMap<>());
                dayMap.get(date).putIfAbsent(period, new java.util.ArrayList<>());

                CombinedScheduleDTO.MedicineInfo info = new CombinedScheduleDTO.MedicineInfo();
                info.setTakeId(rec.getTakeId());
                info.setStatus(rec.getStatus());
                if (rec.getMedicineId() != null) {
                    medicineRepository.findById(rec.getMedicineId()).ifPresent(m -> {
                        info.setMedicineName(m.getMedicineName());
                    });
                }
                // 获取剂量
                planRepository.findById(rec.getPlanId()).ifPresent(plan -> {
                    if (info.getDosage() == null && plan.getDosage() != null) {
                        info.setDosage(plan.getDosage());
                    }
                    if (info.getMedicineName() == null || info.getMedicineName().isEmpty()) {
                        info.setMedicineName("");
                    }
                });

                dayMap.get(date).get(period).add(info);
            }
        }

        List<CombinedScheduleDTO> result = new java.util.ArrayList<>();
        int dayNumber = 1;
        List<LocalDate> sortedDates = new java.util.ArrayList<>(dayMap.keySet());
        java.util.Collections.sort(sortedDates);

        for (LocalDate date : sortedDates) {
            CombinedScheduleDTO dto = new CombinedScheduleDTO();
            dto.setDayNumber(dayNumber++);
            dto.setDate(date);

            Map<String, List<CombinedScheduleDTO.MedicineInfo>> periodMap = dayMap.get(date);
            String[] periodOrder = {"上午", "中午", "下午", "晚上"};
            List<CombinedScheduleDTO.PeriodMedicines> periodList = new java.util.ArrayList<>();
            for (String p : periodOrder) {
                if (periodMap.containsKey(p) && !periodMap.get(p).isEmpty()) {
                    CombinedScheduleDTO.PeriodMedicines pm = new CombinedScheduleDTO.PeriodMedicines();
                    pm.setPeriod(p);
                    pm.setMedicines(periodMap.get(p));
                    periodList.add(pm);
                }
            }
            dto.setPeriods(periodList);
            result.add(dto);
        }
        return result;
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
