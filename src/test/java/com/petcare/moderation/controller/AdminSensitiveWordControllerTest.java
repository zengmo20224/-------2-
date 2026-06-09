package com.petcare.moderation.controller;

import com.petcare.admin.entity.AdminPermission;
import com.petcare.admin.entity.AdminRole;
import com.petcare.admin.entity.AdminRolePermission;
import com.petcare.admin.entity.AdminUser;
import com.petcare.admin.service.AdminPermissionService;
import com.petcare.admin.service.AdminRolePermissionService;
import com.petcare.admin.service.AdminRoleService;
import com.petcare.admin.service.AdminUserService;
import com.petcare.common.security.JwtTokenService;
import com.petcare.moderation.dto.SensitiveWordCreateRequest;
import com.petcare.moderation.entity.SensitiveWord;
import com.petcare.moderation.service.SensitiveWordService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for AdminSensitiveWordController.
 * Covers CRUD operations, permission checks, and validation.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AdminSensitiveWordControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SensitiveWordService sensitiveWordService;

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

    private String adminToken;
    private String noPermToken;

    @BeforeEach
    void setUp() {
        // Admin WITH community:sensitive-word:manage permission
        Long adminId = createTestAdmin("swadmin", "password123456", "MANAGER");
        setupPermission("MANAGER", "community:sensitive-word:manage", "community");
        adminToken = jwtTokenService.signAdminToken(adminId, "swadmin", "MANAGER");

        // Admin WITHOUT the permission
        Long staffId = createTestAdmin("swstaff", "password123456", "STAFF");
        setupRoleOnly("STAFF");
        noPermToken = jwtTokenService.signAdminToken(staffId, "swstaff", "STAFF");
    }

    @Nested
    @DisplayName("GET /api/v1/admin/moderation/sensitive-words")
    class ListSensitiveWords {

        @Test
        @DisplayName("Returns 200 with sensitive words list for authorized admin")
        void listSensitiveWords_authorized() throws Exception {
            // Arrange - create a test word
            SensitiveWord word = new SensitiveWord();
            word.setWord("测试敏感词");
            word.setLevel(1);
            word.setStatus("ACTIVE");
            sensitiveWordService.save(word);

            // Act & Assert
            mockMvc.perform(get("/api/v1/admin/moderation/sensitive-words")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.items").isArray());
        }

        @Test
        @DisplayName("Returns 401 when not authenticated")
        void listSensitiveWords_unauthenticated() throws Exception {
            mockMvc.perform(get("/api/v1/admin/moderation/sensitive-words"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Returns 403 without community:sensitive-word:manage permission")
        void listSensitiveWords_forbidden() throws Exception {
            mockMvc.perform(get("/api/v1/admin/moderation/sensitive-words")
                            .header("Authorization", "Bearer " + noPermToken))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/admin/moderation/sensitive-words")
    class CreateSensitiveWord {

        @Test
        @DisplayName("Returns 201 when creating new sensitive word")
        void createSensitiveWord_success() throws Exception {
            SensitiveWordCreateRequest request = new SensitiveWordCreateRequest("新敏感词", "分类", 2);

            mockMvc.perform(post("/api/v1/admin/moderation/sensitive-words")
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.word").value("新敏感词"))
                    .andExpect(jsonPath("$.data.level").value(2));
        }

        @Test
        @DisplayName("Returns 409 when creating duplicate active sensitive word")
        void createSensitiveWord_duplicate() throws Exception {
            // Arrange - create existing word
            SensitiveWord existing = new SensitiveWord();
            existing.setWord("重复词");
            existing.setLevel(1);
            existing.setStatus("ACTIVE");
            sensitiveWordService.save(existing);

            SensitiveWordCreateRequest request = new SensitiveWordCreateRequest("重复词", null, 1);

            mockMvc.perform(post("/api/v1/admin/moderation/sensitive-words")
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error.code").value("community_sensitive_word_duplicate"));
        }

        @Test
        @DisplayName("Returns 400 when word is blank")
        void createSensitiveWord_blankWord() throws Exception {
            SensitiveWordCreateRequest request = new SensitiveWordCreateRequest("", null, 1);

            mockMvc.perform(post("/api/v1/admin/moderation/sensitive-words")
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error.code").value("validation_error"));
        }

        @Test
        @DisplayName("Returns 400 when level is out of range")
        void createSensitiveWord_invalidLevel() throws Exception {
            SensitiveWordCreateRequest request = new SensitiveWordCreateRequest("测试词", null, 5);

            mockMvc.perform(post("/api/v1/admin/moderation/sensitive-words")
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/admin/moderation/sensitive-words/{id}/disable")
    class DisableSensitiveWord {

        @Test
        @DisplayName("Returns 200 when disabling active word")
        void disableSensitiveWord_success() throws Exception {
            SensitiveWord word = new SensitiveWord();
            word.setWord("待禁用词");
            word.setLevel(1);
            word.setStatus("ACTIVE");
            sensitiveWordService.save(word);

            mockMvc.perform(post("/api/v1/admin/moderation/sensitive-words/{id}/disable", word.getId())
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));

            // Verify status changed
            SensitiveWord updated = sensitiveWordService.getById(word.getId());
            assert "DISABLED".equals(updated.getStatus());
        }

        @Test
        @DisplayName("Returns 409 when disabling already disabled word")
        void disableSensitiveWord_alreadyDisabled() throws Exception {
            SensitiveWord word = new SensitiveWord();
            word.setWord("已禁用词");
            word.setLevel(1);
            word.setStatus("DISABLED");
            sensitiveWordService.save(word);

            mockMvc.perform(post("/api/v1/admin/moderation/sensitive-words/{id}/disable", word.getId())
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.error.code").value("community_sensitive_word_duplicate"));
        }

        @Test
        @DisplayName("Returns 404 when word not found")
        void disableSensitiveWord_notFound() throws Exception {
            mockMvc.perform(post("/api/v1/admin/moderation/sensitive-words/{id}/disable", 999999L)
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isNotFound());
        }
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

    private void setupPermission(String roleCode, String permCode, String module) {
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

    private void setupRoleOnly(String roleCode) {
        AdminRole role = new AdminRole();
        role.setRoleCode(roleCode);
        role.setRoleName(roleCode);
        role.setStatus("ACTIVE");
        adminRoleService.save(role);
    }
}
