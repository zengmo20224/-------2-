package com.petcare.service.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.petcare.common.serialization.SnowflakeIdSerializer;
import java.math.BigDecimal;

/**
 * Response DTO for service item.
 */
public record ServiceItemResponse(
        @JsonSerialize(using = SnowflakeIdSerializer.class) Long id,
        @JsonSerialize(using = SnowflakeIdSerializer.class) Long categoryId,
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
