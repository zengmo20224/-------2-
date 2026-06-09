package com.petcare.product.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * Request DTO for checking/unchecking cart items.
 */
public record CartItemCheckRequest(
        @NotNull(message = "购物车项ID列表不能为空")
        List<Long> cartItemIds,
        Boolean checked
) {
    public CartItemCheckRequest {
        if (checked == null) {
            checked = true;
        }
    }
}
