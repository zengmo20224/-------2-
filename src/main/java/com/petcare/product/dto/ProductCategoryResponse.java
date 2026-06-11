package com.petcare.product.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.petcare.common.serialization.SnowflakeIdSerializer;

/**
 * Response DTO for a product category.
 */
public record ProductCategoryResponse(
        @JsonSerialize(using = SnowflakeIdSerializer.class) Long id,
        String name,
        String iconUrl,
        Integer sort
) {
}
