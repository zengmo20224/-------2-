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
}
