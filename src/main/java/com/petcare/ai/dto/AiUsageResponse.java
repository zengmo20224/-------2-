package com.petcare.ai.dto;

import java.time.LocalDateTime;

/**
 * Response DTO for AI usage log entries.
 * Does not expose full prompts, responses, or provider raw errors.
 */
public record AiUsageResponse(
        Long id,
        Long userId,
        Long adminId,
        String apiType,
        String modelName,
        Integer promptTokens,
        Integer completionTokens,
        Integer totalTokens,
        Boolean success,
        String errorMessage,
        LocalDateTime createTime
) {}
