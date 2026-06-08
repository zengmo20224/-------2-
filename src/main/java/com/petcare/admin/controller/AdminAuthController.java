package com.petcare.admin.controller;

import com.petcare.admin.dto.AdminLoginRequest;
import com.petcare.admin.dto.AdminLoginResponse;
import com.petcare.admin.dto.AdminMeResponse;
import com.petcare.admin.security.AdminPrincipal;
import com.petcare.admin.service.AdminAuthService;
import com.petcare.common.api.ApiResponse;
import com.petcare.common.security.SecurityContextHelper;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Admin authentication endpoints.
 * Login is public; /me requires authentication.
 */
@RestController
@RequestMapping("/api/v1/admin/auth")
public class AdminAuthController {

    private final AdminAuthService adminAuthService;

    public AdminAuthController(AdminAuthService adminAuthService) {
        this.adminAuthService = adminAuthService;
    }

    /**
     * Admin login. Public endpoint (no auth required).
     */
    @PostMapping("/login")
    public ApiResponse<AdminLoginResponse> login(@Valid @RequestBody AdminLoginRequest request) {
        AdminLoginResponse response = adminAuthService.login(request);
        return ApiResponse.ok(response);
    }

    /**
     * Get current admin info. Requires valid Bearer token.
     */
    @GetMapping("/me")
    public ApiResponse<AdminMeResponse> me() {
        AdminPrincipal principal = SecurityContextHelper.getAdminPrincipal()
                .orElseThrow(() -> new IllegalStateException("No authenticated admin found in security context"));
        AdminMeResponse response = adminAuthService.getCurrentAdmin(principal);
        return ApiResponse.ok(response);
    }
}
