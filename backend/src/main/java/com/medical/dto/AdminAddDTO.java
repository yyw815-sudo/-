package com.medical.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AdminAddDTO {

    @NotBlank(message = "管理员账号不能为空")
    @Size(min = 3, max = 20, message = "管理员账号长度为3-20位")
    private String adminName;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度为6-20位")
    private String password;

    @Size(max = 50, message = "姓名最多50个字符")
    private String realname;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "请输入正确的手机号")
    private String phone;
}
