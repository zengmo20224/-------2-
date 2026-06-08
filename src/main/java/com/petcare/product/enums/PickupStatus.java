package com.petcare.product.enums;

import lombok.Getter;

/**
 * Pickup status constants.
 * Default: WAIT_PREPARE (matches schema.sql `product_order.pickup_status` default)
 */
@Getter
public enum PickupStatus {
    WAIT_PREPARE("WAIT_PREPARE"),
    READY_FOR_PICKUP("READY_FOR_PICKUP"),
    PICKED_UP("PICKED_UP");

    private final String code;

    PickupStatus(String code) {
        this.code = code;
    }
}
