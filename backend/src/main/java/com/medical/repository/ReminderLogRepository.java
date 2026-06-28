package com.medical.repository;

import com.medical.entity.ReminderLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReminderLogRepository extends JpaRepository<ReminderLog, Long> {
    
    List<ReminderLog> findByUserIdOrderBySendTimeDesc(Long userId);
    
    List<ReminderLog> findByReminderIdOrderBySendTimeDesc(Long reminderId);
    
    List<ReminderLog> findByChannelOrderBySendTimeDesc(String channel);
    
    List<ReminderLog> findByUserIdAndChannelOrderBySendTimeDesc(Long userId, String channel);
}
