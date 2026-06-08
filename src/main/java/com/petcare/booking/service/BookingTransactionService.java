package com.petcare.booking.service;

import com.petcare.booking.entity.ServiceBooking;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Single-transaction booking operations.
 * Each method runs within a @Transactional boundary.
 * Must be called from a separate bean (e.g. BookingRetryService)
 * to avoid self-invocation proxy issues.
 */
public interface BookingTransactionService {

    /**
     * Attempts a single booking creation within a transaction.
     * Locks the staff-date row, checks for conflicts, then inserts.
     *
     * @return the saved ServiceBooking
     */
    ServiceBooking createBookingOnce(ServiceBooking booking);

    /**
     * Attempts a single staff reassignment within a transaction.
     * Locks the new staff-date row, checks for conflicts, then updates.
     *
     * @param bookingId     the booking to reassign
     * @param newStaffId    the new staff member
     * @param bookingDate   the booking date
     * @param startTime     the booking start time
     * @param endTime       the booking end time
     * @param operatorId    who performed the reassignment
     * @return the updated ServiceBooking
     */
    ServiceBooking reassignBookingOnce(Long bookingId, Long newStaffId,
                                       LocalDate bookingDate,
                                       LocalTime startTime, LocalTime endTime,
                                       Long operatorId);
}
