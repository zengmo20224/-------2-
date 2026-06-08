package com.petcare.product.enums;

import lombok.Getter;

/**
 * Product order status constants.
 * Default: PENDING_CONFIRM (matches schema.sql `product_order.status` default)
 */
@Getter
public enum ProductOrderStatus {
    PENDING_CONFIRM("PENDING_CONFIRM"),
    PREPARING("PREPARING"),
    READY_FOR_PICKUP("READY_FOR_PICKUP"),
    COMPLETED("COMPLETED"),
    CANCELLED("CANCELLED"),
    OUT_OF_STOCK("OUT_OF_STOCK");

    private final String code;

    ProductOrderStatus(String code) {
        this.code = code;
    }
}
