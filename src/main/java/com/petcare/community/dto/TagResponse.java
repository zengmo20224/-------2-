package com.petcare.community.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.petcare.common.serialization.SnowflakeIdSerializer;

/**
 * Response DTO for community tags.
 */
public record TagResponse(
        @JsonSerialize(using = SnowflakeIdSerializer.class) Long id,
        String name,
        Integer usageCount
) {
}
