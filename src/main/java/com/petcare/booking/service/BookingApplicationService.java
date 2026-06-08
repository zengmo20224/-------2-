package com.petcare.booking.service;

import com.petcare.booking.dto.BookingAvailabilityRequest;
import com.petcare.booking.dto.BookingAvailabilityResponse;
import com.petcare.booking.dto.BookingCancelRequest;
import com.petcare.booking.dto.BookingCreateRequest;
import com.petcare.booking.dto.BookingReassignRequest;
import com.petcare.booking.dto.BookingResponse;
import com.petcare.common.pagination.PageResponse;

/**
 * Main application service for booking operations.
 * Orchestrates domain rules, concurrency control, and persistence.
 */
public interface BookingApplicationService {

    // --- Availability ---

    /**
     * Calculates available time slots for a service booking.
     * Does not expose staff IDs.
     */
    BookingAvailabilityResponse getAvailability(BookingAvailabilityRequest request);

    // --- User operations ---

    /**
     * Creates a new booking. Auto-assigns staff.
     * currentUserId comes from the security context, not the request body.
     */
    BookingResponse createBooking(Long currentUserId, BookingCreateRequest request);

    /**
     * Lists the current user's bookings, paginated.
     */
    PageResponse<BookingResponse> getMyBookings(Long currentUserId, int page, int size);

    /**
     * Gets a single booking. Verifies ownership for non-admin access.
     */
    BookingResponse getBooking(Long currentUserId, Long bookingId);

    /**
     * Cancels a booking. Verifies ownership and cancel time window.
     */
    BookingResponse cancelBooking(Long currentUserId, Long bookingId, BookingCancelRequest request);

    // --- Admin operations ---

    /**
     * Lists all bookings with optional filters, paginated.
     */
    PageResponse<BookingResponse> listAdminBookings(int page, int size, String status, String bookingDate);

    /**
     * Gets any booking by ID (admin access).
     */
    BookingResponse getAdminBooking(Long bookingId);

    /**
     * Confirms a PENDING_CONFIRM booking.
     */
    BookingResponse confirmBooking(Long bookingId, String merchantRemark, Long operatorId);

    /**
     * Rejects a PENDING_CONFIRM booking.
     */
    BookingResponse rejectBooking(Long bookingId, String reason, Long operatorId);

    /**
     * Starts service for a CONFIRMED booking.
     */
    BookingResponse startBooking(Long bookingId, Long operatorId);

    /**
     * Completes an IN_SERVICE booking.
     */
    BookingResponse completeBooking(Long bookingId, Long operatorId);

    /**
     * Cancels a booking (admin).
     */
    BookingResponse cancelBookingAdmin(Long bookingId, String reason, Long operatorId);

    /**
     * Reassigns a booking to a different staff member.
     */
    BookingResponse reassignBooking(Long bookingId, BookingReassignRequest request, Long operatorId);
}
