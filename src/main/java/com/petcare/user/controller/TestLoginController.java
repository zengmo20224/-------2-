package com.petcare.user.controller;

import com.petcare.common.api.ApiResponse;
import com.petcare.common.exception.BusinessException;
import com.petcare.common.exception.ErrorCode;
import com.petcare.common.security.JwtTokenService;
import com.petcare.user.dto.TestLoginRequest;
import com.petcare.user.dto.TestLoginResponse;
import com.petcare.user.entity.User;
import com.petcare.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

/**
 * Test-only login endpoint for user E2E testing.
 * Only active when "test" profile is loaded.
 * Non-test profiles will return 404 because this bean does not exist.
 *
 * Only allows existing ACTIVE seed users whose phone is in the allowlist.
 * Does NOT create users, accept arbitrary credentials, or expose private fields.
 */
@RestController
@RequestMapping("/api/v1/auth")
@Profile("test")
public class TestLoginController {

    private final UserService userService;
    private final JwtTokenService jwtTokenService;
    private final Set<String> allowedPhones;

    public TestLoginController(UserService userService,
                               JwtTokenService jwtTokenService,
                               @Value("${petcare.test-login.allowed-phones:}") Set<String> allowedPhones) {
        this.userService = userService;
        this.jwtTokenService = jwtTokenService;
        this.allowedPhones = allowedPhones;
    }

    @PostMapping("/test-login")
    public ApiResponse<TestLoginResponse> testLogin(@Valid @RequestBody TestLoginRequest request) {
        if (!allowedPhones.contains(request.phone())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "用户不存在或已禁用");
        }

        User user = userService.lambdaQuery()
                .eq(User::getPhone, request.phone())
                .eq(User::getStatus, "ACTIVE")
                .one();

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
