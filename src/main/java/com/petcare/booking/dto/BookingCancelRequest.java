package com.petcare.booking.dto;

/**
 * Request DTO for cancelling a booking.
 */
public record BookingCancelRequest(
        String reason
) {
}
