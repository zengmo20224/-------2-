package com.petcare.user.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Step 2 of forgot-password: answer questions and set new password.
 */
public record ResetPasswordRequest(
        @NotBlank(message = "手机号不能为空")
        @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
        String phone,

        @Valid
        List<AnswerItem> answers,

        @NotBlank(message = "新密码不能为空")
        @Size(min = 6, max = 32, message = "密码长度 6-32 位")
        String newPassword
) {
    public record AnswerItem(
            @NotBlank(message = "问题 ID 不能为空")
            String questionId,

            @NotBlank(message = "答案不能为空")
            String answer
    ) {}
}
