package com.petcare.community.controller;

import com.petcare.admin.entity.AdminPermission;
import com.petcare.admin.entity.AdminRole;
import com.petcare.admin.entity.AdminRolePermission;
import com.petcare.admin.entity.AdminUser;
import com.petcare.admin.service.AdminPermissionService;
import com.petcare.admin.service.AdminRolePermissionService;
import com.petcare.admin.service.AdminRoleService;
import com.petcare.admin.service.AdminUserService;
import com.petcare.common.security.JwtTokenService;
import com.petcare.community.entity.Post;
import com.petcare.community.entity.PostComment;
import com.petcare.community.entity.PostReport;
import com.petcare.community.mapper.PostCommentMapper;
import com.petcare.community.mapper.PostMapper;
import com.petcare.community.mapper.PostReportMapper;
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
 * Integration tests for {@link AdminCommunityController}.
 * Uses full Spring context with H2 database to test authentication,
 * authorization, and endpoint behavior end-to-end.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AdminCommunityControllerTest {

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

    @Autowired
    private PostMapper postMapper;

    @Autowired
    private PostCommentMapper postCommentMapper;

    @Autowired
    private PostReportMapper postReportMapper;

    private String adminToken;
    private String noCommunityPermToken;
    private Long testPostId;
    private Long testCommentId;
    private Long testReportId;

    @BeforeEach
    void setUp() {
        // Create admin with all community permissions
        Long adminId = createTestAdmin("community_admin", "password123456", "COMMUNITY_MGR");
        setupCommunityPermissions("COMMUNITY_MGR");
        adminToken = jwtTokenService.signAdminToken(adminId, "community_admin", "COMMUNITY_MGR");

        // Create admin with booking permissions but NO community permissions
        Long noPermAdminId = createTestAdmin("no_community_admin", "password123456", "BOOKING_MGR");
        setupNoCommunityPermissions("BOOKING_MGR");
        noCommunityPermToken = jwtTokenService.signAdminToken(
                noPermAdminId, "no_community_admin", "BOOKING_MGR");

        // Create test data: a post in PENDING_REVIEW status
        Post post = new Post();
        post.setUserId(999L);
        post.setTitle("测试帖子");
        post.setContent("测试内容");
        post.setStatus("PENDING_REVIEW");
        post.setRiskLevel(0);
        post.setViewCount(0);
        post.setLikeCount(0);
        post.setCommentCount(0);
        post.setFavoriteCount(0);
        post.setDeleted(0);
        postMapper.insert(post);
        testPostId = post.getId();

        // Create test data: a comment in PENDING_REVIEW status
        PostComment comment = new PostComment();
        comment.setPostId(testPostId);
        comment.setUserId(999L);
        comment.setContent("测试评论");
        comment.setStatus("PENDING_REVIEW");
        comment.setRiskLevel(0);
        comment.setLikeCount(0);
        comment.setDeleted(0);
        postCommentMapper.insert(comment);
        testCommentId = comment.getId();

        // Create test data: a report in PENDING status
        PostReport report = new PostReport();
        report.setPostId(testPostId);
        report.setReporterId(999L);
        report.setReasonType("SPAM");
        report.setReason("垃圾内容");
        report.setStatus("PENDING");
        postReportMapper.insert(report);
        testReportId = report.getId();
    }

    // ==================== Post Endpoints ====================

    @Nested
    @DisplayName("GET /api/v1/admin/community/posts")
    class ListPostsTests {

        @Test
        @DisplayName("with community:post:read permission returns 200")
        void listPostsWithPermission() throws Exception {
            mockMvc.perform(get("/api/v1/admin/community/posts")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.items").isArray());
        }

        @Test
        @DisplayName("without authentication returns 401")
        void listPostsUnauthenticated() throws Exception {
            mockMvc.perform(get("/api/v1/admin/community/posts"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("without community:post:read permission returns 403")
        void listPostsWithoutPermission() throws Exception {
            mockMvc.perform(get("/api/v1/admin/community/posts")
                            .header("Authorization", "Bearer " + noCommunityPermToken))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/admin/community/posts/{id}/approve")
    class ApprovePostTests {

        @Test
        @DisplayName("with community:post:approve permission returns 200")
        void approvePostWithPermission() throws Exception {
            mockMvc.perform(post("/api/v1/admin/community/posts/" + testPostId + "/approve")
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"remark\":\"审核通过\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.status").value("PUBLISHED"));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/admin/community/posts/{id}/reject")
    class RejectPostTests {

        @Test
        @DisplayName("with community:post:reject permission returns 200")
        void rejectPostWithPermission() throws Exception {
            mockMvc.perform(post("/api/v1/admin/community/posts/" + testPostId + "/reject")
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"remark\":\"内容违规\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.status").value("REJECTED"));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/admin/community/posts/{id}/hide")
    class HidePostTests {

        @Test
        @DisplayName("with community:post:hide permission returns 200")
        void hidePostWithPermission() throws Exception {
            // Post must be PUBLISHED to be hidden
            Post post = postMapper.selectById(testPostId);
            post.setStatus("PUBLISHED");
            postMapper.updateById(post);

            mockMvc.perform(post("/api/v1/admin/community/posts/" + testPostId + "/hide")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.status").value("HIDDEN"));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/admin/community/posts/{id}/delete")
    class DeletePostTests {

        @Test
        @DisplayName("with community:post:delete permission returns 200")
        void deletePostWithPermission() throws Exception {
            mockMvc.perform(post("/api/v1/admin/community/posts/" + testPostId + "/delete")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }

    // ==================== Comment Authorization ====================

    @Nested
    @DisplayName("Comment endpoint authorization")
    class CommentAuthTests {

        @Test
        @DisplayName("POST /comments/{id}/hide without community:comment:hide returns 403")
        void hideCommentWithoutPermission() throws Exception {
            mockMvc.perform(post("/api/v1/admin/community/comments/" + testCommentId + "/hide")
                            .header("Authorization", "Bearer " + noCommunityPermToken))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("POST /comments/{id}/delete without community:comment:delete returns 403")
        void deleteCommentWithoutPermission() throws Exception {
            mockMvc.perform(post("/api/v1/admin/community/comments/" + testCommentId + "/delete")
                            .header("Authorization", "Bearer " + noCommunityPermToken))
                    .andExpect(status().isForbidden());
        }
    }

    // ==================== Report Authorization ====================

    @Nested
    @DisplayName("Report endpoint authorization")
    class ReportAuthTests {

        @Test
        @DisplayName("POST /reports/{id}/handle without community:report:handle returns 403")
        void handleReportWithoutPermission() throws Exception {
            mockMvc.perform(post("/api/v1/admin/community/reports/" + testReportId + "/handle")
                            .header("Authorization", "Bearer " + noCommunityPermToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"handleResult\":\"PROCESSED\",\"hidePost\":false,\"handleRemark\":\"已处理\"}"))
                    .andExpect(status().isForbidden());
        }
    }

    // ==================== Helper Methods ====================

    private Long createTestAdmin(String username, String rawPassword, String role) {
        AdminUser admin = new AdminUser();
        admin.setUsername(username);
        admin.setPassword(passwordEncoder.encode(rawPassword));
        admin.setRole(role);
        admin.setStatus("ACTIVE");
        adminUserService.save(admin);
        return admin.getId();
    }

    private void setupCommunityPermissions(String roleCode) {
        AdminRole role = new AdminRole();
        role.setRoleCode(roleCode);
        role.setRoleName(roleCode);
        role.setStatus("ACTIVE");
        adminRoleService.save(role);

        String[] permCodes = {
                "community:post:read",
                "community:post:approve",
                "community:post:reject",
                "community:post:hide",
                "community:post:delete",
                "community:comment:hide",
                "community:comment:delete",
                "community:report:handle"
        };
        for (String code : permCodes) {
            AdminPermission perm = new AdminPermission();
            perm.setPermissionCode(code);
            perm.setPermissionName(code);
            perm.setModule("community");
            perm.setStatus("ACTIVE");
            adminPermissionService.save(perm);

            AdminRolePermission rp = new AdminRolePermission();
            rp.setRoleId(role.getId());
            rp.setPermissionId(perm.getId());
            adminRolePermissionService.save(rp);
        }
    }

    private void setupNoCommunityPermissions(String roleCode) {
        AdminRole role = new AdminRole();
        role.setRoleCode(roleCode);
        role.setRoleName(roleCode);
        role.setStatus("ACTIVE");
        adminRoleService.save(role);

        // Grant booking permissions but NOT community permissions
        AdminPermission perm = new AdminPermission();
        perm.setPermissionCode("booking:booking:read");
        perm.setPermissionName("booking:booking:read");
        perm.setModule("booking");
        perm.setStatus("ACTIVE");
        adminPermissionService.save(perm);

        AdminRolePermission rp = new AdminRolePermission();
        rp.setRoleId(role.getId());
        rp.setPermissionId(perm.getId());
        adminRolePermissionService.save(rp);
    }
}
