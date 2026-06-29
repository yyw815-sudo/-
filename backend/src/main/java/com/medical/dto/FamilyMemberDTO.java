package com.medical.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class FamilyMemberDTO {

    private Long familyUserId;

    @NotBlank(message = "家属姓名不能为空")
    @Size(max = 50, message = "姓名最多50个字符")
    private String realname;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "请输入正确的手机号")
    private String phone;

    @NotBlank(message = "关系不能为空")
    @Size(max = 50, message = "关系最多50个字符")
    private String relation;

    @Size(max = 20, message = "权限级别最多20个字符")
    private String permissionLevel = "basic";
}