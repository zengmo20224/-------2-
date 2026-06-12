package com.petcare.service.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.petcare.common.serialization.SnowflakeIdSerializer;

/**
 * Response DTO for service category.
 */
public record ServiceCategoryResponse(
        @JsonSerialize(using = SnowflakeIdSerializer.class) Long id,
        String name,
        String iconUrl,
        Integer sort
) {
}
