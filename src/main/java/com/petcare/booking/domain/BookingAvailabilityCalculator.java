package com.petcare.booking.domain;

import com.petcare.booking.entity.ServiceBooking;
import com.petcare.booking.entity.StaffSchedule;
import com.petcare.booking.entity.StaffUnavailableTime;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Pure static calculator for available booking time slots.
 * Does not access the database — takes pre-loaded data as input.
 */
public final class BookingAvailabilityCalculator {

    /** Booking statuses that occupy staff time. */
    private static final Set<String> ACTIVE_STATUSES = Set.of(
            "PENDING_CONFIRM", "CONFIRMED", "IN_SERVICE");

    private BookingAvailabilityCalculator() {
        // utility class
    }

    /**
     * Calculates available time slots across multiple staff members.
     *
     * @param schedules        staff schedules for the target date, keyed by staffId
     * @param unavailableTimes staff unavailable times for the target date, keyed by staffId
     * @param activeBookings   existing active bookings for the target date, keyed by staffId
     * @param slotMinutes      time slot granularity from store_config
     * @param durationMinutes  service duration from service_item
     * @return map of startTime to available staff count (no staffId exposed)
     */
    public static Map<LocalTime, Integer> calculateAvailableSlots(
            Map<Long, List<StaffSchedule>> schedules,
            Map<Long, List<StaffUnavailableTime>> unavailableTimes,
            Map<Long, List<ServiceBooking>> activeBookings,
            int slotMinutes,
            int durationMinutes) {

        Map<LocalTime, Integer> slotCounts = new HashMap<>();

        for (Map.Entry<Long, List<StaffSchedule>> entry : schedules.entrySet()) {
            Long staffId = entry.getKey();
            List<StaffSchedule> staffSchedules = entry.getValue();

            List<StaffUnavailableTime> staffUnavailable =
                    unavailableTimes.getOrDefault(staffId, List.of());
            List<ServiceBooking> staffBookings =
                    activeBookings.getOrDefault(staffId, List.of());

            List<TimeRange> availableWindows = computeAvailableWindows(
                    staffSchedules, staffUnavailable, staffBookings);

            generateSlots(availableWindows, slotMinutes, durationMinutes, slotCounts);
        }

        return slotCounts;
    }

    /**
     * Computes available time windows for a single staff member.
     */
    static List<TimeRange> computeAvailableWindows(
            List<StaffSchedule> schedules,
            List<StaffUnavailableTime> unavailableTimes,
            List<ServiceBooking> activeBookings) {

        // Start with schedule working intervals
        List<TimeRange> windows = new ArrayList<>();
        for (StaffSchedule schedule : schedules) {
            if ("AVAILABLE".equals(schedule.getStatus())) {
                windows.add(new TimeRange(schedule.getStartTime(), schedule.getEndTime()));
            }
        }

        // Subtract unavailable times
        for (StaffUnavailableTime ut : unavailableTimes) {
            TimeRange unavailable = new TimeRange(ut.getStartTime(), ut.getEndTime());
            windows = subtractFromAll(windows, unavailable);
        }

        // Subtract active bookings
        for (ServiceBooking booking : activeBookings) {
            if (ACTIVE_STATUSES.contains(booking.getStatus())) {
                TimeRange booked = new TimeRange(booking.getStartTime(), booking.getEndTime());
                windows = subtractFromAll(windows, booked);
            }
        }

        return windows;
    }

    /**
     * Generates candidate start times within available windows and counts them.
     */
    static void generateSlots(
            List<TimeRange> windows,
            int slotMinutes,
            int durationMinutes,
            Map<LocalTime, Integer> slotCounts) {

        for (TimeRange window : windows) {
            LocalTime candidate = window.getStart();
            LocalTime end = window.getEnd();

            while (!candidate.plusMinutes(durationMinutes).isAfter(end)) {
                slotCounts.merge(candidate, 1, Integer::sum);
                candidate = candidate.plusMinutes(slotMinutes);
            }
        }
    }

    /**
     * Subtracts a range from all windows, returning the flattened result.
     */
    private static List<TimeRange> subtractFromAll(List<TimeRange> windows, TimeRange toSubtract) {
        List<TimeRange> result = new ArrayList<>();
        for (TimeRange window : windows) {
            result.addAll(window.subtract(toSubtract));
        }
        return result;
    }
}
