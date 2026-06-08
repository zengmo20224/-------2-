package com.petcare.common.exception;

/**
 * Application-wide error codes.
 * Each code maps to a specific error scenario with a stable identifier.
 */
public final class ErrorCode {

    private ErrorCode() {
        // prevent instantiation
    }

    public static final String VALIDATION_ERROR = "validation_error";
    public static final String RESOURCE_NOT_FOUND = "resource_not_found";
    public static final String BUSINESS_RULE_VIOLATION = "business_rule_violation";
    public static final String STATE_CONFLICT = "state_conflict";
    public static final String UNAUTHORIZED = "unauthorized";
    public static final String FORBIDDEN = "forbidden";
    public static final String WECHAT_LOGIN_NOT_ENABLED = "wechat_login_not_enabled";
    public static final String INVALID_TOKEN = "invalid_token";
    public static final String EXPIRED_TOKEN = "expired_token";
    public static final String INTERNAL_ERROR = "internal_error";

    // Booking error codes
    public static final String BOOKING_TIME_CONFLICT = "booking_time_conflict";
    public static final String BOOKING_SLOT_UNAVAILABLE = "booking_slot_unavailable";
    public static final String BOOKING_STATUS_INVALID = "booking_status_invalid";
    public static final String BOOKING_SERVICE_UNAVAILABLE = "booking_service_unavailable";
    public static final String BOOKING_DATE_OUT_OF_RANGE = "booking_date_out_of_range";
    public static final String BOOKING_HOME_DISTANCE_EXCEEDED = "booking_home_distance_exceeded";
    public static final String BOOKING_ADDRESS_REQUIRED = "booking_address_required";
    public static final String BOOKING_ADDRESS_NOT_FOUND = "booking_address_not_found";
    public static final String BOOKING_STAFF_UNAVAILABLE = "booking_staff_unavailable";
    public static final String BOOKING_RETRY_EXHAUSTED = "booking_retry_exhausted";
}
