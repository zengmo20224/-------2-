package com.petcare.ai.dto;

import java.time.LocalDateTime;

/**
 * Response DTO for AI message.
 */
public record AiMessageResponse(
        Long id,
        Long conversationId,
        String role,
        String content,
        LocalDateTime createTime
) {}
