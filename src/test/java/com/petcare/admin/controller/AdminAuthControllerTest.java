package com.petcare.admin.controller;

import com.petcare.admin.entity.AdminPermission;
import com.petcare.admin.entity.AdminRole;
import com.petcare.admin.entity.AdminRolePermission;
import com.petcare.admin.entity.AdminUser;
import com.petcare.admin.service.AdminPermissionService;
import com.petcare.admin.service.AdminRolePermissionService;
import com.petcare.admin.service.AdminRoleService;
import com.petcare.admin.service.AdminUserService;
import com.petcare.common.security.JwtTokenService;
import org.junit.jupiter.api.BeforeEach;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for AdminAuthController.
 * Covers login success, wrong password, disabled account, and password not in response.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AdminAuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

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

    @Autowired
    private JwtTokenService jwtTokenService;

    @BeforeEach
    void cleanUp() {
        // Clean up test data to avoid conflicts between tests
    }

    @Test
    @DisplayName("Login success returns token and admin summary")
    void loginSuccessReturnsTokenAndAdminSummary() throws Exception {
        createTestAdmin("logintest", "password123456", "SUPER_ADMIN");

        mockMvc.perform(post("/api/v1/admin/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"logintest\",\"password\":\"password123456\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.expiresInSeconds").isNumber())
                .andExpect(jsonPath("$.data.admin.id").isString())
                .andExpect(jsonPath("$.data.admin.username").value("logintest"))
                .andExpect(jsonPath("$.data.admin.role").value("SUPER_ADMIN"));
    }

    @Test
    @DisplayName("Login response does not contain password")
    void loginResponseDoesNotContainPassword() throws Exception {
        createTestAdmin("nopwdtest", "password123456", "STAFF");

        String response = mockMvc.perform(post("/api/v1/admin/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"nopwdtest\",\"password\":\"password123456\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        // Ensure password is nowhere in the response
        assertThat(response).doesNotContain("password");
        assertThat(response).doesNotContain("$2a$");
    }

    @Test
    @DisplayName("Wrong password returns 401")
    void wrongPasswordReturns401() throws Exception {
        createTestAdmin("wrongpwdtest", "password123456", "STAFF");

        mockMvc.perform(post("/api/v1/admin/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"wrongpwdtest\",\"password\":\"wrongpassword\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("unauthorized"));
    }

    @Test
    @DisplayName("Non-existent user returns 401")
    void nonExistentUserReturns401() throws Exception {
        mockMvc.perform(post("/api/v1/admin/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"ghost\",\"password\":\"password123456\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("unauthorized"));
    }

    @Test
    @DisplayName("Disabled admin cannot login")
    void disabledAdminCannotLogin() throws Exception {
        Long adminId = createTestAdmin("disabledtest", "password123456", "STAFF");
        AdminUser admin = adminUserService.getById(adminId);
        admin.setStatus("DISABLED");
        adminUserService.updateById(admin);

        mockMvc.perform(post("/api/v1/admin/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"disabledtest\",\"password\":\"password123456\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /me with valid token returns admin info")
    void meWithValidTokenReturnsAdminInfo() throws Exception {
        Long adminId = createTestAdmin("metest", "password123456", "SUPER_ADMIN");
        setupRoleAndPermission("SUPER_ADMIN", "admin:role:manage", "admin");

        String token = jwtTokenService.signAdminToken(adminId, "metest", "SUPER_ADMIN");

        mockMvc.perform(get("/api/v1/admin/auth/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(adminId))
                .andExpect(jsonPath("$.data.username").value("metest"))
                .andExpect(jsonPath("$.data.role").value("SUPER_ADMIN"))
                .andExpect(jsonPath("$.data.permissions").isArray());
    }

    @Test
    @DisplayName("GET /me without token returns 401")
    void meWithoutTokenReturns401() throws Exception {
        mockMvc.perform(get("/api/v1/admin/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /me with invalid token returns 401")
    void meWithInvalidTokenReturns401() throws Exception {
        mockMvc.perform(get("/api/v1/admin/auth/me")
                        .header("Authorization", "Bearer invalid.token.here"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Login with missing fields returns 400")
    void loginWithMissingFieldsReturns400() throws Exception {
        mockMvc.perform(post("/api/v1/admin/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("validation_error"));
    }

    // --- helper methods ---

    private Long createTestAdmin(String username, String rawPassword, String role) {
        AdminUser admin = new AdminUser();
        admin.setUsername(username);
        admin.setPassword(passwordEncoder.encode(rawPassword));
        admin.setRole(role);
        admin.setStatus("ACTIVE");
        adminUserService.save(admin);
        return admin.getId();
    }

    private void setupRoleAndPermission(String roleCode, String permCode, String module) {
        AdminRole role = new AdminRole();
        role.setRoleCode(roleCode);
        role.setRoleName(roleCode);
        role.setStatus("ACTIVE");
        adminRoleService.save(role);

        AdminPermission perm = new AdminPermission();
        perm.setPermissionCode(permCode);
        perm.setPermissionName(permCode);
        perm.setModule(module);
        perm.setStatus("ACTIVE");
        adminPermissionService.save(perm);

        AdminRolePermission rp = new AdminRolePermission();
        rp.setRoleId(role.getId());
        rp.setPermissionId(perm.getId());
        adminRolePermissionService.save(rp);
    }

}
