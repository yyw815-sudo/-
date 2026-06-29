package com.medical.controller;

import com.medical.dto.Result;
import com.medical.entity.ReminderLog;
import com.medical.service.ReminderLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

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
}
