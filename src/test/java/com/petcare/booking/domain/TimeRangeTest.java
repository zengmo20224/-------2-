package com.petcare.booking.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for TimeRange value object.
 * Pure logic tests — no Spring context needed.
 */
class TimeRangeTest {

    @Nested
    @DisplayName("Construction validation")
    class Construction {

        @Test
        @DisplayName("Valid range creates successfully")
        void validRangeCreatesSuccessfully() {
            TimeRange range = new TimeRange(LocalTime.of(10, 0), LocalTime.of(11, 0));
            assertThat(range.getStart()).isEqualTo(LocalTime.of(10, 0));
            assertThat(range.getEnd()).isEqualTo(LocalTime.of(11, 0));
        }

        @Test
        @DisplayName("Start equals end throws IllegalArgumentException")
        void startEqualsEndThrows() {
            assertThatThrownBy(() -> new TimeRange(LocalTime.of(10, 0), LocalTime.of(10, 0)))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("start must be before end");
        }

        @Test
        @DisplayName("Start after end throws IllegalArgumentException")
        void startAfterEndThrows() {
            assertThatThrownBy(() -> new TimeRange(LocalTime.of(11, 0), LocalTime.of(10, 0)))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("start must be before end");
        }

        @Test
        @DisplayName("Null start throws NullPointerException")
        void nullStartThrows() {
            assertThatThrownBy(() -> new TimeRange(null, LocalTime.of(11, 0)))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Null end throws NullPointerException")
        void nullEndThrows() {
            assertThatThrownBy(() -> new TimeRange(LocalTime.of(10, 0), null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("Overlap detection")
    class Overlap {

        @Test
        @DisplayName("Partial overlap is detected")
        void partialOverlap() {
            TimeRange a = new TimeRange(LocalTime.of(10, 0), LocalTime.of(11, 0));
            TimeRange b = new TimeRange(LocalTime.of(10, 30), LocalTime.of(11, 30));
            assertThat(a.overlaps(b)).isTrue();
            assertThat(b.overlaps(a)).isTrue();
        }

        @Test
        @DisplayName("Adjacent ranges do NOT overlap")
        void adjacentNoOverlap() {
            TimeRange a = new TimeRange(LocalTime.of(10, 0), LocalTime.of(11, 0));
            TimeRange b = new TimeRange(LocalTime.of(11, 0), LocalTime.of(12, 0));
            assertThat(a.overlaps(b)).isFalse();
            assertThat(b.overlaps(a)).isFalse();
        }

        @Test
        @DisplayName("Containment overlap is detected")
        void containmentOverlap() {
            TimeRange outer = new TimeRange(LocalTime.of(10, 0), LocalTime.of(12, 0));
            TimeRange inner = new TimeRange(LocalTime.of(10, 30), LocalTime.of(11, 30));
            assertThat(outer.overlaps(inner)).isTrue();
            assertThat(inner.overlaps(outer)).isTrue();
        }

        @Test
        @DisplayName("Identical ranges overlap")
        void identicalRangesOverlap() {
            TimeRange a = new TimeRange(LocalTime.of(10, 0), LocalTime.of(11, 0));
            TimeRange b = new TimeRange(LocalTime.of(10, 0), LocalTime.of(11, 0));
            assertThat(a.overlaps(b)).isTrue();
        }

        @Test
        @DisplayName("Completely separate ranges do NOT overlap")
        void separateNoOverlap() {
            TimeRange a = new TimeRange(LocalTime.of(9, 0), LocalTime.of(10, 0));
            TimeRange b = new TimeRange(LocalTime.of(11, 0), LocalTime.of(12, 0));
            assertThat(a.overlaps(b)).isFalse();
        }

        @Test
        @DisplayName("One-minute gap does NOT overlap")
        void oneMinuteGapNoOverlap() {
            TimeRange a = new TimeRange(LocalTime.of(10, 0), LocalTime.of(10, 59));
            TimeRange b = new TimeRange(LocalTime.of(11, 0), LocalTime.of(12, 0));
            assertThat(a.overlaps(b)).isFalse();
        }
    }

    @Nested
    @DisplayName("Duration calculation")
    class Duration {

        @Test
        @DisplayName("Duration is calculated correctly")
        void durationCalculatedCorrectly() {
            TimeRange range = new TimeRange(LocalTime.of(10, 0), LocalTime.of(11, 30));
            assertThat(range.getDurationMinutes()).isEqualTo(90);
        }

        @Test
        @DisplayName("One-hour range has 60 minutes")
        void oneHourIs60Minutes() {
            TimeRange range = new TimeRange(LocalTime.of(9, 0), LocalTime.of(10, 0));
            assertThat(range.getDurationMinutes()).isEqualTo(60);
        }

        @Test
        @DisplayName("One-minute range has 1 minute")
        void oneMinuteRange() {
            TimeRange range = new TimeRange(LocalTime.of(10, 0), LocalTime.of(10, 1));
            assertThat(range.getDurationMinutes()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("Subtraction")
    class Subtraction {

        @Test
        @DisplayName("Subtract non-overlapping returns original")
        void subtractNonOverlapping() {
            TimeRange original = new TimeRange(LocalTime.of(10, 0), LocalTime.of(12, 0));
            TimeRange other = new TimeRange(LocalTime.of(13, 0), LocalTime.of(14, 0));
            List<TimeRange> result = original.subtract(other);
            assertThat(result).hasSize(1);
            assertThat(result.get(0)).isEqualTo(original);
        }

        @Test
        @DisplayName("Subtract middle portion returns two ranges")
        void subtractMiddleReturnsTwo() {
            TimeRange original = new TimeRange(LocalTime.of(9, 0), LocalTime.of(17, 0));
            TimeRange lunch = new TimeRange(LocalTime.of(12, 0), LocalTime.of(13, 0));
            List<TimeRange> result = original.subtract(lunch);
            assertThat(result).hasSize(2);
            assertThat(result.get(0)).isEqualTo(new TimeRange(LocalTime.of(9, 0), LocalTime.of(12, 0)));
            assertThat(result.get(1)).isEqualTo(new TimeRange(LocalTime.of(13, 0), LocalTime.of(17, 0)));
        }

        @Test
        @DisplayName("Subtract left portion returns right remainder")
        void subtractLeftReturnsRight() {
            TimeRange original = new TimeRange(LocalTime.of(9, 0), LocalTime.of(17, 0));
            TimeRange left = new TimeRange(LocalTime.of(8, 0), LocalTime.of(10, 0));
            List<TimeRange> result = original.subtract(left);
            assertThat(result).hasSize(1);
            assertThat(result.get(0)).isEqualTo(new TimeRange(LocalTime.of(10, 0), LocalTime.of(17, 0)));
        }

        @Test
        @DisplayName("Subtract right portion returns left remainder")
        void subtractRightReturnsLeft() {
            TimeRange original = new TimeRange(LocalTime.of(9, 0), LocalTime.of(17, 0));
            TimeRange right = new TimeRange(LocalTime.of(16, 0), LocalTime.of(18, 0));
            List<TimeRange> result = original.subtract(right);
            assertThat(result).hasSize(1);
            assertThat(result.get(0)).isEqualTo(new TimeRange(LocalTime.of(9, 0), LocalTime.of(16, 0)));
        }

        @Test
        @DisplayName("Subtract complete coverage returns empty list")
        void subtractCompleteCoverageReturnsEmpty() {
            TimeRange original = new TimeRange(LocalTime.of(10, 0), LocalTime.of(11, 0));
            TimeRange cover = new TimeRange(LocalTime.of(9, 0), LocalTime.of(12, 0));
            List<TimeRange> result = original.subtract(cover);
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Subtract exact same range returns empty list")
        void subtractExactSameReturnsEmpty() {
            TimeRange original = new TimeRange(LocalTime.of(10, 0), LocalTime.of(11, 0));
            List<TimeRange> result = original.subtract(original);
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("Equality and toString")
    class Equality {

        @Test
        @DisplayName("Equal ranges are equal")
        void equalRanges() {
            TimeRange a = new TimeRange(LocalTime.of(10, 0), LocalTime.of(11, 0));
            TimeRange b = new TimeRange(LocalTime.of(10, 0), LocalTime.of(11, 0));
            assertThat(a).isEqualTo(b);
            assertThat(a.hashCode()).isEqualTo(b.hashCode());
        }

        @Test
        @DisplayName("Different ranges are not equal")
        void differentRanges() {
            TimeRange a = new TimeRange(LocalTime.of(10, 0), LocalTime.of(11, 0));
            TimeRange b = new TimeRange(LocalTime.of(10, 0), LocalTime.of(12, 0));
            assertThat(a).isNotEqualTo(b);
        }

        @Test
        @DisplayName("toString shows range")
        void toStringShowsRange() {
            TimeRange range = new TimeRange(LocalTime.of(10, 0), LocalTime.of(11, 0));
            assertThat(range.toString()).contains("10:00").contains("11:00");
        }
    }
}
