package com.petcare.booking.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/**
 * Request DTO for querying booking availability.
 */
public record BookingAvailabilityRequest(

        @NotNull(message = "门店ID不能为空")
        Long storeId,

        @NotNull(message = "服务项目ID不能为空")
        Long serviceItemId,

        @NotNull(message = "预约日期不能为空")
        LocalDate bookingDate,

        @NotNull(message = "服务模式不能为空")
        String serviceMode
) {
}
