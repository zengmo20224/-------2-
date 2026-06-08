package com.petcare.common.exception;

import com.petcare.common.api.ApiError;
import com.petcare.common.api.ApiResponse;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Global exception handler that converts exceptions into unified ApiResponse format.
 * Ensures error responses never leak SQL, stack traces, or secrets.
 *
 * Spring Security exceptions are primarily handled by RestAuthenticationEntryPoint and
 * RestAccessDeniedHandler. This class provides fallback coverage for edge cases.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handles bean validation errors (e.g. @NotBlank, @Size).
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException ex) {
        List<ApiError.FieldError> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> new ApiError.FieldError(fe.getField(), fe.getDefaultMessage()))
                .toList();

        ApiResponse<Void> body = ApiResponse.error(
                ErrorCode.VALIDATION_ERROR,
                "请求参数不合法",
                fieldErrors
        );
        return ResponseEntity.badRequest().body(body);
    }

    /**
     * Handles business rule violations thrown by service layer.
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusiness(BusinessException ex) {
        HttpStatus status = resolveHttpStatus(ex.getCode());
        ApiResponse<Void> body = ApiResponse.error(ex.getCode(), ex.getMessage());
        return ResponseEntity.status(status).body(body);
    }

    /**
     * Handles Spring Security access denied (403).
     * This is a fallback; primary handling is in RestAccessDeniedHandler.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(AccessDeniedException ex) {
        ApiResponse<Void> body = ApiResponse.error(ErrorCode.FORBIDDEN, "权限不足，无法访问");
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
    }

    /**
     * Handles Spring Security authentication failures (401).
     * This is a fallback; primary handling is in RestAuthenticationEntryPoint.
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthentication(AuthenticationException ex) {
        ApiResponse<Void> body = ApiResponse.error(ErrorCode.UNAUTHORIZED, "认证失败，请先登录");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
    }

    /**
     * Handles 404 Not Found for unknown API paths.
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(NoResourceFoundException ex) {
        ApiResponse<Void> body = ApiResponse.error(
                ErrorCode.RESOURCE_NOT_FOUND,
                "请求的资源不存在"
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    /**
     * Catches all unhandled exceptions.
     * Logs the full stack on the server but returns a generic message to the client.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpected(Exception ex) {
        log.error("Unexpected error", ex);
        ApiResponse<Void> body = ApiResponse.error(
                ErrorCode.INTERNAL_ERROR,
                "服务内部错误，请稍后重试"
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    private HttpStatus resolveHttpStatus(String code) {
        return switch (code) {
            case ErrorCode.VALIDATION_ERROR -> HttpStatus.BAD_REQUEST;
            case ErrorCode.RESOURCE_NOT_FOUND -> HttpStatus.NOT_FOUND;
            case ErrorCode.STATE_CONFLICT -> HttpStatus.CONFLICT;
            case ErrorCode.UNAUTHORIZED -> HttpStatus.UNAUTHORIZED;
            case ErrorCode.FORBIDDEN -> HttpStatus.FORBIDDEN;
            case ErrorCode.WECHAT_LOGIN_NOT_ENABLED -> HttpStatus.UNPROCESSABLE_ENTITY;
            default -> HttpStatus.UNPROCESSABLE_ENTITY;
        };
    }
}
