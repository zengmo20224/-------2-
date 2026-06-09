package com.petcare.product.dto;

/**
 * Response DTO for a product category.
 */
public record ProductCategoryResponse(
        Long id,
        String name,
        String iconUrl,
        Integer sort
) {
}
