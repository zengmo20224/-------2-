package com.petcare.ai.analytics.dto;

/**
 * Aggregated business analytics data.
 * Passed to AI Provider — never raw SQL or Mapper objects.
 */
public record BusinessAnalytics(
        long totalBookings,
        long completedBookings,
        long cancelledBookings,
        long pendingBookings,
        String busiestDayOfWeek,
        String busiestTimeSlot,
        double avgServiceDurationMinutes
) {}
