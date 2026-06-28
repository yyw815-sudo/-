package com.medical.controller;

import com.medical.dto.Result;
import com.medical.dto.UserLoginDTO;
import com.medical.dto.UserRegisterDTO;
import com.medical.entity.User;
import com.medical.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody UserLoginDTO loginDTO) {
        User user = userService.findByUserName(loginDTO.getUserName());
        if (user == null || !user.getPassword().equals(loginDTO.getPassword())) {
            return Result.error(401, "用户名或密码错误");
        }
        Map<String, Object> data = new HashMap<>();
        data.put("userId", user.getUserId());
        data.put("userName", user.getUserName());
        return Result.success(data);
    }

    @PostMapping("/register")
    public Result<User> register(@RequestBody UserRegisterDTO registerDTO) {
        if (userService.findByUserName(registerDTO.getUserName()) != null) {
            return Result.error(400, "用户名已存在");
        }
        User user = new User();
        user.setUserName(registerDTO.getUserName());
        user.setPassword(registerDTO.getPassword());
        if (registerDTO.getRealName() != null) user.setRealname(registerDTO.getRealName());
        if (registerDTO.getPhone() != null) user.setPhone(registerDTO.getPhone());
        return Result.success(userService.register(user));
    }

    @GetMapping("/{userId}")
    public Result<User> getUserById(@PathVariable Long userId) {
        User user = userService.getUserById(userId);
        return user != null ? Result.success(user) : Result.error(404, "用户不存在");
    }
}
