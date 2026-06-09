package com.petcare.product.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Response DTO for a product detail view.
 */
public record ProductDetailResponse(
        Long id,
        Long categoryId,
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
