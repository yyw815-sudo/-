package com.medical.repository;

import com.medical.entity.Reminder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReminderRepository extends JpaRepository<Reminder, Long> {
    
    List<Reminder> findByUserIdOrderByReminderTime(Long userId);
    
    List<Reminder> findByUserIdAndEnabledOrderByReminderTime(Long userId, Integer enabled);
    
    Optional<Reminder> findByReminderIdAndUserId(Long reminderId, Long userId);
    
    List<Reminder> findByPlanId(Long planId);
    
    void deleteByPlanId(Long planId);
}
