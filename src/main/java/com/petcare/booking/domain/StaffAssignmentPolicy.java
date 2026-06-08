package com.petcare.booking.domain;

import com.petcare.common.exception.BusinessException;
import com.petcare.common.exception.ErrorCode;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Selects a staff member for a booking using a deterministic policy.
 * Priority: fewest active bookings today, then smallest staffId.
 * User cannot choose or specify staff.
 */
public final class StaffAssignmentPolicy {

    private StaffAssignmentPolicy() {
        // utility class
    }

    /**
     * Selects the best available staff member for a booking.
     *
     * @param availableStaffIds   staff IDs that have the required skill and are active
     * @param bookingCountsByStaffId map of staffId to count of active bookings for the target date
     * @return the selected staffId
     * @throws BusinessException if no staff is available
     */
    public static Long selectStaff(List<Long> availableStaffIds,
                                   Map<Long, Integer> bookingCountsByStaffId) {
        if (availableStaffIds == null || availableStaffIds.isEmpty()) {
            throw new BusinessException(
                    ErrorCode.BOOKING_STAFF_UNAVAILABLE,
                    "当前没有可用的员工来服务该预约");
        }

        return availableStaffIds.stream()
                .min(Comparator
                        .comparingInt((Long id) -> bookingCountsByStaffId.getOrDefault(id, 0))
                        .thenComparingLong(id -> id))
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.BOOKING_STAFF_UNAVAILABLE,
                        "当前没有可用的员工来服务该预约"));
    }
}
