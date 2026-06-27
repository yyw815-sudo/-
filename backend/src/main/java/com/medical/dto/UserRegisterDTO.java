package com.medical.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 用户注册请求DTO
 */
@Data
public class UserRegisterDTO {
    
    @NotBlank(message = "用户名不能为空")
    private String userName;
    
    @NotBlank(message = "密码不能为空")
    private String password;
    
    private String realName;
    private String phone;
    private String email;
}