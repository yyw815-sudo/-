package com.medical.repository;

import com.medical.entity.MedicationDailyRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface MedicationDailyRecordRepository extends JpaRepository<MedicationDailyRecord, Long> {
    List<MedicationDailyRecord> findByPlanIdOrderByScheduledTimeAsc(Long planId);
    List<MedicationDailyRecord> findByUserIdAndScheduledTimeBetween(Long userId, LocalDateTime start, LocalDateTime end);
    List<MedicationDailyRecord> findByPlanIdAndScheduledTimeBetween(Long planId, LocalDateTime start, LocalDateTime end);
    List<MedicationDailyRecord> findByScheduledTimeBetween(LocalDateTime start, LocalDateTime end);
    void deleteByPlanId(Long planId);
}
