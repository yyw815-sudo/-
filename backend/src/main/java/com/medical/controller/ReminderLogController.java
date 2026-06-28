package com.medical.controller;

import com.medical.dto.Result;
import com.medical.entity.ReminderLog;
import com.medical.service.ReminderLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/reminder-log")
public class ReminderLogController {

    @Autowired
    private ReminderLogService reminderLogService;

    @GetMapping("/user/{userId}")
    public Result<List<ReminderLog>> getLogsByUserId(@PathVariable Long userId) {
        List<ReminderLog> logs = reminderLogService.getLogsByUserId(userId);
        return Result.success(logs);
    }

    @GetMapping("/reminder/{reminderId}")
    public Result<List<ReminderLog>> getLogsByReminderId(@PathVariable Long reminderId) {
        List<ReminderLog> logs = reminderLogService.getLogsByReminderId(reminderId);
        return Result.success(logs);
    }

    @PutMapping("/{logId}/status")
    public Result<ReminderLog> updateLogStatus(@PathVariable Long logId, @RequestBody Map<String, Object> request) {
        String status = (String) request.get("status");
        String response = (String) request.get("response");
        ReminderLog log = reminderLogService.updateLogStatus(logId, status, response);
        return Result.success("状态更新成功", log);
    }

    @PutMapping("/{logId}/received")
    public Result<ReminderLog> markAsReceived(@PathVariable Long logId) {
        ReminderLog log = reminderLogService.updateLogStatus(logId, "received", null);
        return Result.success("已标记为已接收", log);
    }

    @PutMapping("/{logId}/read")
    public Result<ReminderLog> markAsRead(@PathVariable Long logId, @RequestBody(required = false) Map<String, Object> request) {
        String response = request != null ? (String) request.get("response") : null;
        ReminderLog log = reminderLogService.updateLogStatus(logId, "read", response);
        return Result.success("已标记为已读", log);
    }
}
