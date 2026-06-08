package com.petcare.booking.enums;

import lombok.Getter;

/**
 * Booking status constants.
 * Default: PENDING_CONFIRM (matches schema.sql `service_booking.status` default)
 */
@Getter
public enum BookingStatus {
    PENDING_CONFIRM("PENDING_CONFIRM"),
    CONFIRMED("CONFIRMED"),
    IN_SERVICE("IN_SERVICE"),
    COMPLETED("COMPLETED"),
    CANCELLED("CANCELLED"),
    REJECTED("REJECTED");

    private final String code;

    BookingStatus(String code) {
        this.code = code;
    }
}
