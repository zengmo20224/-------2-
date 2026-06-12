package com.petcare.user.controller;

import com.petcare.admin.entity.AdminUser;
import com.petcare.admin.service.AdminUserService;
import com.petcare.common.config.SecurityProperties;
import com.petcare.common.security.JwtTokenService;
import com.petcare.user.entity.User;
import com.petcare.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * RED-1 / RED-2: Integration tests for current user profile API.
 * Contract defined in docs/35-phase-11-02-glm5-user-profile-api-brief.md.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class UserProfileControllerTest {

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

    // ======================== RED-1: Read Contract ========================

    @Nested
    @DisplayName("GET /api/v1/user/profile — read contract")
    class ReadProfile {

        @Test
        @DisplayName("USER token gets own profile successfully")
        void userTokenGetsOwnProfile() throws Exception {
            User user = createUser("13800138001", "小宠主人", "ACTIVE");

            String token = jwtTokenService.signUserToken(user.getId());

            mockMvc.perform(get("/api/v1/user/profile")
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.userId").value(String.valueOf(user.getId())))
                    .andExpect(jsonPath("$.data.nickname").value("小宠主人"))
                    .andExpect(jsonPath("$.data.avatarUrl").isEmpty())
                    .andExpect(jsonPath("$.data.createdAt").isNotEmpty());
        }

        @Test
        @DisplayName("userId is serialized as String, not number")
        void userIdSerializedAsString() throws Exception {
            User user = createUser("13800138001", "ID测试", "ACTIVE");

            String token = jwtTokenService.signUserToken(user.getId());

            // Jackson serializes Long > 2^53 as number by default;
            // we require String to avoid JavaScript precision loss.
            mockMvc.perform(get("/api/v1/user/profile")
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.userId").isString())
                    .andExpect(jsonPath("$.data.userId").value(String.valueOf(user.getId())));
        }

        @Test
        @DisplayName("phone is masked — never returns full phone number")
        void phoneIsMasked() throws Exception {
            User user = createUser("13800138001", "脱敏测试", "ACTIVE");

            String token = jwtTokenService.signUserToken(user.getId());

            mockMvc.perform(get("/api/v1/user/profile")
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.phone").value("138****8001"));
        }

        @Test
        @DisplayName("response does not expose openid, unionid, status, or deleted")
        void responseDoesNotExposeSensitiveFields() throws Exception {
            User user = createUser("13800138001", "隐私测试", "ACTIVE");

            String token = jwtTokenService.signUserToken(user.getId());

            mockMvc.perform(get("/api/v1/user/profile")
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.openid").doesNotExist())
                    .andExpect(jsonPath("$.data.unionid").doesNotExist())
                    .andExpect(jsonPath("$.data.status").doesNotExist())
                    .andExpect(jsonPath("$.data.deleted").doesNotExist());
        }

        @Test
        @DisplayName("ADMIN token gets 403 Forbidden on user profile")
        void adminTokenGets403() throws Exception {
            Long adminId = createTestAdmin("profile_admin", "SUPER_ADMIN");
            String adminToken = jwtTokenService.signAdminToken(adminId, "profile_admin", "SUPER_ADMIN");

            mockMvc.perform(get("/api/v1/user/profile")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("no token returns 401 Unauthorized")
        void noTokenReturns401() throws Exception {
            mockMvc.perform(get("/api/v1/user/profile"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("disabled user token returns 401")
        void disabledUserTokenReturns401() throws Exception {
            User user = createUser("13800138001", "禁用用户", "ACTIVE");
            String token = jwtTokenService.signUserToken(user.getId());

            user.setStatus("DISABLED");
            userService.updateById(user);

            mockMvc.perform(get("/api/v1/user/profile")
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("deleted user token returns 401")
        void deletedUserTokenReturns401() throws Exception {
            User user = createUser("13800138001", "删除用户", "ACTIVE");
            String token = jwtTokenService.signUserToken(user.getId());

            userService.removeById(user.getId());

            mockMvc.perform(get("/api/v1/user/profile")
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ======================== RED-2: Update Contract ========================

    @Nested
    @DisplayName("PUT /api/v1/user/profile — update contract")
    class UpdateProfile {

        @Test
        @DisplayName("USER can update own nickname and avatarUrl")
        void userCanUpdateNicknameAndAvatar() throws Exception {
            User user = createUser("13800138001", "旧昵称", "ACTIVE");
            String token = jwtTokenService.signUserToken(user.getId());

            mockMvc.perform(put("/api/v1/user/profile")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"nickname\":\"新昵称\",\"avatarUrl\":\"https://example.com/avatar.png\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.nickname").value("新昵称"))
                    .andExpect(jsonPath("$.data.avatarUrl").value("https://example.com/avatar.png"))
                    .andExpect(jsonPath("$.data.userId").value(String.valueOf(user.getId())));
        }

        @Test
        @DisplayName("after update, GET returns latest values")
        void getReturnsLatestAfterUpdate() throws Exception {
            User user = createUser("13800138001", "旧昵称", "ACTIVE");
            String token = jwtTokenService.signUserToken(user.getId());

            mockMvc.perform(put("/api/v1/user/profile")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"nickname\":\"更新后\",\"avatarUrl\":\"https://cdn.example.com/new.png\"}"))
                    .andExpect(status().isOk());

            mockMvc.perform(get("/api/v1/user/profile")
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.nickname").value("更新后"))
                    .andExpect(jsonPath("$.data.avatarUrl").value("https://cdn.example.com/new.png"));
        }

        @Test
        @DisplayName("ADMIN token update returns 403")
        void adminTokenUpdateReturns403() throws Exception {
            Long adminId = createTestAdmin("update_admin", "SUPER_ADMIN");
            String adminToken = jwtTokenService.signAdminToken(adminId, "update_admin", "SUPER_ADMIN");

            mockMvc.perform(put("/api/v1/user/profile")
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"nickname\":\"admin尝试\"}"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("request cannot modify phone, openid, unionid, status — fields are ignored")
        void requestCannotModifyProtectedFields() throws Exception {
            User user = createUser("13800138001", "原昵称", "ACTIVE");
            String token = jwtTokenService.signUserToken(user.getId());

            // Attempt to inject protected fields via request body
            mockMvc.perform(put("/api/v1/user/profile")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"nickname\":\"合法更新\",\"phone\":\"99999999999\",\"openid\":\"injected\",\"unionid\":\"injected\",\"status\":\"DISABLED\"}"))
                    .andExpect(status().isOk());

            // Verify database: only nickname changed, phone/openid/status untouched
            User updated = userService.getById(user.getId());
            assertThat(updated.getNickname()).isEqualTo("合法更新");
            assertThat(updated.getPhone()).isEqualTo("13800138001");
            assertThat(updated.getOpenid()).isEqualTo("test_openid_13800138001");
            assertThat(updated.getStatus()).isEqualTo("ACTIVE");
        }

        @Test
        @DisplayName("blank nickname returns 400 validation_error")
        void blankNicknameReturns400() throws Exception {
            User user = createUser("13800138001", "测试", "ACTIVE");
            String token = jwtTokenService.signUserToken(user.getId());

            mockMvc.perform(put("/api/v1/user/profile")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"nickname\":\"   \"}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error.code").value("validation_error"));
        }

        @Test
        @DisplayName("null nickname returns 400 validation_error")
        void nullNicknameReturns400() throws Exception {
            User user = createUser("13800138001", "测试", "ACTIVE");
            String token = jwtTokenService.signUserToken(user.getId());

            mockMvc.perform(put("/api/v1/user/profile")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"avatarUrl\":\"https://example.com/a.png\"}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error.code").value("validation_error"));
        }

        @Test
        @DisplayName("nickname exceeding 64 characters returns 400")
        void nicknameExceeding64Returns400() throws Exception {
            User user = createUser("13800138001", "测试", "ACTIVE");
            String token = jwtTokenService.signUserToken(user.getId());

            String longNickname = "a".repeat(65);

            mockMvc.perform(put("/api/v1/user/profile")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"nickname\":\"" + longNickname + "\"}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error.code").value("validation_error"));
        }

        @Test
        @DisplayName("avatarUrl exceeding 255 characters returns 400")
        void avatarUrlExceeding255Returns400() throws Exception {
            User user = createUser("13800138001", "测试", "ACTIVE");
            String token = jwtTokenService.signUserToken(user.getId());

            String longUrl = "https://example.com/" + "a".repeat(240);

            mockMvc.perform(put("/api/v1/user/profile")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"nickname\":\"合法\",\"avatarUrl\":\"" + longUrl + "\"}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error.code").value("validation_error"));
        }

        @Test
        @DisplayName("avatarUrl with javascript: protocol returns 400")
        void avatarUrlWithJavascriptProtocolReturns400() throws Exception {
            User user = createUser("13800138001", "测试", "ACTIVE");
            String token = jwtTokenService.signUserToken(user.getId());

            mockMvc.perform(put("/api/v1/user/profile")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"nickname\":\"合法\",\"avatarUrl\":\"javascript:alert(1)\"}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error.code").value("validation_error"));
        }

        @Test
        @DisplayName("avatarUrl with data: protocol returns 400")
        void avatarUrlWithDataProtocolReturns400() throws Exception {
            User user = createUser("13800138001", "测试", "ACTIVE");
            String token = jwtTokenService.signUserToken(user.getId());

            mockMvc.perform(put("/api/v1/user/profile")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"nickname\":\"合法\",\"avatarUrl\":\"data:image/png;base64,abc\"}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error.code").value("validation_error"));
        }

        @Test
        @DisplayName("avatarUrl allows null to clear avatar")
        void avatarUrlAllowsNullToClear() throws Exception {
            User user = createUser("13800138001", "测试", "ACTIVE");
            user.setAvatarUrl("https://example.com/old.png");
            userService.updateById(user);
            String token = jwtTokenService.signUserToken(user.getId());

            mockMvc.perform(put("/api/v1/user/profile")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"nickname\":\"合法\",\"avatarUrl\":null}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.avatarUrl").isEmpty());
        }

        @Test
        @DisplayName("avatarUrl allows empty string to clear avatar")
        void avatarUrlAllowsEmptyStringToClear() throws Exception {
            User user = createUser("13800138001", "测试", "ACTIVE");
            user.setAvatarUrl("https://example.com/old.png");
            userService.updateById(user);
            String token = jwtTokenService.signUserToken(user.getId());

            mockMvc.perform(put("/api/v1/user/profile")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"nickname\":\"合法\",\"avatarUrl\":\"\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.avatarUrl").isEmpty());
        }
    }

    // ======================== Helpers ========================

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
