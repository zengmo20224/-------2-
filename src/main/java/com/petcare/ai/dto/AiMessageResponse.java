package com.petcare.ai.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.petcare.common.serialization.SnowflakeIdSerializer;

import java.time.LocalDateTime;

/**
 * Response DTO for AI message.
 */
public record AiMessageResponse(
        @JsonSerialize(using = SnowflakeIdSerializer.class) Long id,
        @JsonSerialize(using = SnowflakeIdSerializer.class) Long conversationId,
        String role,
        String content,
        LocalDateTime createTime
) {}
