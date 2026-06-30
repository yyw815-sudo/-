package com.medical.repository;

import com.medical.entity.MedicationPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface MedicationPlanRepository extends JpaRepository<MedicationPlan, Long> {
    List<MedicationPlan> findByUserId(Long userId);
    List<MedicationPlan> findByRecordId(Long recordId);
    List<MedicationPlan> findByMedicineId(Long medicineId);
    List<MedicationPlan> findByUserIdAndStatus(Long userId, Integer status);
    List<MedicationPlan> findByUserIdAndRecordId(Long userId, Long recordId);

    @Query("SELECT COUNT(p) FROM MedicationPlan p WHERE p.recordId = :recordId")
    long countByRecordId(@Param("recordId") Long recordId);

    void deleteByRecordId(Long recordId);
}
