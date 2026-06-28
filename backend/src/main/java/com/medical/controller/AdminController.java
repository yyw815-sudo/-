package com.medical.controller;

import com.medical.dto.AdminLoginDTO;
import com.medical.dto.Result;
import com.medical.entity.Admin;
import com.medical.service.AdminService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

/**
 * 管理员控制器
 */
@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    /**
     * 管理员登录
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody AdminLoginDTO dto) {
        Admin admin = adminService.login(dto.getAdminName(), dto.getPassword());
        if (admin != null) {
            Map<String, Object> data = new HashMap<>();
            data.put("adminId", admin.getAdminId());
            data.put("adminName", admin.getAdminName());
            data.put("realname", admin.getRealname());
            data.put("token", "admin-token-" + admin.getAdminId());
            data.put("role", "ADMIN");

            return Result.success("登录成功", data);
        }
        return Result.error("管理员账号或密码错误");
    }

    /**
     * 获取管理员信息
     */
    @GetMapping("/{adminId}")
    public Result<Admin> getAdminInfo(@PathVariable Long adminId) {
        return adminService.getAdminById(adminId)
                .map(admin -> {
                    admin.setPassword(null);
                    return Result.success(admin);
                })
                .orElse(Result.error("管理员不存在"));
    }
}