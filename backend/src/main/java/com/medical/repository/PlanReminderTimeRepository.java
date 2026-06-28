package com.medical.repository;

import com.medical.entity.PlanReminderTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PlanReminderTimeRepository extends JpaRepository<PlanReminderTime, Long> {
    
    List<PlanReminderTime> findByPlanId(Long planId);
    
    void deleteByPlanId(Long planId);
}
