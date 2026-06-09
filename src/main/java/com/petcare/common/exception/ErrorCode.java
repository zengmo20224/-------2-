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

    // Community error codes
    public static final String COMMUNITY_POST_NOT_FOUND = "community_post_not_found";
    public static final String COMMUNITY_TOPIC_NOT_FOUND = "community_topic_not_found";
    public static final String COMMUNITY_COMMENT_NOT_FOUND = "community_comment_not_found";
    public static final String COMMUNITY_CONTENT_REJECTED = "community_content_rejected";
    public static final String COMMUNITY_CONTENT_PENDING_REVIEW = "community_content_pending_review";
    public static final String COMMUNITY_POST_NOT_VISIBLE = "community_post_not_visible";
    public static final String COMMUNITY_DUPLICATE_LIKE = "community_duplicate_like";
    public static final String COMMUNITY_DUPLICATE_FAVORITE = "community_duplicate_favorite";
    public static final String COMMUNITY_DUPLICATE_REPORT = "community_duplicate_report";
    public static final String COMMUNITY_REVIEW_STATUS_INVALID = "community_review_status_invalid";
    public static final String COMMUNITY_SENSITIVE_WORD_DUPLICATE = "community_sensitive_word_duplicate";
    public static final String COMMUNITY_FILE_UPLOAD_NOT_DECIDED = "community_file_upload_not_decided";
}
