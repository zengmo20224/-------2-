package com.petcare.common.exception;

/**
 * Base exception for business rule violations.
 * Carries a stable error code and user-friendly message.
 */
public class BusinessException extends RuntimeException {

    private final String code;

    public BusinessException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
