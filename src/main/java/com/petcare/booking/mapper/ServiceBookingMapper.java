package com.petcare.booking.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.petcare.booking.entity.ServiceBooking;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Mapper
public interface ServiceBookingMapper extends BaseMapper<ServiceBooking> {

    /**
     * Finds bookings that conflict with the requested time range.
     * A conflict occurs when: existing.start < requested.end AND existing.end > requested.start
     * Only checks active statuses: PENDING_CONFIRM, CONFIRMED, IN_SERVICE.
     *
     * @param excludeBookingId booking ID to exclude (for reassignment), nullable
     */
    List<ServiceBooking> selectConflictingBookings(@Param("staffId") Long staffId,
                                                   @Param("bookingDate") LocalDate bookingDate,
                                                   @Param("startTime") LocalTime startTime,
                                                   @Param("endTime") LocalTime endTime,
                                                   @Param("excludeBookingId") Long excludeBookingId);

    /**
     * Locks a booking row for atomic status transition.
     * Must be called within a @Transactional context.
     */
    ServiceBooking selectBookingForUpdate(@Param("bookingId") Long bookingId);
}
