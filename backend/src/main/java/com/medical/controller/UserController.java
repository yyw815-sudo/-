package com.medical.controller;

import com.medical.dto.Result;
import com.medical.dto.UserLoginDTO;
import com.medical.dto.UserRegisterDTO;
import com.medical.entity.User;
import com.medical.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户控制器
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result<User> register(@Valid @RequestBody UserRegisterDTO dto) {
        User user = new User();
        user.setUserName(dto.getUserName());
        user.setPassword(dto.getPassword());
        user.setRealname(dto.getRealName());
        user.setPhone(dto.getPhone());

        User savedUser = userService.createUser(user);
        savedUser.setPassword(null); // 不返回密码

        return Result.success("注册成功", savedUser);
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody UserLoginDTO dto) {
        User user = userService.login(dto.getUserName(), dto.getPassword());
        if (user != null) {
            Map<String, Object> data = new HashMap<>();
            data.put("userId", user.getUserId());
            data.put("userName", user.getUserName());
            data.put("realname", user.getRealname());
            data.put("token", "mock-token-" + user.getUserId()); // TODO: 后续实现JWT

            return Result.success("登录成功", data);
        }
        return Result.error("用户名或密码错误");
    }

    /**
     * 获取用户信息
     */
    @GetMapping("/{userId}")
    public Result<User> getUserInfo(@PathVariable Long userId) {
        return userService.getUserById(userId)
                .map(user -> {
                    user.setPassword(null);
                    return Result.success(user);
                })
                .orElse(Result.error("用户不存在"));
    }

    /**
     * 更新用户信息
     */
    @PutMapping("/{userId}")
    public Result<User> updateUser(@PathVariable Long userId, @RequestBody User user) {
        return userService.getUserById(userId)
                .map(existingUser -> {
                    user.setUserId(userId);
                    user.setPassword(existingUser.getPassword()); // 保持原密码
                    User updatedUser = userService.updateUser(user);
                    updatedUser.setPassword(null);
                    return Result.success("更新成功", updatedUser);
                })
                .orElse(Result.error("用户不存在"));
    }
}
