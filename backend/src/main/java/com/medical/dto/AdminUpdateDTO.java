package com.medical.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AdminUpdateDTO {

    @Size(max = 50, message = "姓名最多50个字符")
    private String realname;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "请输入正确的手机号")
    private String phone;

    @Size(min = 6, max = 20, message = "密码长度为6-20位")
    private String password;
}