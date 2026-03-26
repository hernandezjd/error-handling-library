package com.projectaccounts.errorhandling.exceptions;

import com.projectaccounts.errorhandling.response.ErrorCode;
import org.springframework.http.HttpStatus;

import java.util.Map;

/**
 * Thrown when request validation fails (e.g., missing fields, invalid format).
 */
public class ValidationException extends CustomException {
    public ValidationException(String message) {
        super(ErrorCode.VALIDATION_ERROR, HttpStatus.BAD_REQUEST, message);
    }

    public ValidationException(String message, Map<String, String> details) {
        super(ErrorCode.VALIDATION_ERROR, HttpStatus.BAD_REQUEST, message, details);
    }

    public ValidationException(ErrorCode errorCode, String message) {
        super(errorCode, HttpStatus.BAD_REQUEST, message);
    }

    public ValidationException(ErrorCode errorCode, String message, Map<String, String> details) {
        super(errorCode, HttpStatus.BAD_REQUEST, message, details);
    }
}
