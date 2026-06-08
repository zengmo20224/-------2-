package com.petcare.booking.domain;

import com.petcare.common.exception.BusinessException;
import com.petcare.common.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for StaffAssignmentPolicy.
 * Pure logic tests — no Spring context needed.
 */
class StaffAssignmentPolicyTest {

    @Nested
    @DisplayName("Staff selection")
    class StaffSelection {

        @Test
        @DisplayName("Single available staff is selected")
        void singleStaffSelected() {
            Long result = StaffAssignmentPolicy.selectStaff(List.of(100L), Map.of());
            assertThat(result).isEqualTo(100L);
        }

        @Test
        @DisplayName("Staff with fewer bookings is selected")
        void fewerBookingsSelected() {
            Map<Long, Integer> counts = Map.of(100L, 5, 200L, 2, 300L, 8);
            Long result = StaffAssignmentPolicy.selectStaff(List.of(100L, 200L, 300L), counts);
            assertThat(result).isEqualTo(200L);
        }

        @Test
        @DisplayName("Same booking count selects smaller staffId")
        void sameCountSelectsSmallerId() {
            Map<Long, Integer> counts = Map.of(300L, 3, 100L, 3, 200L, 3);
            Long result = StaffAssignmentPolicy.selectStaff(List.of(300L, 100L, 200L), counts);
            assertThat(result).isEqualTo(100L);
        }

        @Test
        @DisplayName("Staff not in bookingCountsMap defaults to 0")
        void missingCountDefaultsToZero() {
            Map<Long, Integer> counts = Map.of(100L, 5);
            // Staff 200L has no entry → count = 0 → should be selected
            Long result = StaffAssignmentPolicy.selectStaff(List.of(100L, 200L), counts);
            assertThat(result).isEqualTo(200L);
        }

        @Test
        @DisplayName("Empty bookingCountsMap selects smallest staffId")
        void emptyCountsSelectsSmallest() {
            Long result = StaffAssignmentPolicy.selectStaff(
                    List.of(300L, 100L, 200L), Collections.emptyMap());
            assertThat(result).isEqualTo(100L);
        }
    }

    @Nested
    @DisplayName("Error handling")
    class ErrorHandling {

        @Test
        @DisplayName("Empty staff list throws BOOKING_STAFF_UNAVAILABLE")
        void emptyListThrows() {
            assertThatThrownBy(() -> StaffAssignmentPolicy.selectStaff(List.of(), Map.of()))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code").isEqualTo(ErrorCode.BOOKING_STAFF_UNAVAILABLE);
        }

        @Test
        @DisplayName("Null staff list throws BOOKING_STAFF_UNAVAILABLE")
        void nullListThrows() {
            assertThatThrownBy(() -> StaffAssignmentPolicy.selectStaff(null, Map.of()))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code").isEqualTo(ErrorCode.BOOKING_STAFF_UNAVAILABLE);
        }
    }
}
