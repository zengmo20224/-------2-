package com.petcare.booking.dto;

import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for admin reassigning a booking to a different staff.
 */
public record BookingReassignRequest(
        @NotNull(message = "新员工ID不能为空")
        Long newStaffId
) {
}
