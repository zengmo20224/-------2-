package com.petcare.moderation.dto;

import java.time.LocalDateTime;

/**
 * Response DTO for sensitive word queries.
 */
public record SensitiveWordResponse(
        Long id,
        String word,
        String category,
        Integer level,
        String status,
        LocalDateTime createTime
) {
}
