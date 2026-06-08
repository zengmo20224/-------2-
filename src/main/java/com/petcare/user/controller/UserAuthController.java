package com.petcare.user.controller;

import com.petcare.common.api.ApiResponse;
import com.petcare.user.auth.WechatLoginProvider;
import com.petcare.user.dto.WechatLoginRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * User authentication endpoints.
 * V1 only provides WeChat login placeholder.
 */
@RestController
@RequestMapping("/api/v1/auth")
public class UserAuthController {

    private final WechatLoginProvider wechatLoginProvider;

    public UserAuthController(WechatLoginProvider wechatLoginProvider) {
        this.wechatLoginProvider = wechatLoginProvider;
    }

    /**
     * WeChat login placeholder. Public endpoint (no auth required).
     * V1 returns 422 with wechat_login_not_enabled error code.
     * Does NOT create users, generate openid, or issue tokens.
     */
    @PostMapping("/wechat-login")
    public ApiResponse<Void> wechatLogin(@Valid @RequestBody WechatLoginRequest request) {
        // This will throw BusinessException with WECHAT_LOGIN_NOT_ENABLED
        wechatLoginProvider.login(request.code());
        // Unreachable in V1, but kept for future implementation
        return ApiResponse.ok(null);
    }
}
