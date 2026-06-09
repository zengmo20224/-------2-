package com.petcare.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for creating a pickup order.
 * Does NOT contain userId, price, or amount — those come from server-side.
 */
public record ProductOrderCreateRequest(
        @NotNull(message = "门店ID不能为空")
        Long storeId,
        @NotBlank(message = "联系人姓名不能为空")
        String contactName,
        @NotBlank(message = "联系电话不能为空")
        String contactPhone,
        String remark
) {
}
