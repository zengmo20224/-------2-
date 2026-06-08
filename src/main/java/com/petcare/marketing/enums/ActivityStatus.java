package com.petcare.marketing.enums;

import lombok.Getter;

/**
 * Marketing activity status constants.
 * Default: DRAFT (matches schema.sql `marketing_activity.status` default)
 */
@Getter
public enum ActivityStatus {
    DRAFT("DRAFT"),
    ACTIVE("ACTIVE"),
    ENDED("ENDED"),
    CANCELLED("CANCELLED");

    private final String code;

    ActivityStatus(String code) {
        this.code = code;
    }
}
