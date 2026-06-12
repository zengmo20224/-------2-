package com.petcare.user.controller;

import com.petcare.common.api.ApiResponse;
import com.petcare.user.auth.TestLoginService;
import com.petcare.user.dto.TestLoginRequest;
import com.petcare.user.dto.TestLoginResponse;
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
 * Controller only handles protocol adaptation (receive request, call service, wrap response).
 * All business logic is in TestLoginService.
 */
@RestController
@RequestMapping("/api/v1/auth")
@Profile("test")
public class TestLoginController {

    private final TestLoginService testLoginService;

    public TestLoginController(TestLoginService testLoginService) {
        this.testLoginService = testLoginService;
    }

    @PostMapping("/test-login")
    public ApiResponse<TestLoginResponse> testLogin(@Valid @RequestBody TestLoginRequest request) {
        TestLoginResponse response = testLoginService.authenticate(request.phone());
        return ApiResponse.ok(response);
    }
}
