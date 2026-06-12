package com.petcare.user.controller;

import com.petcare.common.config.SecurityProperties;
import com.petcare.common.security.JwtTokenService;
import com.petcare.user.entity.User;
import com.petcare.user.service.UserService;
import com.petcare.admin.entity.AdminUser;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * RED-2: Integration tests for test-login endpoint and USER JWT authentication.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class TestLoginControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenService jwtTokenService;

    @Autowired
    private UserService userService;

    @Autowired
    private AdminUserService adminUserService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // === Test Login Success ===

    @Test
    @DisplayName("test-login with ACTIVE user returns token and user info")
    void testLoginWithActiveUserReturnsToken() throws Exception {
        createUser("13800138001", "测试用户", "ACTIVE");

        mockMvc.perform(post("/api/v1/auth/test-login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phone\":\"13800138001\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.expiresInSeconds").isNumber())
                .andExpect(jsonPath("$.data.user.id").isString())
                .andExpect(jsonPath("$.data.user.nickname").value("测试用户"));
    }

    @Test
    @DisplayName("test-login response does not expose phone, openid, or unionid")
    void testLoginResponseDoesNotExposePrivateFields() throws Exception {
        createUser("13800138001", "测试用户", "ACTIVE");

        mockMvc.perform(post("/api/v1/auth/test-login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phone\":\"13800138001\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.user.phone").doesNotExist())
                .andExpect(jsonPath("$.data.user.openid").doesNotExist())
                .andExpect(jsonPath("$.data.user.unionid").doesNotExist());
    }

    @Test
    @DisplayName("test-login USER token can access user auth probe")
    void testLoginTokenAccessesUserAuthProbe() throws Exception {
        User user = createUser("13800138001", "探针用户", "ACTIVE");

        String token = jwtTokenService.signUserToken(user.getId());

        mockMvc.perform(get("/api/v1/test/user-auth-probe")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userId").isNumber());
    }

    // === Test Login Failure ===

    @Test
    @DisplayName("test-login with non-existent phone returns 401 and does not create user")
    void testLoginWithNonExistentPhoneReturns401() throws Exception {
        long userCountBefore = userService.count();

        mockMvc.perform(post("/api/v1/auth/test-login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phone\":\"19999999999\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("unauthorized"));

        assertThat(userService.count()).isEqualTo(userCountBefore);
    }

    @Test
    @DisplayName("test-login with disabled user returns 401")
    void testLoginWithDisabledUserReturns401() throws Exception {
        createUser("13800138002", "禁用用户", "DISABLED");

        mockMvc.perform(post("/api/v1/auth/test-login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phone\":\"13800138002\"}"))
                .andExpect(status().isUnauthorized());
    }

    // === Validation ===

    @Test
    @DisplayName("test-login with missing phone returns 400")
    void testLoginWithMissingPhoneReturns400() throws Exception {
        mockMvc.perform(post("/api/v1/auth/test-login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phone\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("validation_error"));
    }

    @Test
    @DisplayName("test-login with invalid phone format returns 400")
    void testLoginWithInvalidPhoneFormatReturns400() throws Exception {
        mockMvc.perform(post("/api/v1/auth/test-login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phone\":\"123\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("validation_error"));
    }

    // === Token Type Isolation ===

    @Test
    @DisplayName("ADMIN token cannot access user auth probe (403 Forbidden)")
    void adminTokenCannotAccessUserAuthProbe() throws Exception {
        Long adminId = createTestAdmin("probe_admin", "SUPER_ADMIN");
        String adminToken = jwtTokenService.signAdminToken(adminId, "probe_admin", "SUPER_ADMIN");

        mockMvc.perform(get("/api/v1/test/user-auth-probe")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("No token returns 401 for user auth probe")
    void noTokenReturns401ForUserAuthProbe() throws Exception {
        mockMvc.perform(get("/api/v1/test/user-auth-probe"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Invalid token returns 401 for user auth probe")
    void invalidTokenReturns401ForUserAuthProbe() throws Exception {
        mockMvc.perform(get("/api/v1/test/user-auth-probe")
                        .header("Authorization", "Bearer invalid.jwt.token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Expired USER token returns 401")
    void expiredUserTokenReturns401() throws Exception {
        User user = createUser("13800138003", "过期测试", "ACTIVE");
        String expiredToken = jwtTokenService.signUserToken(user.getId(), -60);

        mockMvc.perform(get("/api/v1/test/user-auth-probe")
                        .header("Authorization", "Bearer " + expiredToken))
                .andExpect(status().isUnauthorized());
    }

    // === Helper ===

    // --- Token State Change Regression (HIGH-1) ---

    @Test
    @DisplayName("USER token returns 401 after user is disabled")
    void userTokenReturns401AfterUserDisabled() throws Exception {
        User user = createUser("13800138010", "禁用回归", "ACTIVE");
        String token = jwtTokenService.signUserToken(user.getId());

        user.setStatus("DISABLED");
        userService.updateById(user);

        mockMvc.perform(get("/api/v1/test/user-auth-probe")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("USER token returns 401 after user is logically deleted")
    void userTokenReturns401AfterUserDeleted() throws Exception {
        User user = createUser("13800138011", "删除回归", "ACTIVE");
        String token = jwtTokenService.signUserToken(user.getId());

        userService.removeById(user.getId());

        mockMvc.perform(get("/api/v1/test/user-auth-probe")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("ADMIN token returns 401 after admin is disabled")
    void adminTokenReturns401AfterAdminDisabled() throws Exception {
        Long adminId = createTestAdmin("disabled_admin_test", "SUPER_ADMIN");
        String token = jwtTokenService.signAdminToken(adminId, "disabled_admin_test", "SUPER_ADMIN");

        AdminUser admin = adminUserService.getById(adminId);
        admin.setStatus("DISABLED");
        adminUserService.updateById(admin);

        mockMvc.perform(get("/api/v1/admin/auth/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    // --- Issuer Validation (MEDIUM-2) ---

    @Test
    @DisplayName("Token signed with wrong issuer returns 401")
    void tokenWithWrongIssuerReturns401() throws Exception {
        SecurityProperties wrongIssuerProps = new SecurityProperties(
                "test-secret-key-for-unit-tests-at-least-32-chars",
                "malicious-issuer",
                120
        );
        JwtTokenService wrongService = new JwtTokenService(wrongIssuerProps);
        String wrongToken = wrongService.signUserToken(9999L);

        mockMvc.perform(get("/api/v1/test/user-auth-probe")
                        .header("Authorization", "Bearer " + wrongToken))
                .andExpect(status().isUnauthorized());
    }

    // --- Token Type Validation (MEDIUM-2) ---

    @Test
    @DisplayName("Token with missing tokenType returns 401")
    void tokenWithMissingTokenTypeReturns401() throws Exception {
        String noTypeToken = io.jsonwebtoken.Jwts.builder()
                .subject("9999")
                .issuer("petcare-o2o-api-test")
                .issuedAt(new java.util.Date())
                .expiration(new java.util.Date(System.currentTimeMillis() + 3600000))
                .signWith(io.jsonwebtoken.security.Keys.hmacShaKeyFor(
                        "test-secret-key-for-unit-tests-at-least-32-chars"
                                .getBytes(java.nio.charset.StandardCharsets.UTF_8)))
                .compact();

        mockMvc.perform(get("/api/v1/test/user-auth-probe")
                        .header("Authorization", "Bearer " + noTypeToken))
                .andExpect(status().isUnauthorized());
    }

    // --- Allowlist Tests (HIGH-3) ---

    @Test
    @DisplayName("test-login rejects ACTIVE user not in allowlist")
    void testLoginRejectsActiveUserNotInAllowlist() throws Exception {
        createUser("13800138999", "不在名单", "ACTIVE");

        mockMvc.perform(post("/api/v1/auth/test-login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phone\":\"13800138999\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("test-login rejects logically deleted user and does not create")
    void testLoginRejectsDeletedUserAndDoesNotCreate() throws Exception {
        User user = createUser("13800138012", "已删除用户", "ACTIVE");
        userService.removeById(user.getId());
        long countAfterDelete = userService.count();

        mockMvc.perform(post("/api/v1/auth/test-login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phone\":\"13800138012\"}"))
                .andExpect(status().isUnauthorized());

        assertThat(userService.count()).isEqualTo(countAfterDelete);
    }

    @Test
    @DisplayName("test-login for allowlisted phone with non-existent user returns 401")
    void testLoginAllowlistedPhoneNonExistentReturns401() throws Exception {
        long countBefore = userService.count();

        mockMvc.perform(post("/api/v1/auth/test-login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phone\":\"13800138050\"}"))
                .andExpect(status().isUnauthorized());

        assertThat(userService.count()).isEqualTo(countBefore);
    }

    // === Helper ===

    private User createUser(String phone, String nickname, String status) {
        User user = new User();
        user.setPhone(phone);
        user.setNickname(nickname);
        user.setStatus(status);
        user.setOpenid("test_openid_" + phone);
        user.setUnionid("test_unionid_" + phone);
        userService.save(user);
        return user;
    }

    private Long createTestAdmin(String username, String role) {
        AdminUser admin = new AdminUser();
        admin.setUsername(username);
        admin.setPassword(passwordEncoder.encode("password123456"));
        admin.setRole(role);
        admin.setStatus("ACTIVE");
        adminUserService.save(admin);
        return admin.getId();
    }
}
