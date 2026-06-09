package com.petcare.ai.dto;

import java.time.LocalDateTime;

/**
 * Response DTO for AI conversation.
 */
public record AiConversationResponse(
        Long id,
        String conversationType,
        String title,
        LocalDateTime createTime
) {}
