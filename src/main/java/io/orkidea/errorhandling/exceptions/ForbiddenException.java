package io.orkidea.errorhandling.exceptions;

import io.orkidea.errorhandling.response.ErrorCode;
import org.springframework.http.HttpStatus;

/**
 * Thrown when a user is authenticated but lacks permissions to access a resource (403 Forbidden).
 */
public class ForbiddenException extends CustomException {
    public ForbiddenException(String message) {
        super(ErrorCode.FORBIDDEN, HttpStatus.FORBIDDEN, message);
    }

    public ForbiddenException(ErrorCode errorCode, String message) {
        super(errorCode, HttpStatus.FORBIDDEN, message);
    }
}
