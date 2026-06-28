package com.medical.repository;

import com.medical.entity.MedicationPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface MedicationPlanRepository extends JpaRepository<MedicationPlan, Long> {
    
    List<MedicationPlan> findByUserIdOrderByCreateTimeDesc(Long userId);
    
    Optional<MedicationPlan> findByPlanIdAndUserId(Long planId, Long userId);
    
    List<MedicationPlan> findByUserIdAndStatus(Long userId, Integer status);
}
