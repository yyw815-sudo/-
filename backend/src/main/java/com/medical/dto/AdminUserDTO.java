package com.medical.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.time.LocalDate;
import java.time.Year;

@Data
public class AdminUserDTO {

    @NotBlank(message = "用户名不能为空", groups = {Create.class})
    @Size(min = 3, max = 20, message = "用户名长度为3-20位")
    private String userName;

    @NotBlank(message = "密码不能为空", groups = {Create.class})
    @Size(min = 6, max = 20, message = "密码长度为6-20位", groups = {Create.class})
    private String password;

    @JsonProperty("realName")
    @Size(max = 50, message = "姓名最多50个字符")
    private String realname;

    private Integer gender;

    private LocalDate birthday;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "请输入正确的手机号")
    private String phone;

    public interface Create {}
    public interface Update {}
}
