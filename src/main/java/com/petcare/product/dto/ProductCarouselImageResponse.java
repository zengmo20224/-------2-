package com.petcare.product.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.petcare.common.serialization.SnowflakeIdSerializer;

/**
 * Public/admin response DTO for product page carousel images.
 */
public record ProductCarouselImageResponse(
        @JsonSerialize(using = SnowflakeIdSerializer.class) Long id,
        String title,
        String imageUrl,
        String linkType,
        @JsonSerialize(using = SnowflakeIdSerializer.class) Long linkTargetId,
        String status,
        Integer sort
) {
}
