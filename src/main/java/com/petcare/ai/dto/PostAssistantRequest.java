package com.petcare.ai.dto;

import jakarta.validation.constraints.Size;

/**
 * Request DTO for AI post assistant.
 * Only accepts facts explicitly provided by the user.
 */
public record PostAssistantRequest(

        @Size(max = 50, message = "宠物称呼不能超过50个字符")
        String petName,

        @Size(max = 20, message = "宠物类型不能超过20个字符")
        String petType,

        @Size(min = 1, max = 500, message = "事件描述长度应在1到500个字符之间")
        String event,

        @Size(max = 20, message = "语气风格不能超过20个字符")
        String tone,

        @Size(max = 1000, message = "原始文案不能超过1000个字符")
        String originalText
) {}
