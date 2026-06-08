package com.petcare.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Admin login request DTO.
 */
public record AdminLoginRequest(

        @NotBlank(message = "用户名不能为空")
        @Size(min = 3, max = 64, message = "用户名长度须在3到64之间")
        String username,

        @NotBlank(message = "密码不能为空")
        @Size(min = 8, max = 128, message = "密码长度须在8到128之间")
        String password
) {
}
