package com.petcare.product.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.petcare.common.serialization.SnowflakeIdSerializer;

import java.math.BigDecimal;
import java.util.List;

/**
 * Response DTO for a product detail view.
 */
public record ProductDetailResponse(
        @JsonSerialize(using = SnowflakeIdSerializer.class) Long id,
        @JsonSerialize(using = SnowflakeIdSerializer.class) Long categoryId,
        String categoryName,
        String name,
        String coverUrl,
        BigDecimal price,
        Integer stock,
        Integer salesCount,
        String description,
        Integer pickupOnly,
        List<String> imageUrls
) {
}
