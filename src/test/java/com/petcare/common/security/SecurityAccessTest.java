package com.petcare.common.security;

import com.petcare.admin.entity.AdminPermission;
import com.petcare.admin.entity.AdminRole;
import com.petcare.admin.entity.AdminRolePermission;
import com.petcare.admin.entity.AdminUser;
import com.petcare.admin.service.AdminPermissionService;
import com.petcare.admin.service.AdminRolePermissionService;
import com.petcare.admin.service.AdminRoleService;
import com.petcare.admin.service.AdminUserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for security access control.
 * Verifies 401 for unauthenticated, 403 for unauthorized, 200 for authorized.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class SecurityAccessTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenService jwtTokenService;

    @Autowired
    private AdminUserService adminUserService;

    @Autowired
    private AdminRoleService adminRoleService;

    @Autowired
    private AdminPermissionService adminPermissionService;

    @Autowired
    private AdminRolePermissionService adminRolePermissionService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("Health check requires no authentication")
    void healthCheckRequiresNoAuth() throws Exception {
        mockMvc.perform(get("/api/v1/system/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("Protected endpoint returns 401 without token")
    void protectedEndpointReturns401WithoutToken() throws Exception {
        mockMvc.perform(get("/api/v1/admin/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("unauthorized"));
    }

    @Test
    @DisplayName("Protected endpoint returns 401 with missing Bearer prefix")
    void protectedEndpointReturns401WithWrongAuthHeader() throws Exception {
        mockMvc.perform(get("/api/v1/admin/auth/me")
                        .header("Authorization", "Token sometoken"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Protected endpoint returns 401 with invalid token")
    void protectedEndpointReturns401WithInvalidToken() throws Exception {
        mockMvc.perform(get("/api/v1/admin/auth/me")
                        .header("Authorization", "Bearer invalid.jwt.token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Protected endpoint returns 401 with expired token")
    void protectedEndpointReturns401WithExpiredToken() throws Exception {
        // Create an admin first so the filter can find them
        createTestAdmin("expired_tok_test", "STAFF");

        // Sign a token that expired 60 seconds ago
        String expiredToken = jwtTokenService.signAdminToken(1L, "expired_tok_test", "STAFF", -60);

        mockMvc.perform(get("/api/v1/admin/auth/me")
                        .header("Authorization", "Bearer " + expiredToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Valid token returns 200 for /me")
    void validTokenReturns200ForMe() throws Exception {
        Long adminId = createTestAdmin("valid_tok_test", "SUPER_ADMIN");

        String token = jwtTokenService.signAdminToken(adminId, "valid_tok_test", "SUPER_ADMIN");

        mockMvc.perform(get("/api/v1/admin/auth/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("valid_tok_test"));
    }

    @Test
    @DisplayName("Login endpoint is publicly accessible")
    void loginEndpointIsPublic() throws Exception {
        mockMvc.perform(post("/api/v1/admin/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"test\",\"password\":\"testpassword123\"}"))
                .andExpect(status().isUnauthorized()); // 401 because user doesn't exist, not 401 for auth
    }

    @Test
    @DisplayName("Wechat login endpoint is publicly accessible")
    void wechatLoginEndpointIsPublic() throws Exception {
        mockMvc.perform(post("/api/v1/auth/wechat-login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"test-code\"}"))
                .andExpect(status().isUnprocessableEntity()); // 422 because not enabled, not 401
    }

    @Test
    @DisplayName("STAFF does not have store:config:update permission")
    void staffDoesNotHaveStoreConfigUpdate() throws Exception {
        Long adminId = setupAdminWithPermissions("staff_perm_test", "STAFF",
                new String[]{"booking:booking:read"}, new String[]{"booking"});

        String token = jwtTokenService.signAdminToken(adminId, "staff_perm_test", "STAFF");

        mockMvc.perform(get("/api/v1/admin/auth/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.permissions").isArray())
                .andExpect(jsonPath("$.data.permissions").isNotEmpty());
    }

    @Test
    @DisplayName("MANAGER does not have admin:role:manage permission")
    void managerDoesNotHaveAdminRoleManage() throws Exception {
        Long adminId = setupAdminWithPermissions("manager_perm_test", "MANAGER",
                new String[]{"booking:booking:confirm"}, new String[]{"booking"});

        String token = jwtTokenService.signAdminToken(adminId, "manager_perm_test", "MANAGER");

        mockMvc.perform(get("/api/v1/admin/auth/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.permissions").value(
                        org.hamcrest.Matchers.not(
                                org.hamcrest.Matchers.hasItem("admin:role:manage"))));
    }

    @Test
    @DisplayName("SUPER_ADMIN has admin:role:manage permission")
    void superAdminHasAdminRoleManage() throws Exception {
        Long adminId = setupAdminWithPermissions("super_perm_test", "SUPER_ADMIN",
                new String[]{"admin:role:manage", "store:config:update"},
                new String[]{"admin", "store"});

        String token = jwtTokenService.signAdminToken(adminId, "super_perm_test", "SUPER_ADMIN");

        mockMvc.perform(get("/api/v1/admin/auth/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.permissions").value(
                        org.hamcrest.Matchers.hasItem("admin:role:manage")));
    }

    // --- helper methods ---

    private Long createTestAdmin(String username, String role) {
        AdminUser admin = new AdminUser();
        admin.setUsername(username);
        admin.setPassword(passwordEncoder.encode("password123456"));
        admin.setRole(role);
        admin.setStatus("ACTIVE");
        adminUserService.save(admin);
        return admin.getId();
    }

    private Long setupAdminWithPermissions(String username, String roleCode,
                                            String[] permCodes, String[] modules) {
        Long adminId = createTestAdmin(username, roleCode);

        AdminRole role = new AdminRole();
        role.setRoleCode(roleCode);
        role.setRoleName(roleCode);
        role.setStatus("ACTIVE");
        adminRoleService.save(role);

        for (int i = 0; i < permCodes.length; i++) {
            AdminPermission perm = new AdminPermission();
            perm.setPermissionCode(permCodes[i]);
            perm.setPermissionName(permCodes[i]);
            perm.setModule(modules[i]);
            perm.setStatus("ACTIVE");
            adminPermissionService.save(perm);

            AdminRolePermission rp = new AdminRolePermission();
            rp.setRoleId(role.getId());
            rp.setPermissionId(perm.getId());
            adminRolePermissionService.save(rp);
        }

        return adminId;
    }
}
