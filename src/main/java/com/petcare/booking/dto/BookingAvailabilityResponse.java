package com.petcare.booking.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Response DTO for booking availability query.
 * Does not expose staffId — only aggregate counts.
 */
public record BookingAvailabilityResponse(
        Long storeId,
        Long serviceItemId,
        LocalDate bookingDate,
        String serviceMode,
        Integer durationMinutes,
        Integer timeSlotMinutes,
        List<SlotInfo> slots
) {

    /**
     * A single available time slot.
     */
    public record SlotInfo(
            LocalTime startTime,
            LocalTime endTime,
            int availableStaffCount
    ) {
    }
}
