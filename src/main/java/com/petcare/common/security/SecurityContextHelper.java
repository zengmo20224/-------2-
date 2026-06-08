package com.petcare.common.security;

import com.petcare.admin.security.AdminPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Utility to extract current admin identity from the Spring Security context.
 * Provides reusable methods for subsequent business phases to check resource ownership.
 */
public final class SecurityContextHelper {

    private SecurityContextHelper() {
        // utility class
    }

    /**
     * Returns the current Authentication if present.
     */
    public static Optional<Authentication> getAuthentication() {
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication());
    }

    /**
     * Returns the AdminPrincipal if the current user is an authenticated admin.
     */
    public static Optional<AdminPrincipal> getAdminPrincipal() {
        return getAuthentication()
                .filter(auth -> auth.getPrincipal() instanceof AdminPrincipal)
                .map(auth -> (AdminPrincipal) auth.getPrincipal());
    }

    /**
     * Returns the current admin's ID, or empty if not authenticated as admin.
     */
    public static Optional<Long> getCurrentAdminId() {
        return getAdminPrincipal().map(AdminPrincipal::getAdminId);
    }

    /**
     * Returns the current admin's role, or empty if not authenticated as admin.
     */
    public static Optional<String> getCurrentAdminRole() {
        return getAdminPrincipal().map(AdminPrincipal::getRole);
    }

    /**
     * Returns the current admin's permission codes, or empty list if not authenticated.
     */
    public static List<String> getCurrentAdminPermissions() {
        return getAdminPrincipal()
                .map(AdminPrincipal::getPermissionCodes)
                .orElse(Collections.emptyList());
    }
}
