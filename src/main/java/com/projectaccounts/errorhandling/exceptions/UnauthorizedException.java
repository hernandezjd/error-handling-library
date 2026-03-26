package com.projectaccounts.errorhandling.exceptions;

import com.projectaccounts.errorhandling.response.ErrorCode;
import org.springframework.http.HttpStatus;

/**
 * Thrown when a request is missing valid authentication credentials (401 Unauthorized).
 */
public class UnauthorizedException extends CustomException {
    public UnauthorizedException(String message) {
        super(ErrorCode.UNAUTHORIZED, HttpStatus.UNAUTHORIZED, message);
    }

    public UnauthorizedException(ErrorCode errorCode, String message) {
        super(errorCode, HttpStatus.UNAUTHORIZED, message);
    }
}
