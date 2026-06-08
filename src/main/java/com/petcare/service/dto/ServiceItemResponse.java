package com.petcare.service.dto;

import java.math.BigDecimal;

/**
 * Response DTO for service item.
 */
public record ServiceItemResponse(
        Long id,
        Long categoryId,
        String name,
        String serviceMode,
        BigDecimal price,
        Integer durationMinutes,
        String petType,
        String petSize,
        Integer needAddress,
        Integer needPet,
        String description,
        String coverUrl
) {
}
