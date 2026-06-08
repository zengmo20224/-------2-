package com.petcare.common.exception;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.petcare.common.config.SecurityConfig;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests for the global exception handler.
 * Verifies that validation errors, business exceptions, and unexpected errors
 * are all converted into the unified ApiResponse format.
 */
@WebMvcTest
@Import({SecurityConfig.class, GlobalExceptionHandler.class, GlobalExceptionHandlerTest.TestController.class})
@ActiveProfiles("test")
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    /**
     * Test-only controller that triggers various exception types.
     */
    @RestController
    @RequestMapping("/api/v1/test")
    static class TestController {

        @PostMapping("/validate")
        public void validate(@Valid @RequestBody TestRequest request) {
            // Bean validation will fail before reaching here
        }

        @PostMapping("/business")
        public void business() {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_VIOLATION, "测试业务异常");
        }

        @PostMapping("/unexpected")
        public void unexpected() {
            throw new RuntimeException("Unexpected error");
        }
    }

    record TestRequest(
            @NotBlank(message = "名称不能为空") String name
    ) {}

    @Test
    @WithMockUser
    void handleValidation_shouldReturn400WithFieldErrors() throws Exception {
        String body = """
                {"name": ""}
                """;

        mockMvc.perform(post("/api/v1/test/validate")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("validation_error"))
                .andExpect(jsonPath("$.error.message").value("请求参数不合法"))
                .andExpect(jsonPath("$.error.details").isArray());
    }

    @Test
    @WithMockUser
    void handleBusiness_shouldReturn422WithErrorCode() throws Exception {
        mockMvc.perform(post("/api/v1/test/business")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("business_rule_violation"))
                .andExpect(jsonPath("$.error.message").value("测试业务异常"));
    }

    @Test
    @WithMockUser
    void handleUnexpected_shouldReturn500WithGenericMessage() throws Exception {
        mockMvc.perform(post("/api/v1/test/unexpected")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("internal_error"))
                .andExpect(jsonPath("$.error.message").value("服务内部错误，请稍后重试"));
    }
}
