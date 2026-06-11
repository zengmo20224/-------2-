package com.petcare.product.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.petcare.common.serialization.SnowflakeIdSerializer;

import java.math.BigDecimal;

/**
 * Response DTO for a cart item.
 */
public record CartItemResponse(
        @JsonSerialize(using = SnowflakeIdSerializer.class) Long id,
        @JsonSerialize(using = SnowflakeIdSerializer.class) Long productId,
        String productName,
        String productCoverUrl,
        BigDecimal productPrice,
        Integer productStock,
        Integer quantity,
        Boolean checked,
        BigDecimal subtotal
) {
}
