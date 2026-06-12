package com.petcare.product.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.petcare.common.serialization.SnowflakeIdSerializer;
import java.math.BigDecimal;

/**
 * Response DTO for a product summary in list views.
 */
public record ProductSummaryResponse(
        @JsonSerialize(using = SnowflakeIdSerializer.class) Long id,
        @JsonSerialize(using = SnowflakeIdSerializer.class) Long categoryId,
        String name,
        String coverUrl,
        BigDecimal price,
        Integer stock,
        Integer salesCount,
        Integer sort
) {
}
