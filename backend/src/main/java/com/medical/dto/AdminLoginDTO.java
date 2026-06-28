package com.medical.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AdminLoginDTO {

    @NotBlank(message = "请输入管理员账号")
    private String adminName;

    @NotBlank(message = "请输入管理员密码")
    private String password;
}
