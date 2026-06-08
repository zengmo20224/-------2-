package com.petcare.user.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * WeChat login request DTO (placeholder).
 * V1 does not implement real WeChat login.
 */
public record WechatLoginRequest(

        @NotBlank(message = "微信授权码不能为空")
        String code
) {
}
