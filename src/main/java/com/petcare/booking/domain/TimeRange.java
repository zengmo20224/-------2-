package com.petcare.booking.domain;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Immutable value object representing a time interval [start, end).
 * Start is inclusive, end is exclusive — adjacent ranges do not overlap.
 */
public final class TimeRange {

    private final LocalTime start;
    private final LocalTime end;

    public TimeRange(LocalTime start, LocalTime end) {
        Objects.requireNonNull(start, "start must not be null");
        Objects.requireNonNull(end, "end must not be null");
        if (!start.isBefore(end)) {
            throw new IllegalArgumentException(
                    "start must be before end, got [" + start + ", " + end + ")");
        }
        this.start = start;
        this.end = end;
    }

    /**
     * Returns true if this range overlaps with the other.
     * Overlap formula: this.start &lt; other.end AND this.end &gt; other.start
     * Adjacent ranges (e.g. 10:00-11:00 and 11:00-12:00) do NOT overlap.
     */
    public boolean overlaps(TimeRange other) {
        Objects.requireNonNull(other, "other must not be null");
        return this.start.isBefore(other.end) && this.end.isAfter(other.start);
    }

    /**
     * Returns the duration of this range in minutes.
     */
    public int getDurationMinutes() {
        return (end.toSecondOfDay() - start.toSecondOfDay()) / 60;
    }

    /**
     * Subtracts the given range from this range.
     * Returns 0, 1, or 2 resulting ranges.
     * If there is no overlap, returns a list containing only this range.
     */
    public List<TimeRange> subtract(TimeRange other) {
        Objects.requireNonNull(other, "other must not be null");
        if (!this.overlaps(other)) {
            return Collections.singletonList(this);
        }

        List<TimeRange> result = new ArrayList<>(2);

        // Left remaining part: [this.start, other.start)
        if (this.start.isBefore(other.start)) {
            result.add(new TimeRange(this.start, other.start));
        }

        // Right remaining part: [other.end, this.end)
        if (other.end.isBefore(this.end)) {
            result.add(new TimeRange(other.end, this.end));
        }

        return Collections.unmodifiableList(result);
    }

    public LocalTime getStart() {
        return start;
    }

    public LocalTime getEnd() {
        return end;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TimeRange that)) return false;
        return start.equals(that.start) && end.equals(that.end);
    }

    @Override
    public int hashCode() {
        return Objects.hash(start, end);
    }

    @Override
    public String toString() {
        return "[" + start + ", " + end + ")";
    }
}
