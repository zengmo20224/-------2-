package com.petcare.ai.analytics;

import com.petcare.ai.analytics.dto.BusinessAnalytics;
import com.petcare.booking.mapper.ServiceBookingMapper;
import com.petcare.booking.entity.ServiceBooking;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import java.time.LocalDate;

/**
 * Aggregates booking-related business analytics.
 * Uses parameterized queries via MyBatis-Plus QueryWrapper — never raw SQL concatenation.
 * Outputs a fixed DTO — the AI Provider never sees Mapper or database objects.
 */
public class BusinessAnalyticsAggregator {

    private final ServiceBookingMapper serviceBookingMapper;

    public BusinessAnalyticsAggregator(ServiceBookingMapper serviceBookingMapper) {
        this.serviceBookingMapper = serviceBookingMapper;
    }

    /**
     * Aggregates business analytics for the given date range.
     *
     * @param startDate start date (inclusive), must not be null
     * @param endDate   end date (inclusive), must not be null
     * @return aggregated business analytics DTO
     */
    public BusinessAnalytics aggregate(LocalDate startDate, LocalDate endDate) {
        long totalBookings = serviceBookingMapper.selectCount(
                new QueryWrapper<ServiceBooking>()
                        .ge("booking_date", startDate)
                        .le("booking_date", endDate)
                        .eq("deleted", 0));

        long completedBookings = serviceBookingMapper.selectCount(
                new QueryWrapper<ServiceBooking>()
                        .ge("booking_date", startDate)
                        .le("booking_date", endDate)
                        .eq("status", "COMPLETED")
                        .eq("deleted", 0));

        long cancelledBookings = serviceBookingMapper.selectCount(
                new QueryWrapper<ServiceBooking>()
                        .ge("booking_date", startDate)
                        .le("booking_date", endDate)
                        .eq("status", "CANCELLED")
                        .eq("deleted", 0));

        long pendingBookings = serviceBookingMapper.selectCount(
                new QueryWrapper<ServiceBooking>()
                        .ge("booking_date", startDate)
                        .le("booking_date", endDate)
                        .eq("status", "PENDING_CONFIRM")
                        .eq("deleted", 0));

        return new BusinessAnalytics(
                totalBookings,
                completedBookings,
                cancelledBookings,
                pendingBookings,
                null,
                null,
                0.0
        );
    }
}
