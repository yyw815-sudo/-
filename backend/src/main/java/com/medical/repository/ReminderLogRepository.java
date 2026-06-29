package com.medical.repository;

import com.medical.entity.ReminderLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReminderLogRepository extends JpaRepository<ReminderLog, Long> {

    List<ReminderLog> findByUserIdOrderByCreateTimeDesc(Long userId);
}
