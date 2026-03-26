package com.projectaccounts.errorhandling.exceptions;

import com.projectaccounts.errorhandling.response.ErrorCode;
import org.springframework.http.HttpStatus;

/**
 * Thrown when a requested resource cannot be found (404 Not Found).
 */
public class ResourceNotFoundException extends CustomException {
    public ResourceNotFoundException(String message) {
        super(ErrorCode.NOT_FOUND, HttpStatus.NOT_FOUND, message);
    }

    public ResourceNotFoundException(ErrorCode errorCode, String message) {
        super(errorCode, HttpStatus.NOT_FOUND, message);
    }

    public ResourceNotFoundException(String resourceType, Object id) {
        super(ErrorCode.RESOURCE_NOT_FOUND, HttpStatus.NOT_FOUND,
            String.format("%s with ID %s not found", resourceType, id));
    }
}
