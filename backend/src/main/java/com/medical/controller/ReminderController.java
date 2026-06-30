package com.medical.controller;

import com.medical.dto.ReminderDTO;
import com.medical.dto.ReminderResponseDTO;
import com.medical.dto.Result;
import com.medical.entity.Reminder;
import com.medical.service.ReminderService;
import com.medical.service.ReminderLogService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/reminder")
public class ReminderController {

    @Autowired
    private ReminderService reminderService;

    @Autowired
    private ReminderLogService reminderLogService;

    @PostMapping("/create")
    public Result<ReminderResponseDTO> createReminder(@Valid @RequestBody ReminderDTO dto) {
        Reminder reminder = reminderService.createReminder(dto);
        return Result.success("提醒配置创建成功", reminderService.convertToResponseDTO(reminder));
    }

    @PostMapping("/create-from-plan")
    public Result<List<ReminderResponseDTO>> createRemindersFromPlan(@RequestBody Map<String, Object> data) {
        try {
            Long userId = Long.valueOf(data.get("userId").toString());
            Long planId = data.get("planId") != null ? Long.valueOf(data.get("planId").toString()) : null;
            String reminderTime = (String) data.get("reminderTime");
            boolean includeApp = data.get("includeApp") != null && Boolean.parseBoolean(data.get("includeApp").toString());
            boolean includeSms = data.get("includeSms") != null && Boolean.parseBoolean(data.get("includeSms").toString());
            boolean includePhone = data.get("includePhone") != null && Boolean.parseBoolean(data.get("includePhone").toString());
            
            List<Reminder> reminders = reminderService.createRemindersFromPlan(
                userId, planId, reminderTime, includeApp, includeSms, includePhone
            );
            
            return Result.success("提醒创建成功", reminderService.convertToResponseDTOList(reminders));
        } catch (Exception e) {
            return Result.error("创建失败: " + e.getMessage());
        }
    }

    @PutMapping("/{reminderId}")
    public Result<ReminderResponseDTO> updateReminder(@PathVariable Long reminderId, @RequestBody ReminderDTO dto) {
        Reminder reminder = reminderService.updateReminder(reminderId, dto);
        return Result.success("提醒配置更新成功", reminderService.convertToResponseDTO(reminder));
    }

    @GetMapping("/user/{userId}")
    public Result<List<ReminderResponseDTO>> getRemindersByUserId(@PathVariable Long userId) {
        List<Reminder> reminders = reminderService.getRemindersByUserId(userId);
        return Result.success(reminderService.convertToResponseDTOList(reminders));
    }

    @GetMapping("/user/{userId}/enabled")
    public Result<List<ReminderResponseDTO>> getEnabledRemindersByUserId(@PathVariable Long userId) {
        List<Reminder> reminders = reminderService.getEnabledRemindersByUserId(userId);
        return Result.success(reminderService.convertToResponseDTOList(reminders));
    }

    @GetMapping("/{reminderId}")
    public Result<ReminderResponseDTO> getReminderById(@PathVariable Long reminderId) {
        return reminderService.getReminderById(reminderId)
                .map(reminder -> Result.success(reminderService.convertToResponseDTO(reminder)))
                .orElse(Result.error("提醒配置不存在"));
    }

    @PutMapping("/{reminderId}/toggle")
    public Result<ReminderResponseDTO> toggleReminder(@PathVariable Long reminderId, @RequestParam Integer enabled) {
        Reminder reminder = reminderService.toggleReminder(reminderId, enabled);
        String message = enabled == 1 ? "提醒已启用" : "提醒已禁用";
        return Result.success(message, reminderService.convertToResponseDTO(reminder));
    }

    @DeleteMapping("/{reminderId}")
    public Result<Void> deleteReminder(@PathVariable Long reminderId) {
        reminderService.deleteReminder(reminderId);
        return new Result<>(200, "提醒配置已删除", null);
    }

    @PostMapping("/send/{reminderId}")
    public Result<Map<String, Object>> sendReminder(@PathVariable Long reminderId) {
        try {
            Map<String, Object> result = reminderLogService.sendReminder(reminderId);
            return Result.success("提醒已发送", result);
        } catch (Exception e) {
            return Result.error("发送失败: " + e.getMessage());
        }
    }
}
