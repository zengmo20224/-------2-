package com.petcare.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for sending a message in an AI conversation.
 */
public record AiMessageCreateRequest(

        @NotBlank(message = "消息内容不能为空")
        @Size(min = 1, max = 2000, message = "消息内容长度应在1到2000个字符之间")
        String content
) {}
