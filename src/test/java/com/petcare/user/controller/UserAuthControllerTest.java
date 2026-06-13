package com.petcare.user.controller;

import com.petcare.user.entity.User;
import com.petcare.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for user registration, login, and password recovery.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class UserAuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    // === Register ===

    @Test
    @DisplayName("register with valid data returns token and user info")
    void registerWithValidDataReturnsToken() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "phone": "13900001111",
                                    "password": "test123456",
                                    "nickname": "新用户",
                                    "securityQuestions": [
                                        {"question": "你的宠物叫什么名字？", "answer": "豆豆"},
                                        {"question": "你最喜欢的食物？", "answer": "火锅"}
                                    ]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.user.nickname").value("新用户"));
    }

    @Test
    @DisplayName("register with duplicate phone returns 422")
    void registerWithDuplicatePhoneReturns422() throws Exception {
        createUser("13900002222");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "phone": "13900002222",
                                    "password": "test123456",
                                    "nickname": "重复用户",
                                    "securityQuestions": [
                                        {"question": "问题1", "answer": "答案1"}
                                    ]
                                }
                                """))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error.code").value("phone_already_registered"));
    }

    @Test
    @DisplayName("register with invalid phone format returns 400")
    void registerWithInvalidPhoneReturns400() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "phone": "123",
                                    "password": "test123456",
                                    "nickname": "测试",
                                    "securityQuestions": []
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("validation_error"));
    }

    @Test
    @DisplayName("register with short password returns 400")
    void registerWithShortPasswordReturns400() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "phone": "13900003333",
                                    "password": "123",
                                    "nickname": "短密码",
                                    "securityQuestions": []
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("validation_error"));
    }

    // === Login ===

    @Test
    @DisplayName("login with correct password returns token")
    void loginWithCorrectPasswordReturnsToken() throws Exception {
        // Register first
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "phone": "13900004444",
                                    "password": "test123456",
                                    "nickname": "登录测试",
                                    "securityQuestions": [
                                        {"question": "问题1", "answer": "答案1"}
                                    ]
                                }
                                """))
                .andExpect(status().isOk());

        // Login
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "phone": "13900004444",
                                    "password": "test123456"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.user.nickname").value("登录测试"));
    }

    @Test
    @DisplayName("login with wrong password returns 401")
    void loginWithWrongPasswordReturns401() throws Exception {
        createUser("13900005555");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "phone": "13900005555",
                                    "password": "wrongpassword"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("invalid_credentials"));
    }

    @Test
    @DisplayName("login with non-existent phone returns 401")
    void loginWithNonExistentPhoneReturns401() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "phone": "19999999999",
                                    "password": "whatever"
                                }
                                """))
                .andExpect(status().isUnauthorized());
    }

    // === Forgot Password ===

    @Test
    @DisplayName("forgot-password questions returns security questions without answers")
    void forgotPasswordReturnsSecurityQuestions() throws Exception {
        // Register with questions
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "phone": "13900006666",
                                    "password": "test123456",
                                    "nickname": "找回密码",
                                    "securityQuestions": [
                                        {"question": "你的宠物叫什么？", "answer": "豆豆"},
                                        {"question": "你的家乡？", "answer": "上海"}
                                    ]
                                }
                                """))
                .andExpect(status().isOk());

        // Get questions
        mockMvc.perform(post("/api/v1/auth/forgot-password/questions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"phone": "13900006666"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].question").value("你的宠物叫什么？"))
                .andExpect(jsonPath("$.data[1].question").value("你的家乡？"));
    }

    @Test
    @DisplayName("forgot-password reset with correct answers changes password")
    void forgotPasswordResetWithCorrectAnswersChangesPassword() throws Exception {
        // Register
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "phone": "13900007777",
                                    "password": "oldpassword",
                                    "nickname": "重置密码",
                                    "securityQuestions": [
                                        {"question": "你的宠物叫什么？", "answer": "豆豆"}
                                    ]
                                }
                                """))
                .andExpect(status().isOk());

        // Get question IDs
        String questionsJson = mockMvc.perform(post("/api/v1/auth/forgot-password/questions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"phone": "13900007777"}
                                """))
                .andReturn().getResponse().getContentAsString();

        // Extract question ID (simplified — the id field is a string in JSON)
        // Use the actual ID from the response
        String questionId = extractIdFromJson(questionsJson);

        // Reset password
        mockMvc.perform(post("/api/v1/auth/forgot-password/reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("""
                                {
                                    "phone": "13900007777",
                                    "answers": [{"questionId": "%s", "answer": "豆豆"}],
                                    "newPassword": "newpassword"
                                }
                                """, questionId)))
                .andExpect(status().isOk());

        // Verify new password works
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "phone": "13900007777",
                                    "password": "newpassword"
                                }
                                """))
                .andExpect(status().isOk());

        // Verify old password no longer works
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "phone": "13900007777",
                                    "password": "oldpassword"
                                }
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("forgot-password reset with wrong answer returns 422")
    void forgotPasswordResetWithWrongAnswerReturns422() throws Exception {
        // Register
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "phone": "13900008888",
                                    "password": "oldpassword",
                                    "nickname": "错误答案",
                                    "securityQuestions": [
                                        {"question": "你的宠物叫什么？", "answer": "豆豆"}
                                    ]
                                }
                                """))
                .andExpect(status().isOk());

        // Get question IDs
        String questionsJson = mockMvc.perform(post("/api/v1/auth/forgot-password/questions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"phone": "13900008888"}
                                """))
                .andReturn().getResponse().getContentAsString();

        String questionId = extractIdFromJson(questionsJson);

        // Try reset with wrong answer
        mockMvc.perform(post("/api/v1/auth/forgot-password/reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("""
                                {
                                    "phone": "13900008888",
                                    "answers": [{"questionId": "%s", "answer": "错误的答案"}],
                                    "newPassword": "newpassword"
                                }
                                """, questionId)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error.code").value("security_answer_incorrect"));
    }

    @Test
    @DisplayName("forgot-password questions for non-existent phone returns 401")
    void forgotPasswordForNonExistentPhoneReturns401() throws Exception {
        mockMvc.perform(post("/api/v1/auth/forgot-password/questions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"phone": "19999999999"}
                                """))
                .andExpect(status().isUnauthorized());
    }

    // === Helper ===

    private User createUser(String phone) {
        User user = new User();
        user.setPhone(phone);
        user.setNickname("测试用户");
        user.setStatus("ACTIVE");
        userService.save(user);
        return user;
    }

    /** Extract the first id value from the forgot-password questions JSON response */
    private String extractIdFromJson(String json) {
        // The response format is: {"success":true,"data":[{"id":"123","question":"..."},...]}
        int idStart = json.indexOf("\"id\":\"") + 6;
        int idEnd = json.indexOf("\"", idStart);
        return json.substring(idStart, idEnd);
    }
}
