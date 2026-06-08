package com.petcare.admin.service;

import com.petcare.admin.dto.AdminLoginRequest;
import com.petcare.admin.dto.AdminLoginResponse;
import com.petcare.admin.dto.AdminMeResponse;
import com.petcare.admin.security.AdminPrincipal;

/**
 * Admin authentication service interface.
 */
public interface AdminAuthService {

    /**
     * Authenticates an admin and returns a JWT token.
     *
     * @param request login request containing username and password
     * @return login response with token and admin summary
     */
    AdminLoginResponse login(AdminLoginRequest request);

    /**
     * Returns current admin info based on the authenticated principal.
     *
     * @param principal the authenticated admin principal
     * @return admin info with permissions
     */
    AdminMeResponse getCurrentAdmin(AdminPrincipal principal);
}
