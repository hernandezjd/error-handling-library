package com.projectaccounts.errorhandling.exceptions;

import com.projectaccounts.errorhandling.response.ErrorCode;
import org.springframework.http.HttpStatus;

/**
 * Thrown when a request conflicts with the current state of a resource (409 Conflict).
 * Examples: duplicate entry, stale object, invalid state transition.
 */
public class ConflictException extends CustomException {
    public ConflictException(String message) {
        super(ErrorCode.CONFLICT, HttpStatus.CONFLICT, message);
    }

    public ConflictException(ErrorCode errorCode, String message) {
        super(errorCode, HttpStatus.CONFLICT, message);
    }
}
