package com.projectaccounts.errorhandling.handlers;

import com.projectaccounts.errorhandling.exceptions.CustomException;
import com.projectaccounts.errorhandling.filters.RequestIdFilter;
import com.projectaccounts.errorhandling.response.ErrorCode;
import com.projectaccounts.errorhandling.response.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for all REST services.
 * Transforms all exceptions to ErrorResponse format with request ID.
 * Never exposes stack traces in responses (logs them instead).
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handle CustomException and its subclasses.
     */
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponse> handleCustomException(CustomException ex, WebRequest request) {
        String requestId = RequestIdFilter.getCurrentRequestId();
        long timestamp = System.currentTimeMillis();

        ErrorResponse response = new ErrorResponse(
            requestId,
            ex.getErrorCode(),
            ex.getMessage(),
            ex.getDetails(),
            timestamp
        );

        return new ResponseEntity<>(response, ex.getHttpStatus());
    }

    /**
     * Handle Spring Security access denied exceptions (403 Forbidden).
     * Explicitly sets X-Request-Id header to ensure it's present in all error responses.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(
            AccessDeniedException ex, WebRequest request) {
        String requestId = RequestIdFilter.getCurrentRequestId();
        long timestamp = System.currentTimeMillis();

        ErrorResponse response = new ErrorResponse(
            requestId,
            ErrorCode.FORBIDDEN,
            "You do not have permission to perform this action",
            timestamp
        );

        return ResponseEntity
            .status(HttpStatus.FORBIDDEN)
            .header(RequestIdFilter.REQUEST_ID_HEADER, requestId)
            .body(response);
    }

    /**
     * Handle validation errors from @Valid annotations.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex, WebRequest request) {
        String requestId = RequestIdFilter.getCurrentRequestId();
        long timestamp = System.currentTimeMillis();

        // Extract field-level validation errors
        Map<String, String> details = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
            details.put(error.getField(), error.getDefaultMessage())
        );

        ErrorResponse response = new ErrorResponse(
            requestId,
            ErrorCode.VALIDATION_ERROR,
            "Validation failed",
            details,
            timestamp
        );

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle generic exceptions (fallback).
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception ex, WebRequest request) {
        String requestId = RequestIdFilter.getCurrentRequestId();
        long timestamp = System.currentTimeMillis();

        // Log the full exception for debugging
        logger.error("Unhandled exception [requestId={}]", requestId, ex);

        // Return safe message to client (no stack trace)
        ErrorResponse response = new ErrorResponse(
            requestId,
            ErrorCode.INTERNAL_SERVER_ERROR,
            "An internal server error occurred. Please contact support if the problem persists.",
            timestamp
        );

        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
