package com.medical.controller;

import com.medical.dto.Result;
import com.medical.entity.Admin;
import com.medical.entity.User;
import com.medical.repository.AdminRepository;
import com.medical.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private AdminRepository adminRepository;
    
    @Autowired
    private UserRepository userRepository;

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> loginData) {
        String adminName = loginData.get("username");
        String password = loginData.get("password");

        Optional<Admin> adminOpt = adminRepository.findByAdminName(adminName);
        if (adminOpt.isPresent()) {
            Admin admin = adminOpt.get();
            if (admin.getPassword().equals(password)) {
                Map<String, Object> data = new HashMap<>();
                data.put("adminId", admin.getAdminId());
                data.put("adminName", admin.getAdminName());
                data.put("realname", admin.getRealname());
                data.put("token", "admin-token-" + admin.getAdminId());

                return Result.success("登录成功", data);
            }
        }
        return Result.error("用户名或密码错误");
    }

    @GetMapping("/info/{adminId}")
    public Result<Admin> getAdminInfo(@PathVariable Long adminId) {
        return adminRepository.findById(adminId)
                .map(admin -> {
                    admin.setPassword(null);
                    return Result.success(admin);
                })
                .orElse(Result.error("管理员不存在"));
    }

    @GetMapping("/users")
    public Result<Map<String, Object>> getUserList(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<User> page = userRepository.findAll(pageable);
        
        Map<String, Object> data = new HashMap<>();
        data.put("list", page.getContent());
        data.put("total", page.getTotalElements());
        data.put("pageNum", pageNum);
        data.put("pageSize", pageSize);
        
        return Result.success("查询成功", data);
    }

    @GetMapping("/users/{userId}")
    public Result<User> getUserDetail(@PathVariable Long userId) {
        return userRepository.findById(userId)
                .map(user -> {
                    user.setPassword(null);
                    return Result.success(user);
                })
                .orElse(Result.error("用户不存在"));
    }

    @PostMapping("/users")
    public Result<User> addUser(@RequestBody User user) {
        user.setPassword("123456");
        User saved = userRepository.save(user);
        saved.setPassword(null);
        return Result.success("创建成功", saved);
    }

    @PutMapping("/users/{userId}")
    public Result<User> updateUser(@PathVariable Long userId, @RequestBody User user) {
        return userRepository.findById(userId)
                .map(existing -> {
                    existing.setUserName(user.getUserName());
                    existing.setRealname(user.getRealname());
                    existing.setPhone(user.getPhone());
                    existing.setGender(user.getGender());
                    existing.setBirthday(user.getBirthday());
                    User updated = userRepository.save(existing);
                    updated.setPassword(null);
                    return Result.success("更新成功", updated);
                })
                .orElse(Result.error("用户不存在"));
    }

    @DeleteMapping("/users/{userId}")
    public Result<String> deleteUser(@PathVariable Long userId) {
        if (!userRepository.existsById(userId)) {
            return Result.error("用户不存在");
        }
        userRepository.deleteById(userId);
        return Result.success("删除成功");
    }

    @PutMapping("/users/{userId}/reset-password")
    public Result<String> resetUserPassword(@PathVariable Long userId, @RequestBody Map<String, String> body) {
        return userRepository.findById(userId)
                .map(user -> {
                    user.setPassword(body.get("newPassword"));
                    userRepository.save(user);
                    return Result.success("密码重置成功");
                })
                .orElse(Result.error("用户不存在"));
    }
}