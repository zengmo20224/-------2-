package com.petcare.booking.dto;

/**
 * Request DTO for admin confirming a booking.
 */
public record BookingConfirmRequest(
        String merchantRemark
) {
}
