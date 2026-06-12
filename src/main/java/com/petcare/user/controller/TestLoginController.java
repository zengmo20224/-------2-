package com.petcare.user.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.petcare.common.api.ApiResponse;
import com.petcare.common.exception.BusinessException;
import com.petcare.common.exception.ErrorCode;
import com.petcare.common.security.JwtTokenService;
import com.petcare.user.dto.TestLoginRequest;
import com.petcare.user.dto.TestLoginResponse;
import com.petcare.user.entity.User;
import com.petcare.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Test-only login endpoint for user E2E testing.
 * Only active when "test" profile is loaded.
 * Non-test profiles will return 404 because this bean does not exist.
 *
 * Only allows existing ACTIVE seed users to login.
 * Does NOT create users, accept arbitrary credentials, or expose private fields.
 */
@RestController
@RequestMapping("/api/v1/auth")
@Profile("test")
public class TestLoginController {

    private final UserService userService;
    private final JwtTokenService jwtTokenService;

    public TestLoginController(UserService userService, JwtTokenService jwtTokenService) {
        this.userService = userService;
        this.jwtTokenService = jwtTokenService;
    }

    @PostMapping("/test-login")
    public ApiResponse<TestLoginResponse> testLogin(@Valid @RequestBody TestLoginRequest request) {
        User user = userService.getOne(
                new LambdaQueryWrapper<User>()
                        .eq(User::getPhone, request.phone())
                        .eq(User::getStatus, "ACTIVE")
        );

        if (user == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "用户不存在或已禁用");
        }

        String accessToken = jwtTokenService.signUserToken(user.getId());

        TestLoginResponse response = new TestLoginResponse(
                "Bearer",
                accessToken,
                jwtTokenService.getExpirationSeconds(),
                new TestLoginResponse.UserInfo(
                        String.valueOf(user.getId()),
                        user.getNickname()
                )
        );

        return ApiResponse.ok(response);
    }
}
