package com.petcare.user.controller;

import com.petcare.common.api.ApiResponse;
import com.petcare.common.security.SecurityContextHelper;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Test-only authentication probe for verifying USER JWT flow.
 * Lives in src/test/java — must not be in production code.
 * Requires ROLE_USER — ADMIN tokens will get 403 Forbidden.
 */
@RestController
@RequestMapping("/api/v1/test")
public class UserAuthProbeController {

    @GetMapping("/user-auth-probe")
    @PreAuthorize("hasRole('USER')")
    public ApiResponse<Map<String, Object>> probe() {
        return SecurityContextHelper.getCurrentUserId()
                .map(userId -> ApiResponse.<Map<String, Object>>ok(Map.of("userId", (Object) userId)))
                .orElse(ApiResponse.<Map<String, Object>>ok(Map.of("userId", (Object) "none")));
    }
}
