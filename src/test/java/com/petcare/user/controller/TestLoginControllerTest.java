package com.petcare.user.controller;

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
    @DisplayName("ADMIN token cannot access user auth probe")
    void adminTokenCannotAccessUserAuthProbe() throws Exception {
        Long adminId = createTestAdmin("probe_admin", "SUPER_ADMIN");
        String adminToken = jwtTokenService.signAdminToken(adminId, "probe_admin", "SUPER_ADMIN");

        mockMvc.perform(get("/api/v1/test/user-auth-probe")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userId").value("none"));
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
