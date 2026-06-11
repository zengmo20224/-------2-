package com.petcare.ai.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.petcare.common.serialization.SnowflakeIdSerializer;

import java.time.LocalDateTime;

/**
 * Response DTO for AI usage log entries.
 * Does not expose full prompts, responses, or provider raw errors.
 */
public record AiUsageResponse(
        @JsonSerialize(using = SnowflakeIdSerializer.class) Long id,
        @JsonSerialize(using = SnowflakeIdSerializer.class) Long userId,
        @JsonSerialize(using = SnowflakeIdSerializer.class) Long adminId,
        String apiType,
        String modelName,
        Integer promptTokens,
        Integer completionTokens,
        Integer totalTokens,
        Boolean success,
        String errorMessage,
        LocalDateTime createTime
) {}
