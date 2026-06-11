package com.petcare.ai.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.petcare.common.serialization.SnowflakeIdSerializer;

import java.time.LocalDateTime;

/**
 * Response DTO for AI conversation.
 */
public record AiConversationResponse(
        @JsonSerialize(using = SnowflakeIdSerializer.class) Long id,
        String conversationType,
        String title,
        LocalDateTime createTime
) {}
