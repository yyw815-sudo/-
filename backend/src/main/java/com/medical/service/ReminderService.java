package com.medical.service;

import com.medical.dto.ReminderDTO;
import com.medical.dto.ReminderResponseDTO;
import com.medical.entity.Reminder;
import com.medical.repository.ReminderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ReminderService {

    @Autowired
    private ReminderRepository reminderRepository;

    @Transactional
    public List<Reminder> createRemindersFromPlan(Long userId, Long planId, String reminderTime, boolean includeApp, boolean includeSms, boolean includePhone) {
        List<Reminder> reminders = new ArrayList<>();
        LocalTime time = LocalTime.parse(reminderTime, DateTimeFormatter.ofPattern("HH:mm:ss"));
        
        if (includeApp) {
            Reminder reminder = new Reminder(userId, planId, time, "APP", 1, 1);
            reminders.add(reminderRepository.save(reminder));
        }
        if (includeSms) {
            Reminder reminder = new Reminder(userId, planId, time, "短信", 1, 2);
            reminders.add(reminderRepository.save(reminder));
        }
        if (includePhone) {
            Reminder reminder = new Reminder(userId, planId, time, "电话", 1, 3);
            reminders.add(reminderRepository.save(reminder));
        }
        
        return reminders;
    }

    @Transactional
    public Reminder createReminder(ReminderDTO dto) {
        LocalTime time = LocalTime.parse(dto.getReminderTime(), DateTimeFormatter.ofPattern("HH:mm:ss"));
        Reminder reminder = new Reminder(
                dto.getUserId(),
                dto.getPlanId(),
                time,
                dto.getChannel(),
                dto.getEnabled() != null ? dto.getEnabled() : 1,
                dto.getLevel() != null ? dto.getLevel() : 1
        );
        return reminderRepository.save(reminder);
    }

    @Transactional
    public Reminder updateReminder(Long reminderId, ReminderDTO dto) {
        Reminder reminder = reminderRepository.findById(reminderId)
                .orElseThrow(() -> new RuntimeException("提醒配置不存在"));
        
        if (dto.getReminderTime() != null) {
            reminder.setReminderTime(LocalTime.parse(dto.getReminderTime(), DateTimeFormatter.ofPattern("HH:mm:ss")));
        }
        if (dto.getChannel() != null) {
            reminder.setChannel(dto.getChannel());
        }
        if (dto.getEnabled() != null) {
            reminder.setEnabled(dto.getEnabled());
        }
        if (dto.getLevel() != null) {
            reminder.setLevel(dto.getLevel());
        }
        if (dto.getPlanId() != null) {
            reminder.setPlanId(dto.getPlanId());
        }
        
        return reminderRepository.save(reminder);
    }

    public List<Reminder> getRemindersByUserId(Long userId) {
        return reminderRepository.findByUserIdOrderByReminderTime(userId);
    }

    public List<Reminder> getEnabledRemindersByUserId(Long userId) {
        return reminderRepository.findByUserIdAndEnabledOrderByReminderTime(userId, 1);
    }

    public Optional<Reminder> getReminderById(Long reminderId) {
        return reminderRepository.findById(reminderId);
    }

    @Transactional
    public Reminder toggleReminder(Long reminderId, Integer enabled) {
        Reminder reminder = reminderRepository.findById(reminderId)
                .orElseThrow(() -> new RuntimeException("提醒配置不存在"));
        reminder.setEnabled(enabled);
        return reminderRepository.save(reminder);
    }

    @Transactional
    public void deleteReminder(Long reminderId) {
        if (!reminderRepository.existsById(reminderId)) {
            throw new RuntimeException("提醒配置不存在");
        }
        reminderRepository.deleteById(reminderId);
    }

    public ReminderResponseDTO convertToResponseDTO(Reminder reminder) {
        ReminderResponseDTO dto = new ReminderResponseDTO();
        dto.setReminderId(reminder.getReminderId());
        dto.setUserId(reminder.getUserId());
        dto.setPlanId(reminder.getPlanId());
        dto.setReminderTime(reminder.getReminderTime().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        dto.setChannel(reminder.getChannel());
        dto.setEnabled(reminder.getEnabled());
        dto.setLevel(reminder.getLevel());
        return dto;
    }

    public List<ReminderResponseDTO> convertToResponseDTOList(List<Reminder> reminders) {
        List<ReminderResponseDTO> dtoList = new ArrayList<>();
        for (Reminder reminder : reminders) {
            dtoList.add(convertToResponseDTO(reminder));
        }
        return dtoList;
    }
}
