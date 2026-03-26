package com.projectaccounts.errorhandling.exceptions;

import com.projectaccounts.errorhandling.response.ErrorCode;
import org.springframework.http.HttpStatus;

import java.util.Map;

/**
 * Base exception class for all application-level exceptions.
 * Includes error code, HTTP status, and optional details for field-level errors.
 */
public abstract class CustomException extends RuntimeException {
    private final ErrorCode errorCode;
    private final HttpStatus httpStatus;
    private final Map<String, String> details;

    // Constructor with just message
    public CustomException(ErrorCode errorCode, HttpStatus httpStatus, String message) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
        this.details = null;
    }

    // Constructor with message and details
    public CustomException(ErrorCode errorCode, HttpStatus httpStatus, String message, Map<String, String> details) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
        this.details = details;
    }

    // Constructor with message and cause (for wrapping checked exceptions)
    public CustomException(ErrorCode errorCode, HttpStatus httpStatus, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
        this.details = null;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public Map<String, String> getDetails() {
        return details;
    }
}
