package com.petcare.booking.domain;

import com.petcare.booking.entity.ServiceBooking;
import com.petcare.booking.entity.StaffSchedule;
import com.petcare.booking.entity.StaffUnavailableTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for BookingAvailabilityCalculator.
 * Pure logic tests — no Spring context needed.
 */
class BookingAvailabilityCalculatorTest {

    private static final LocalDate DATE = LocalDate.of(2026, 6, 15);

    @Nested
    @DisplayName("Basic slot generation")
    class BasicSlotGeneration {

        @Test
        @DisplayName("Full day schedule with 30-min slots and 60-min duration")
        void fullDaySchedule() {
            Map<Long, List<StaffSchedule>> schedules = Map.of(
                    1L, List.of(schedule(1L, "09:00", "17:00")));

            Map<LocalTime, Integer> slots = BookingAvailabilityCalculator.calculateAvailableSlots(
                    schedules, Map.of(), Map.of(), 30, 60);

            // 09:00 to 16:00 = 15 slots (last slot: 16:00 + 60min = 17:00)
            assertThat(slots).hasSize(15);
            assertThat(slots.get(LocalTime.of(9, 0))).isEqualTo(1);
            assertThat(slots.get(LocalTime.of(16, 0))).isEqualTo(1);
            // 16:30 + 60min = 17:30 > 17:00, so not available
            assertThat(slots).doesNotContainKey(LocalTime.of(16, 30));
        }

        @Test
        @DisplayName("Service duration equals available interval produces one slot")
        void durationEqualsInterval() {
            Map<Long, List<StaffSchedule>> schedules = Map.of(
                    1L, List.of(schedule(1L, "10:00", "11:00")));

            Map<LocalTime, Integer> slots = BookingAvailabilityCalculator.calculateAvailableSlots(
                    schedules, Map.of(), Map.of(), 30, 60);

            assertThat(slots).hasSize(1);
            assertThat(slots).containsKey(LocalTime.of(10, 0));
        }

        @Test
        @DisplayName("Service duration exceeds available interval produces no slots")
        void durationExceedsInterval() {
            Map<Long, List<StaffSchedule>> schedules = Map.of(
                    1L, List.of(schedule(1L, "10:00", "10:30")));

            Map<LocalTime, Integer> slots = BookingAvailabilityCalculator.calculateAvailableSlots(
                    schedules, Map.of(), Map.of(), 30, 60);

            assertThat(slots).isEmpty();
        }
    }

    @Nested
    @DisplayName("Unavailable time deduction")
    class UnavailableTimeDeduction {

        @Test
        @DisplayName("Lunch break splits schedule into two windows")
        void lunchBreakSplitsSchedule() {
            Map<Long, List<StaffSchedule>> schedules = Map.of(
                    1L, List.of(schedule(1L, "09:00", "17:00")));
            Map<Long, List<StaffUnavailableTime>> unavailable = Map.of(
                    1L, List.of(unavailable(1L, "12:00", "13:00")));

            Map<LocalTime, Integer> slots = BookingAvailabilityCalculator.calculateAvailableSlots(
                    schedules, unavailable, Map.of(), 30, 60);

            // Morning: 09:00-12:00, slots: 09:00, 09:30, 10:00, 10:30, 11:00 (11:30+60=12:30>12:00? no, 11:00+60=12:00 OK)
            // 09:00, 09:30, 10:00, 10:30, 11:00 = 5 slots (11:00+60=12:00 <= 12:00, but subtract makes 12:00 excluded)
            // After 13:00: 13:00-17:00, slots: 13:00, 13:30, 14:00, 14:30, 15:00, 15:30, 16:00 = 7 slots
            assertThat(slots).doesNotContainKey(LocalTime.of(11, 30));
            assertThat(slots).doesNotContainKey(LocalTime.of(12, 0));
            assertThat(slots).doesNotContainKey(LocalTime.of(12, 30));
            assertThat(slots).containsKey(LocalTime.of(13, 0));
        }
    }

    @Nested
    @DisplayName("Active booking deduction")
    class ActiveBookingDeduction {

        @Test
        @DisplayName("PENDING_CONFIRM booking occupies its time slot")
        void pendingConfirmOccupiesSlot() {
            Map<Long, List<StaffSchedule>> schedules = Map.of(
                    1L, List.of(schedule(1L, "09:00", "17:00")));
            Map<Long, List<ServiceBooking>> bookings = Map.of(
                    1L, List.of(booking(1L, "PENDING_CONFIRM", "10:00", "11:00")));

            Map<LocalTime, Integer> slots = BookingAvailabilityCalculator.calculateAvailableSlots(
                    schedules, Map.of(), bookings, 30, 60);

            // 10:00 slot should not be available (10:00+60=11:00 still conflicts)
            // 09:30 is also not available because 09:30+60=10:30 which overlaps 10:00-11:00
            assertThat(slots).doesNotContainKey(LocalTime.of(10, 0));
        }

        @Test
        @DisplayName("CONFIRMED booking occupies its time slot")
        void confirmedOccupiesSlot() {
            Map<Long, List<StaffSchedule>> schedules = Map.of(
                    1L, List.of(schedule(1L, "09:00", "17:00")));
            Map<Long, List<ServiceBooking>> bookings = Map.of(
                    1L, List.of(booking(1L, "CONFIRMED", "10:00", "11:00")));

            Map<LocalTime, Integer> slots = BookingAvailabilityCalculator.calculateAvailableSlots(
                    schedules, Map.of(), bookings, 30, 60);

            assertThat(slots).doesNotContainKey(LocalTime.of(10, 0));
        }

        @Test
        @DisplayName("IN_SERVICE booking occupies its time slot")
        void inServiceOccupiesSlot() {
            Map<Long, List<StaffSchedule>> schedules = Map.of(
                    1L, List.of(schedule(1L, "09:00", "17:00")));
            Map<Long, List<ServiceBooking>> bookings = Map.of(
                    1L, List.of(booking(1L, "IN_SERVICE", "10:00", "11:00")));

            Map<LocalTime, Integer> slots = BookingAvailabilityCalculator.calculateAvailableSlots(
                    schedules, Map.of(), bookings, 30, 60);

            assertThat(slots).doesNotContainKey(LocalTime.of(10, 0));
        }

        @Test
        @DisplayName("CANCELLED booking does NOT occupy time")
        void cancelledDoesNotOccupy() {
            Map<Long, List<StaffSchedule>> schedules = Map.of(
                    1L, List.of(schedule(1L, "09:00", "12:00")));
            Map<Long, List<ServiceBooking>> bookings = Map.of(
                    1L, List.of(booking(1L, "CANCELLED", "10:00", "11:00")));

            Map<LocalTime, Integer> slots = BookingAvailabilityCalculator.calculateAvailableSlots(
                    schedules, Map.of(), bookings, 30, 60);

            assertThat(slots).containsKey(LocalTime.of(10, 0));
        }

        @Test
        @DisplayName("REJECTED booking does NOT occupy time")
        void rejectedDoesNotOccupy() {
            Map<Long, List<StaffSchedule>> schedules = Map.of(
                    1L, List.of(schedule(1L, "09:00", "12:00")));
            Map<Long, List<ServiceBooking>> bookings = Map.of(
                    1L, List.of(booking(1L, "REJECTED", "10:00", "11:00")));

            Map<LocalTime, Integer> slots = BookingAvailabilityCalculator.calculateAvailableSlots(
                    schedules, Map.of(), bookings, 30, 60);

            assertThat(slots).containsKey(LocalTime.of(10, 0));
        }

        @Test
        @DisplayName("COMPLETED booking does NOT occupy time")
        void completedDoesNotOccupy() {
            Map<Long, List<StaffSchedule>> schedules = Map.of(
                    1L, List.of(schedule(1L, "09:00", "12:00")));
            Map<Long, List<ServiceBooking>> bookings = Map.of(
                    1L, List.of(booking(1L, "COMPLETED", "10:00", "11:00")));

            Map<LocalTime, Integer> slots = BookingAvailabilityCalculator.calculateAvailableSlots(
                    schedules, Map.of(), bookings, 30, 60);

            assertThat(slots).containsKey(LocalTime.of(10, 0));
        }
    }

    @Nested
    @DisplayName("Adjacent time not overlapping")
    class AdjacentTime {

        @Test
        @DisplayName("10:00-11:00 and 11:00-12:00 are both available (adjacent, not overlapping)")
        void adjacentBookingsBothAvailable() {
            Map<Long, List<StaffSchedule>> schedules = Map.of(
                    1L, List.of(schedule(1L, "09:00", "13:00")));
            // Existing booking at 10:00-11:00
            Map<Long, List<ServiceBooking>> bookings = Map.of(
                    1L, List.of(booking(1L, "CONFIRMED", "10:00", "11:00")));

            Map<LocalTime, Integer> slots = BookingAvailabilityCalculator.calculateAvailableSlots(
                    schedules, Map.of(), bookings, 30, 60);

            // 11:00 should still be available (11:00+60=12:00, does not overlap 10:00-11:00)
            assertThat(slots).containsKey(LocalTime.of(11, 0));
        }
    }

    @Nested
    @DisplayName("Multi-staff aggregation")
    class MultiStaffAggregation {

        @Test
        @DisplayName("Two staff members aggregate availableStaffCount")
        void twoStaffAggregateCount() {
            Map<Long, List<StaffSchedule>> schedules = Map.of(
                    1L, List.of(schedule(1L, "09:00", "12:00")),
                    2L, List.of(schedule(2L, "09:00", "12:00")));

            Map<LocalTime, Integer> slots = BookingAvailabilityCalculator.calculateAvailableSlots(
                    schedules, Map.of(), Map.of(), 30, 60);

            assertThat(slots.get(LocalTime.of(9, 0))).isEqualTo(2);
            assertThat(slots.get(LocalTime.of(10, 0))).isEqualTo(2);
        }

        @Test
        @DisplayName("One staff booked reduces count to 1")
        void oneStaffBookedReducesCount() {
            Map<Long, List<StaffSchedule>> schedules = Map.of(
                    1L, List.of(schedule(1L, "09:00", "12:00")),
                    2L, List.of(schedule(2L, "09:00", "12:00")));
            Map<Long, List<ServiceBooking>> bookings = Map.of(
                    1L, List.of(booking(1L, "CONFIRMED", "10:00", "11:00")));

            Map<LocalTime, Integer> slots = BookingAvailabilityCalculator.calculateAvailableSlots(
                    schedules, Map.of(), bookings, 30, 60);

            // At 10:00, staff 1 is booked but staff 2 is available
            assertThat(slots.get(LocalTime.of(10, 0))).isEqualTo(1);
        }
    }

    // --- helper methods ---

    private static StaffSchedule schedule(long staffId, String start, String end) {
        StaffSchedule s = new StaffSchedule();
        s.setStaffId(staffId);
        s.setWorkDate(DATE);
        s.setStartTime(LocalTime.parse(start));
        s.setEndTime(LocalTime.parse(end));
        s.setStatus("AVAILABLE");
        return s;
    }

    private static StaffUnavailableTime unavailable(long staffId, String start, String end) {
        StaffUnavailableTime u = new StaffUnavailableTime();
        u.setStaffId(staffId);
        u.setUnavailableDate(DATE);
        u.setStartTime(LocalTime.parse(start));
        u.setEndTime(LocalTime.parse(end));
        u.setReasonType("LUNCH");
        return u;
    }

    private static ServiceBooking booking(long staffId, String status, String start, String end) {
        ServiceBooking b = new ServiceBooking();
        b.setStaffId(staffId);
        b.setBookingDate(DATE);
        b.setStartTime(LocalTime.parse(start));
        b.setEndTime(LocalTime.parse(end));
        b.setStatus(status);
        return b;
    }
}
