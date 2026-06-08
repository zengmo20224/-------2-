package com.petcare.common.enums;

import lombok.Getter;

/**
 * Store status constants.
 * Default: OPEN (matches schema.sql `store.status` default)
 */
@Getter
public enum StoreStatus {
    OPEN("OPEN"),
    CLOSED("CLOSED");

    private final String code;

    StoreStatus(String code) {
        this.code = code;
    }
}
