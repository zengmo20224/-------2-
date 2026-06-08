package com.petcare.admin.enums;

import lombok.Getter;

/**
 * Admin role code constants.
 * Default: STAFF (matches schema.sql `admin_user.role` default)
 */
@Getter
public enum AdminRoleCode {
    SUPER_ADMIN("SUPER_ADMIN"),
    MANAGER("MANAGER"),
    STAFF("STAFF");

    private final String code;

    AdminRoleCode(String code) {
        this.code = code;
    }
}
