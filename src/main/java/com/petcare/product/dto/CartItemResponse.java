package com.petcare.product.dto;

import java.math.BigDecimal;

/**
 * Response DTO for a cart item.
 */
public record CartItemResponse(
        Long id,
        Long productId,
        String productName,
        String productCoverUrl,
        BigDecimal productPrice,
        Integer productStock,
        Integer quantity,
        Boolean checked,
        BigDecimal subtotal
) {
}
