package com.petcare.booking.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Response DTO for a booking.
 * Does not expose full phone numbers or other sensitive data.
 */
public record BookingResponse(
        Long id,
        String bookingNo,
        Long userId,
        Long petId,
        Long storeId,
        Long serviceItemId,
        Long staffId,
        String serviceMode,
        LocalDate bookingDate,
        LocalTime startTime,
        LocalTime endTime,
        Long addressId,
        BigDecimal distanceKm,
        String contactName,
        String contactPhone,
        BigDecimal price,
        String paymentMethod,
        String paymentStatus,
        String status,
        String remark,
        String merchantRemark,
        LocalDateTime createTime
) {
}
