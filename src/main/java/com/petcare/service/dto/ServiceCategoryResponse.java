package com.petcare.service.dto;

/**
 * Response DTO for service category.
 */
public record ServiceCategoryResponse(
        Long id,
        String name,
        String iconUrl,
        Integer sort
) {
}
