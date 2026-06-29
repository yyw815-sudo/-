package com.medical.controller;

import com.medical.dto.FamilyAuthDTO;
import com.medical.dto.FamilyMemberDTO;
import com.medical.dto.Result;
import com.medical.entity.FamilyAuth;
import com.medical.entity.FamilyMember;
import com.medical.entity.ReminderLog;
import com.medical.service.FamilyService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/family")
public class FamilyController {

    @Autowired
    private FamilyService familyService;

    @GetMapping("/members")
    public Result<List<FamilyMember>> getFamilyMembers(@RequestParam Long userId) {
        List<FamilyMember> members = familyService.getFamilyMembers(userId);
        return Result.success("查询成功", members);
    }

    @GetMapping("/members/{memberId}")
    public Result<FamilyMember> getFamilyMember(@RequestParam Long userId, @PathVariable Long memberId) {
        return familyService.getFamilyMember(userId, memberId)
                .map(member -> Result.success(member))
                .orElse(Result.error("家属成员不存在"));
    }

    @PostMapping("/members")
    public Result<FamilyMember> addFamilyMember(@RequestParam Long userId, @Valid @RequestBody FamilyMemberDTO dto) {
        try {
            FamilyMember member = familyService.addFamilyMember(userId, dto);
            return Result.success("添加成功", member);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/members/{memberId}")
    public Result<FamilyMember> updateFamilyMember(@RequestParam Long userId, @PathVariable Long memberId, @RequestBody FamilyMemberDTO dto) {
        try {
            FamilyMember member = familyService.updateFamilyMember(userId, memberId, dto);
            return Result.success("更新成功", member);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/members/{memberId}")
    public Result<String> deleteFamilyMember(@RequestParam Long userId, @PathVariable Long memberId) {
        try {
            familyService.deleteFamilyMember(userId, memberId);
            return Result.success("删除成功");
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/auth")
    public Result<List<FamilyAuth>> getFamilyAuth(@RequestParam Long userId) {
        List<FamilyAuth> authList = familyService.getAllFamilyAuth(userId);
        return Result.success("查询成功", authList);
    }

    @GetMapping("/auth/{memberId}")
    public Result<FamilyAuth> getFamilyAuthByMember(@RequestParam Long userId, @PathVariable Long memberId) {
        try {
            FamilyAuth auth = familyService.getFamilyAuth(userId, memberId);
            return Result.success(auth);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/auth")
    public Result<FamilyAuth> updateFamilyAuth(@RequestParam Long userId, @Valid @RequestBody FamilyAuthDTO dto) {
        try {
            FamilyAuth auth = familyService.updateFamilyAuth(userId, dto);
            return Result.success("权限更新成功", auth);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/reminders/{memberId}")
    public Result<List<ReminderLog>> getFamilyReminders(@RequestParam Long userId, @PathVariable Long memberId) {
        try {
            List<ReminderLog> logs = familyService.getFamilyReminderLogs(userId, memberId);
            return Result.success("查询成功", logs);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/reminders")
    public Result<List<ReminderLog>> getAllFamilyReminders(@RequestParam Long userId) {
        try {
            List<FamilyMember> members = familyService.getFamilyMembers(userId);
            List<ReminderLog> allLogs = new java.util.ArrayList<>();
            for (FamilyMember member : members) {
                if (member.getFamilyUserId() != null) {
                    List<ReminderLog> logs = familyService.getReminderLogsByFamilyUserId(member.getFamilyUserId());
                    for (ReminderLog log : logs) {
                        log.setRemark(member.getRealname());
                    }
                    allLogs.addAll(logs);
                }
            }
            allLogs.sort((a, b) -> b.getCreateTime().compareTo(a.getCreateTime()));
            return Result.success("查询成功", allLogs);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/members/search")
    public Result<List<FamilyMember>> searchFamilyMembers(@RequestParam Long userId, @RequestParam(required = false) String keyword) {
        List<FamilyMember> members = familyService.getFamilyMembers(userId);
        if (keyword == null || keyword.trim().isEmpty()) {
            return Result.success("查询成功", members);
        }
        String k = keyword.toLowerCase();
        List<FamilyMember> filtered = members.stream()
                .filter(m -> (m.getRealname() != null && m.getRealname().toLowerCase().contains(k))
                        || (m.getPhone() != null && m.getPhone().contains(k))
                        || (m.getRelation() != null && m.getRelation().toLowerCase().contains(k)))
                .collect(java.util.stream.Collectors.toList());
        return Result.success("查询成功", filtered);
    }
}