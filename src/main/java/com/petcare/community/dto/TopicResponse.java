package com.petcare.community.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.petcare.common.serialization.SnowflakeIdSerializer;

import java.time.LocalDateTime;

/**
 * Response DTO for topic queries.
 */
public record TopicResponse(
        @JsonSerialize(using = SnowflakeIdSerializer.class) Long id,
        String name,
        String description,
        Integer sort,
        LocalDateTime createTime
) {
}
