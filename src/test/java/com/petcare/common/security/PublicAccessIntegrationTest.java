package com.petcare.common.security;

import com.petcare.admin.entity.AdminUser;
import com.petcare.admin.service.AdminUserService;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Security contract for anonymous (unauthenticated) public read access.
 *
 * <p>Defines the Phase 11-05 contract:
 * <ul>
 *   <li>Anonymous GET on the public catalog/community whitelist succeeds (200) or
 *       returns a safe 404 when the resource does not exist.</li>
 *   <li>Anonymous GET on user-private, admin, order, cart, booking and AI endpoints
 *       is rejected with 401.</li>
 *   <li>Anonymous write (POST/PUT/DELETE) on public paths is rejected with 401.</li>
 *   <li>Anonymous GET on non-whitelisted paths is rejected with 401.</li>
 *   <li>Malformed/expired Bearer tokens never downgrade to anonymous on public reads.</li>
 *   <li>Valid USER/ADMIN tokens still read public content normally.</li>
 * </ul>
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class PublicAccessIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenService jwtTokenService;

    @Autowired
    private AdminUserService adminUserService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserService userService;

    // ==================== Anonymous whitelist GET succeeds ====================

    @Nested
    @DisplayName("Anonymous GET on public catalog whitelist")
    class AnonymousCatalogReads {

        @Test
        @DisplayName("GET /api/v1/service-categories returns 200 anonymously")
        void serviceCategoriesAnonymous() throws Exception {
            mockMvc.perform(get("/api/v1/service-categories"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("GET /api/v1/service-items returns 200 anonymously")
        void serviceItemsListAnonymous() throws Exception {
            mockMvc.perform(get("/api/v1/service-items"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("GET /api/v1/service-items/{id} returns safe 404 anonymously when absent")
        void serviceItemDetailAnonymousNotFound() throws Exception {
            mockMvc.perform(get("/api/v1/service-items/999999999"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("GET /api/v1/product-categories returns 200 anonymously")
        void productCategoriesAnonymous() throws Exception {
            mockMvc.perform(get("/api/v1/product-categories"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("GET /api/v1/products returns 200 anonymously")
        void productsListAnonymous() throws Exception {
            mockMvc.perform(get("/api/v1/products"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("GET /api/v1/products/{id} returns safe 404 anonymously when absent")
        void productDetailAnonymousNotFound() throws Exception {
            mockMvc.perform(get("/api/v1/products/999999999"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("Anonymous GET on public community whitelist")
    class AnonymousCommunityReads {

        @Test
        @DisplayName("GET /api/v1/topics returns 200 anonymously")
        void topicsListAnonymous() throws Exception {
            mockMvc.perform(get("/api/v1/topics"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("GET /api/v1/topics/{id} returns safe 404 anonymously when absent")
        void topicDetailAnonymousNotFound() throws Exception {
            mockMvc.perform(get("/api/v1/topics/999999999"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("GET /api/v1/posts returns 200 anonymously")
        void postsListAnonymous() throws Exception {
            mockMvc.perform(get("/api/v1/posts"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("GET /api/v1/posts/{id} returns safe 404 anonymously when absent")
        void postDetailAnonymousNotFound() throws Exception {
            mockMvc.perform(get("/api/v1/posts/999999999"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("GET /api/v1/posts/{postId}/comments returns safe 404 anonymously when post absent")
        void postCommentsAnonymousNotFound() throws Exception {
            mockMvc.perform(get("/api/v1/posts/999999999/comments"))
                    .andExpect(status().isNotFound());
        }
    }

    // ==================== Anonymous private/admin GET rejected ====================

    @Nested
    @DisplayName("Anonymous GET on private and admin endpoints rejected with 401")
    class AnonymousPrivateReadsRejected {

        @Test
        @DisplayName("GET /api/v1/user/pets rejected with 401")
        void userPetsRejected() throws Exception {
            mockMvc.perform(get("/api/v1/user/pets"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("GET /api/v1/user/addresses rejected with 401")
        void userAddressesRejected() throws Exception {
            mockMvc.perform(get("/api/v1/user/addresses"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("GET /api/v1/bookings rejected with 401")
        void bookingsRejected() throws Exception {
            mockMvc.perform(get("/api/v1/bookings"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("GET /api/v1/cart-items rejected with 401")
        void cartItemsRejected() throws Exception {
            mockMvc.perform(get("/api/v1/cart-items"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("GET /api/v1/product-orders rejected with 401")
        void productOrdersRejected() throws Exception {
            mockMvc.perform(get("/api/v1/product-orders"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("GET /api/v1/admin/auth/me rejected with 401")
        void adminMeRejected() throws Exception {
            mockMvc.perform(get("/api/v1/admin/auth/me"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("GET /api/v1/admin/community/posts rejected with 401")
        void adminCommunityPostsRejected() throws Exception {
            mockMvc.perform(get("/api/v1/admin/community/posts"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("GET /api/v1/ai/conversations rejected with 401 (AI stays protected)")
        void aiConversationsRejected() throws Exception {
            mockMvc.perform(get("/api/v1/ai/conversations"))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ==================== Anonymous writes on public paths rejected ====================

    @Nested
    @DisplayName("Anonymous write on public paths rejected with 401")
    class AnonymousWritesRejected {

        @Test
        @DisplayName("POST /api/v1/posts rejected with 401")
        void createPostRejected() throws Exception {
            mockMvc.perform(post("/api/v1/posts")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"title\":\"x\",\"content\":\"y\"}"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("POST /api/v1/posts/{postId}/comments rejected with 401")
        void createCommentRejected() throws Exception {
            mockMvc.perform(post("/api/v1/posts/1/comments")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"content\":\"y\"}"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("POST /api/v1/posts/{postId}/like rejected with 401")
        void likePostRejected() throws Exception {
            mockMvc.perform(post("/api/v1/posts/1/like"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("DELETE /api/v1/posts/{postId}/like rejected with 401")
        void unlikePostRejected() throws Exception {
            mockMvc.perform(delete("/api/v1/posts/1/like"))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ==================== Non-whitelist GET stays protected ====================

    @Nested
    @DisplayName("Non-whitelist GET stays authenticated")
    class NonWhitelistGetStaysProtected {

        @Test
        @DisplayName("GET /api/v1/bookings/1 rejected with 401")
        void bookingDetailRejected() throws Exception {
            mockMvc.perform(get("/api/v1/bookings/1"))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ==================== Token behaviour on public reads ====================

    @Nested
    @DisplayName("Bearer token behaviour on public reads")
    class TokenBehaviourOnPublicReads {

        @Test
        @DisplayName("Valid ADMIN token reads public GET normally")
        void validAdminTokenReadsPublic() throws Exception {
            String token = signTokenForNewAdmin("public_read_admin", "SUPER_ADMIN");
            mockMvc.perform(get("/api/v1/posts").header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Valid USER token reads public GET normally")
        void validUserTokenReadsPublic() throws Exception {
            String token = jwtTokenService.signUserToken(createActiveUser());
            mockMvc.perform(get("/api/v1/posts").header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Malformed Bearer token on public GET is rejected with 401 (no downgrade)")
        void malformedTokenRejected() throws Exception {
            mockMvc.perform(get("/api/v1/posts").header("Authorization", "Bearer not.a.real.token"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Expired Bearer token on public GET is rejected with 401 (no downgrade)")
        void expiredTokenRejected() throws Exception {
            signTokenForNewAdmin("public_read_expired", "SUPER_ADMIN");
            String expired = jwtTokenService.signAdminToken(1L, "public_read_expired", "SUPER_ADMIN", -60);
            mockMvc.perform(get("/api/v1/posts").header("Authorization", "Bearer " + expired))
                    .andExpect(status().isUnauthorized());
        }

        private String signTokenForNewAdmin(String username, String role) {
            AdminUser admin = new AdminUser();
            admin.setUsername(username);
            admin.setPassword(passwordEncoder.encode("password123456"));
            admin.setRole(role);
            admin.setStatus("ACTIVE");
            adminUserService.save(admin);
            return jwtTokenService.signAdminToken(admin.getId(), username, role);
        }

        private Long createActiveUser() {
            User user = new User();
            user.setOpenid("public_read_user_openid");
            user.setNickname("公开读取测试用户");
            user.setStatus("ACTIVE");
            userService.save(user);
            return user.getId();
        }
    }
}
