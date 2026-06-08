package com.petcare.common.enums;

import lombok.Getter;

/**
 * User status constants.
 * Default: ACTIVE (matches schema.sql `user.status` default)
 */
@Getter
public enum UserStatus {
    ACTIVE("ACTIVE"),
    DISABLED("DISABLED");

    private final String code;

    UserStatus(String code) {
        this.code = code;
    }
}
