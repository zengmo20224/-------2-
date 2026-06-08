package com.petcare.booking.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for admin rejecting a booking.
 */
public record BookingRejectRequest(
        @NotBlank(message = "拒绝原因不能为空")
        String reason
) {
}
