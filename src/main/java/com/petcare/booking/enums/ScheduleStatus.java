package com.petcare.booking.enums;

import lombok.Getter;

/**
 * Staff schedule status constants.
 * Default: AVAILABLE (matches schema.sql `staff_schedule.status` default)
 */
@Getter
public enum ScheduleStatus {
    AVAILABLE("AVAILABLE"),
    UNAVAILABLE("UNAVAILABLE");

    private final String code;

    ScheduleStatus(String code) {
        this.code = code;
    }
}
