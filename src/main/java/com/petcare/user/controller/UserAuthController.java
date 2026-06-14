package com.petcare.user.controller;

import com.petcare.common.api.ApiResponse;
import com.petcare.user.auth.UserAuthService;
import com.petcare.user.auth.WechatLoginProvider;
import com.petcare.user.dto.*;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * User authentication endpoints.
 * Public endpoints: register, login, forgot-password, wechat-login placeholder.
 */
@RestController
@RequestMapping("/api/v1/auth")
public class UserAuthController {

    private final WechatLoginProvider wechatLoginProvider;
    private final UserAuthService userAuthService;

    public UserAuthController(WechatLoginProvider wechatLoginProvider,
                              UserAuthService userAuthService) {
        this.wechatLoginProvider = wechatLoginProvider;
        this.userAuthService = userAuthService;
    }

    /**
     * WeChat login placeholder. Public endpoint (no auth required).
     * V1 returns 422 with wechat_login_not_enabled error code.
     */
    @PostMapping("/wechat-login")
    public ApiResponse<Void> wechatLogin(@Valid @RequestBody WechatLoginRequest request) {
        wechatLoginProvider.login(request.code());
        return ApiResponse.ok(null);
    }

    /**
     * Returns the preset security questions for the registration form.
     * Public endpoint.
     */
    @GetMapping("/security-questions")
    public ApiResponse<List<String>> getPresetSecurityQuestions() {
        return ApiResponse.ok(PresetSecurityQuestions.QUESTIONS);
    }

    /**
     * Register a new user with phone + password + security questions.
     * Public endpoint.
     */
    @PostMapping("/register")
    public ApiResponse<PasswordLoginResponse> register(@Valid @RequestBody RegisterRequest request) {
        PasswordLoginResponse response = userAuthService.register(request);
        return ApiResponse.ok(response);
    }

    /**
     * Login with phone + password. Public endpoint.
     */
    @PostMapping("/login")
    public ApiResponse<PasswordLoginResponse> login(@Valid @RequestBody PasswordLoginRequest request) {
        PasswordLoginResponse response = userAuthService.login(request);
        return ApiResponse.ok(response);
    }

    /**
     * Step 1 of forgot-password: get security questions for a phone number.
     * Public endpoint. Never returns answers.
     */
    @PostMapping("/forgot-password/questions")
    public ApiResponse<List<SecurityQuestionView>> getSecurityQuestions(
            @Valid @RequestBody ForgotPasswordQuestionsRequest request) {
        List<SecurityQuestionView> questions = userAuthService.getSecurityQuestions(request);
        return ApiResponse.ok(questions);
    }

    /**
     * Step 2 of forgot-password: answer questions and reset password.
     * Public endpoint.
     */
    @PostMapping("/forgot-password/reset")
    public ApiResponse<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        userAuthService.resetPassword(request);
        return ApiResponse.ok(null);
    }
}
