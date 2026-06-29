package com.medical.service;

import com.medical.entity.ReminderLog;
import com.medical.repository.ReminderLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ReminderLogService {

    @Autowired
    private ReminderLogRepository reminderLogRepository;

    public List<ReminderLog> getLogsByUserId(Long userId) {
        return reminderLogRepository.findByUserIdOrderByCreateTimeDesc(userId);
    }
}
