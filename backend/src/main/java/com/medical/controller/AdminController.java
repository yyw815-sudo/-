package com.medical.controller;

import com.medical.dto.AdminAddDTO;
import com.medical.dto.AdminLoginDTO;
import com.medical.dto.AdminProfileDTO;
import com.medical.dto.AdminUpdateDTO;
import com.medical.dto.AdminUserDTO;
import com.medical.dto.Result;
import com.medical.entity.Admin;
import com.medical.entity.User;
import com.medical.service.AdminService;
import com.medical.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private UserService userService;

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

    @GetMapping("/{adminId}")
    public Result<Admin> getAdminInfo(@PathVariable Long adminId) {
        return adminService.getAdminById(adminId)
                .map(admin -> Result.success(admin))
                .orElse(Result.error("管理员不存在"));
    }

    @GetMapping("/users")
    public Result<Map<String, Object>> getUserList(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword) {
        Page<User> page = userService.getUserPage(pageNum, pageSize, keyword);
        Map<String, Object> data = new HashMap<>();
        data.put("list", page.getContent());
        data.put("total", page.getTotalElements());
        data.put("pageNum", pageNum);
        data.put("pageSize", pageSize);
        return Result.success("查询成功", data);
    }

    @GetMapping("/users/{userId}")
    public Result<User> getUserDetail(@PathVariable Long userId) {
        return userService.getUserById(userId)
                .map(user -> Result.success(user))
                .orElse(Result.error("用户不存在"));
    }

    @PostMapping("/users")
    public Result<User> addUser(@Valid @RequestBody AdminUserDTO dto) {
        try {
            User user = new User();
            user.setUserName(dto.getUserName());
            user.setPassword(dto.getPassword());
            user.setRealname(dto.getRealname());
            user.setGender(dto.getGender());
            user.setBirthday(dto.getBirthday());
            user.setPhone(dto.getPhone());
            User saved = userService.createUser(user);
            return Result.success("添加成功", saved);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/users/{userId}")
    public Result<User> updateUser(@PathVariable Long userId, @Valid @RequestBody AdminUserDTO dto) {
        return userService.getUserById(userId)
                .map(user -> {
                    if (dto.getRealname() != null) user.setRealname(dto.getRealname());
                    if (dto.getGender() != null) user.setGender(dto.getGender());
                    if (dto.getBirthday() != null) user.setBirthday(dto.getBirthday());
                    if (dto.getPhone() != null) user.setPhone(dto.getPhone());
                    User updated = userService.updateUser(user);
                    return Result.success("更新成功", updated);
                })
                .orElse(Result.error("用户不存在"));
    }

    @DeleteMapping("/users/{userId}")
    public Result<String> deleteUser(@PathVariable Long userId) {
        if (!userService.getUserById(userId).isPresent()) {
            return Result.error("用户不存在");
        }
        userService.deleteUser(userId);
        return Result.success("删除成功");
    }

    @PutMapping("/users/{userId}/reset-password")
    public Result<String> resetPassword(@PathVariable Long userId, @RequestBody Map<String, String> request) {
        String newPassword = request.get("newPassword");
        if (newPassword == null || newPassword.length() < 6) {
            return Result.error("新密码至少6位");
        }
        try {
            userService.resetPassword(userId, newPassword);
            return Result.success("密码重置成功");
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/profile/{adminId}")
    public Result<Admin> updateAdminProfile(@PathVariable Long adminId, @Valid @RequestBody AdminProfileDTO dto) {
        return adminService.getAdminById(adminId)
                .map(admin -> {
                    if (dto.getRealname() != null) admin.setRealname(dto.getRealname());
                    if (dto.getPhone() != null) admin.setPhone(dto.getPhone());
                    Admin updated = adminService.updateAdmin(admin);
                    return Result.success("个人信息更新成功", updated);
                })
                .orElse(Result.error("管理员不存在"));
    }

    @GetMapping("/account/{adminId}")
    public Result<String> deleteAdminAccount(@PathVariable Long adminId) {
        if (!adminService.getAdminById(adminId).isPresent()) {
            return Result.error("管理员不存在");
        }
        adminService.deleteAdmin(adminId);
        return Result.success("账号注销成功");
    }

    @GetMapping("/admins")
    public Result<Map<String, Object>> getAdminList(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword) {
        Page<Admin> page = adminService.getAdminPage(pageNum, pageSize, keyword);
        Map<String, Object> data = new HashMap<>();
        data.put("list", page.getContent());
        data.put("total", page.getTotalElements());
        data.put("pageNum", pageNum);
        data.put("pageSize", pageSize);
        return Result.success("查询成功", data);
    }

    @PostMapping("/admins")
    public Result<Admin> addAdmin(@Valid @RequestBody AdminAddDTO dto) {
        try {
            Admin admin = new Admin();
            admin.setAdminName(dto.getAdminName());
            admin.setPassword(dto.getPassword());
            admin.setRealname(dto.getRealname());
            admin.setPhone(dto.getPhone());
            Admin saved = adminService.createAdmin(admin);
            return Result.success("添加成功", saved);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/admins/{adminId}")
    public Result<Admin> updateAdmin(@PathVariable Long adminId, @Valid @RequestBody AdminUpdateDTO dto) {
        return adminService.getAdminById(adminId)
                .map(admin -> {
                    if (dto.getRealname() != null) admin.setRealname(dto.getRealname());
                    if (dto.getPhone() != null) admin.setPhone(dto.getPhone());
                    if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
                        admin.setPassword(dto.getPassword());
                    }
                    Admin updated = adminService.updateAdmin(admin);
                    return Result.success("更新成功", updated);
                })
                .orElse(Result.error("管理员不存在"));
    }

    @DeleteMapping("/admins/{adminId}")
    public Result<String> deleteAdmin(@PathVariable Long adminId) {
        if (!adminService.getAdminById(adminId).isPresent()) {
            return Result.error("管理员不存在");
        }
        adminService.deleteAdmin(adminId);
        return Result.success("删除成功");
    }

    @PutMapping("/admins/{adminId}/reset-password")
    public Result<String> resetAdminPassword(@PathVariable Long adminId, @RequestBody Map<String, String> request) {
        String newPassword = request.get("newPassword");
        if (newPassword == null || newPassword.length() < 6) {
            return Result.error("新密码至少6位");
        }
        try {
            adminService.resetPassword(adminId, newPassword);
            return Result.success("密码重置成功");
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }
}