package com.medical.service;

import com.medical.entity.Reminder;
import com.medical.entity.ReminderLog;
import com.medical.repository.ReminderLogRepository;
import com.medical.repository.ReminderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReminderLogService {

    @Autowired
    private ReminderLogRepository reminderLogRepository;

    @Autowired
    private ReminderRepository reminderRepository;

    @Transactional
    public Map<String, Object> sendReminder(Long reminderId) {
        Reminder reminder = reminderRepository.findById(reminderId)
                .orElseThrow(() -> new RuntimeException("提醒配置不存在"));

        String content = buildReminderContent(reminder);
        
        ReminderLog log = new ReminderLog(
                reminder.getReminderId(),
                reminder.getUserId(),
                reminder.getPlanId(),
                reminder.getChannel(),
                content
        );
        log.setLevel(reminder.getLevel());

        Map<String, Object> result = new HashMap<>();

        try {
            boolean sendSuccess = executeSend(reminder, content);
            log.setStatus(sendSuccess ? "sent" : "failed");
            log.setSendTime(LocalDateTime.now());
            ReminderLog savedLog = reminderLogRepository.save(log);
            
            result.put("success", sendSuccess);
            result.put("logId", savedLog.getLogId());
            result.put("channel", reminder.getChannel());
            
            if (!sendSuccess) {
                result.put("error", "发送失败");
            }
        } catch (Exception e) {
            log.setStatus("failed");
            log.setSendTime(LocalDateTime.now());
            reminderLogRepository.save(log);
            result.put("success", false);
            result.put("error", e.getMessage());
        }

        return result;
    }

    private String buildReminderContent(Reminder reminder) {
        StringBuilder content = new StringBuilder();
        content.append("用药提醒：");
        content.append("时间 ").append(reminder.getReminderTime());
        content.append("，渠道 ").append(reminder.getChannel());
        if (reminder.getPlanId() != null) {
            content.append("，关联计划ID ").append(reminder.getPlanId());
        }
        return content.toString();
    }

    private boolean executeSend(Reminder reminder, String content) {
        try {
            if ("APP".equals(reminder.getChannel())) {
                return sendAppNotification(reminder.getUserId(), content);
            } else if ("短信".equals(reminder.getChannel())) {
                return sendSmsNotification(reminder.getUserId(), content);
            } else if ("电话".equals(reminder.getChannel())) {
                return sendPhoneNotification(reminder.getUserId(), content);
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean sendAppNotification(Long userId, String content) {
        return true;
    }

    private boolean sendSmsNotification(Long userId, String content) {
        return true;
    }

    private boolean sendPhoneNotification(Long userId, String content) {
        return true;
    }

    public List<ReminderLog> getLogsByUserId(Long userId) {
        return reminderLogRepository.findByUserIdOrderBySendTimeDesc(userId);
    }

    public List<ReminderLog> getLogsByReminderId(Long reminderId) {
        return reminderLogRepository.findByReminderIdOrderBySendTimeDesc(reminderId);
    }

    @Transactional
    public ReminderLog updateLogStatus(Long logId, String status, String response) {
        ReminderLog log = reminderLogRepository.findById(logId)
                .orElseThrow(() -> new RuntimeException("提醒日志不存在"));
        
        if (status != null) {
            log.setStatus(status);
        }
        if (response != null) {
            log.setResponse(response);
        }
        if ("received".equals(status) || "read".equals(status)) {
            log.setReceiveTime(LocalDateTime.now());
        }
        
        return reminderLogRepository.save(log);
    }

    @Transactional
    public void markAsReceived(Long logId) {
        updateLogStatus(logId, "received", null);
    }

    @Transactional
    public void markAsRead(Long logId, String response) {
        updateLogStatus(logId, "read", response);
    }
}
