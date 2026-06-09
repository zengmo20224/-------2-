package com.petcare.product.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for adding a product to the cart.
 */
public record CartItemCreateRequest(
        @NotNull(message = "商品ID不能为空")
        Long productId,
        @NotNull(message = "数量不能为空")
        @Min(value = 1, message = "数量最少为1")
        @Max(value = 99, message = "数量最多为99")
        Integer quantity
) {
}
