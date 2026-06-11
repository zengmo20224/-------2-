package com.petcare.booking.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.petcare.common.serialization.SnowflakeIdSerializer;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Response DTO for a booking.
 * Does not expose full phone numbers or other sensitive data.
 */
public record BookingResponse(
        @JsonSerialize(using = SnowflakeIdSerializer.class) Long id,
        String bookingNo,
        @JsonSerialize(using = SnowflakeIdSerializer.class) Long userId,
        @JsonSerialize(using = SnowflakeIdSerializer.class) Long petId,
        @JsonSerialize(using = SnowflakeIdSerializer.class) Long storeId,
        @JsonSerialize(using = SnowflakeIdSerializer.class) Long serviceItemId,
        @JsonSerialize(using = SnowflakeIdSerializer.class) Long staffId,
        String serviceMode,
        LocalDate bookingDate,
        LocalTime startTime,
        LocalTime endTime,
        @JsonSerialize(using = SnowflakeIdSerializer.class) Long addressId,
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
