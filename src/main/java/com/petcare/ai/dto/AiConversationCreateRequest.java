package com.petcare.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for creating an AI conversation.
 */
public record AiConversationCreateRequest(

        @NotBlank(message = "会话类型不能为空")
        @Pattern(regexp = "CUSTOMER_SERVICE|PET_CHAT", message = "只允许 CUSTOMER_SERVICE 或 PET_CHAT 类型")
        String conversationType,

        @Size(max = 100, message = "标题不能超过100个字符")
        String title
) {}
