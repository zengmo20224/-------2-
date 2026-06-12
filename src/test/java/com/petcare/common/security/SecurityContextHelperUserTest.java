package com.petcare.common.security;

import com.petcare.user.security.UserPrincipal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * RED-1 tests: SecurityContextHelper user identity methods.
 * These tests define the contract for getUserPrincipal and getCurrentUserId.
 */
class SecurityContextHelperUserTest {

    @Test
    @DisplayName("getUserPrincipal returns UserPrincipal when user is authenticated")
    void getUserPrincipalReturnsUserPrincipal() {
        UserPrincipal userPrincipal = new UserPrincipal(1001L);
        setAuthentication(userPrincipal);

        assertThat(SecurityContextHelper.getUserPrincipal())
                .isPresent()
                .get()
                .extracting(UserPrincipal::getUserId)
                .isEqualTo(1001L);

        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("getCurrentUserId returns user ID when user is authenticated")
    void getCurrentUserIdReturnsUserId() {
        UserPrincipal userPrincipal = new UserPrincipal(2001L);
        setAuthentication(userPrincipal);

        assertThat(SecurityContextHelper.getCurrentUserId())
                .isPresent()
                .hasValue(2001L);

        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("getUserPrincipal returns empty when admin is authenticated")
    void getUserPrincipalReturnsEmptyForAdmin() {
        com.petcare.admin.security.AdminPrincipal adminPrincipal =
                new com.petcare.admin.security.AdminPrincipal(1L, "admin", "SUPER_ADMIN", List.of());
        setAuthentication(adminPrincipal);

        assertThat(SecurityContextHelper.getUserPrincipal()).isEmpty();
        assertThat(SecurityContextHelper.getCurrentUserId()).isEmpty();

        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("getUserPrincipal returns empty when no authentication")
    void getUserPrincipalReturnsEmptyWhenNoAuth() {
        SecurityContextHolder.clearContext();

        assertThat(SecurityContextHelper.getUserPrincipal()).isEmpty();
        assertThat(SecurityContextHelper.getCurrentUserId()).isEmpty();
    }

    @Test
    @DisplayName("getAdminPrincipal still works after adding user methods")
    void adminMethodsStillWorkAfterUserAddition() {
        com.petcare.admin.security.AdminPrincipal adminPrincipal =
                new com.petcare.admin.security.AdminPrincipal(1L, "admin", "SUPER_ADMIN", List.of("booking:booking:read"));
        setAuthentication(adminPrincipal);

        assertThat(SecurityContextHelper.getAdminPrincipal()).isPresent();
        assertThat(SecurityContextHelper.getCurrentAdminId()).hasValue(1L);
        assertThat(SecurityContextHelper.getCurrentAdminRole()).hasValue("SUPER_ADMIN");

        SecurityContextHolder.clearContext();
    }

    private void setAuthentication(Object principal) {
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(principal, null, List.of());
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);
    }
}
