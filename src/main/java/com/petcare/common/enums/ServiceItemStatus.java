package com.petcare.common.enums;

import lombok.Getter;

/**
 * Service item status constants.
 * Default: ON_SALE (matches schema.sql `service_item.status` default)
 */
@Getter
public enum ServiceItemStatus {
    ON_SALE("ON_SALE"),
    OFF_SALE("OFF_SALE");

    private final String code;

    ServiceItemStatus(String code) {
        this.code = code;
    }
}
