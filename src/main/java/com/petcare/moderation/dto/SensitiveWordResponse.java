package com.petcare.moderation.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.petcare.common.serialization.SnowflakeIdSerializer;

import java.time.LocalDateTime;

/**
 * Response DTO for sensitive word queries.
 */
public record SensitiveWordResponse(
        @JsonSerialize(using = SnowflakeIdSerializer.class) Long id,
        String word,
        String category,
        Integer level,
        String status,
        LocalDateTime createTime
) {
}
