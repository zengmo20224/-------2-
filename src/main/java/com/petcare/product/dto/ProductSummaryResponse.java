package com.petcare.product.dto;

import java.math.BigDecimal;

/**
 * Response DTO for a product summary in list views.
 */
public record ProductSummaryResponse(
        Long id,
        Long categoryId,
        String name,
        String coverUrl,
        BigDecimal price,
        Integer stock,
        Integer salesCount,
        Integer sort
) {
}
