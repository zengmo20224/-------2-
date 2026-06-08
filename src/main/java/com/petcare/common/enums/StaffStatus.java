package com.petcare.common.enums;

import lombok.Getter;

/**
 * Staff status constants.
 * Default: ACTIVE (matches schema.sql `staff.status` default)
 */
@Getter
public enum StaffStatus {
    ACTIVE("ACTIVE"),
    INACTIVE("INACTIVE");

    private final String code;

    StaffStatus(String code) {
        this.code = code;
    }
}
