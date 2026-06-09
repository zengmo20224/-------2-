package com.petcare.community.dto;

import java.time.LocalDateTime;

/**
 * Response DTO for topic queries.
 */
public record TopicResponse(
        Long id,
        String name,
        String description,
        Integer sort,
        LocalDateTime createTime
) {
}
