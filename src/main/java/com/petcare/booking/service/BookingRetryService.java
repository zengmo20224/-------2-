package com.petcare.booking.service;

import com.petcare.booking.entity.ServiceBooking;

/**
 * Wraps booking transactions with deadlock/lock-timeout retry logic.
 * Delegates to BookingTransactionService for actual transaction work.
 */
public interface BookingRetryService {

    /**
     * Retries booking creation on deadlock/lock-timeout.
     * Max 2 retries with short random backoff.
     *
     * @return the saved ServiceBooking
     */
    ServiceBooking createBookingWithRetry(ServiceBooking booking);
}
