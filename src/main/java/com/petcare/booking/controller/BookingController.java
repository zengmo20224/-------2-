package com.petcare.booking.controller;

import com.petcare.booking.dto.BookingAvailabilityRequest;
import com.petcare.booking.dto.BookingAvailabilityResponse;
import com.petcare.booking.dto.BookingCancelRequest;
import com.petcare.booking.dto.BookingCreateRequest;
import com.petcare.booking.dto.BookingResponse;
import com.petcare.booking.service.BookingApplicationService;
import com.petcare.common.api.ApiResponse;
import com.petcare.common.exception.BusinessException;
import com.petcare.common.exception.ErrorCode;
import com.petcare.common.pagination.PageResponse;
import com.petcare.common.security.SecurityContextHelper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * User-facing booking endpoints.
 * Requires authenticated user identity (USER role JWT).
 * Availability query is public (no auth required).
 */
@RestController
@RequestMapping("/api/v1/bookings")
public class BookingController {

    private final BookingApplicationService bookingApplicationService;

    public BookingController(BookingApplicationService bookingApplicationService) {
        this.bookingApplicationService = bookingApplicationService;
    }

    /**
     * Query available time slots for a service.
     * Public endpoint — no authentication required.
     */
    @GetMapping("/availability")
    public ResponseEntity<ApiResponse<BookingAvailabilityResponse>> getAvailability(
            @RequestParam Long storeId,
            @RequestParam Long serviceItemId,
            @RequestParam String bookingDate,
            @RequestParam String serviceMode) {
        BookingAvailabilityRequest request = new BookingAvailabilityRequest(
                storeId, serviceItemId, java.time.LocalDate.parse(bookingDate), serviceMode);
        BookingAvailabilityResponse response = bookingApplicationService.getAvailability(request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * Create a new booking.
     * currentUserId comes from the security context, not the request body.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<BookingResponse>> createBooking(
            @Valid @RequestBody BookingCreateRequest request) {
        Long currentUserId = resolveCurrentUserId();
        BookingResponse response = bookingApplicationService.createBooking(currentUserId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    /**
     * List current user's bookings.
     */
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<PageResponse<BookingResponse>>> getMyBookings(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long currentUserId = resolveCurrentUserId();
        PageResponse<BookingResponse> response = bookingApplicationService.getMyBookings(currentUserId, page, size);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * Get a single booking detail. Ownership is verified.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BookingResponse>> getBooking(@PathVariable Long id) {
        Long currentUserId = resolveCurrentUserId();
        BookingResponse response = bookingApplicationService.getBooking(currentUserId, id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * Cancel a booking. Ownership and cancel time window are verified.
     */
    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<BookingResponse>> cancelBooking(
            @PathVariable Long id,
            @RequestBody(required = false) BookingCancelRequest request) {
        Long currentUserId = resolveCurrentUserId();
        String reason = request != null ? request.reason() : null;
        BookingResponse response = bookingApplicationService.cancelBooking(
                currentUserId, id, new BookingCancelRequest(reason));
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * Resolves current user ID from the security context.
     */
    private Long resolveCurrentUserId() {
        return SecurityContextHelper.getCurrentUserId()
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED, "请先登录"));
    }
}
