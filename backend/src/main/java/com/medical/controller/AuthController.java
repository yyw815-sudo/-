package com.medical.controller;

import com.medical.dto.Result;
import com.medical.dto.PhoneLoginDTO;
import com.medical.dto.ResetPasswordDTO;
import com.medical.entity.User;
import com.medical.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

/**
 * 认证控制器
 * 负责手机号验证码登录等认证相关功能
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    private final Map<String, String> verificationCodes = new HashMap<>();

    /**
     * 发送短信验证码
     */
    @PostMapping("/send-code")
    public Result<Map<String, Object>> sendCode(@RequestBody Map<String, String> request) {
        String phone = request.get("phone");
        if (phone == null || !phone.matches("^1[3-9]\\d{9}$")) {
            return Result.error("请输入正确的手机号");
        }
        String code = String.format("%06d", (int)(Math.random() * 1000000));
        verificationCodes.put(phone, code);
        System.out.println("【模拟短信】向手机号 " + phone + " 发送验证码：" + code);
        Map<String, Object> data = new HashMap<>();
        data.put("phone", phone);
        data.put("code", code);
        return Result.success("验证码已发送", data);
    }

    /**
     * 手机号登录
     */
    @PostMapping("/phone-login")
    public Result<Map<String, Object>> phoneLogin(@Valid @RequestBody PhoneLoginDTO dto) {
        String savedCode = verificationCodes.get(dto.getPhone());
        if (savedCode == null || !savedCode.equals(dto.getCode())) {
            return Result.error("验证码错误");
        }
        verificationCodes.remove(dto.getPhone());

        User user = userService.getUserByPhone(dto.getPhone());
        if (user == null) {
            return Result.error("该手机号未注册");
        }

        Map<String, Object> data = new HashMap<>();
        data.put("userId", user.getUserId());
        data.put("userName", user.getUserName());
        data.put("realname", user.getRealname());
        data.put("phone", user.getPhone());
        data.put("token", "mock-phone-token-" + user.getUserId());
        return Result.success("登录成功", data);
    }

    /**
     * 重置密码
     */
    @PostMapping("/reset-password")
    public Result<String> resetPassword(@Valid @RequestBody ResetPasswordDTO dto) {
        String savedCode = verificationCodes.get(dto.getPhone());
        if (savedCode == null || !savedCode.equals(dto.getCode())) {
            return Result.error("验证码错误或已过期");
        }
        verificationCodes.remove(dto.getPhone());

        User user = userService.getUserByPhone(dto.getPhone());
        if (user == null) {
            return Result.error("该手机号未注册");
        }

        userService.resetPassword(user.getUserId(), dto.getNewPassword());
        return Result.success("密码重置成功");
    }
}
