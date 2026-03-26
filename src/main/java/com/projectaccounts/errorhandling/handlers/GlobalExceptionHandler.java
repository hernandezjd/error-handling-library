package com.projectaccounts.errorhandling.handlers;

import com.projectaccounts.errorhandling.exceptions.CustomException;
import com.projectaccounts.errorhandling.filters.RequestIdFilter;
import com.projectaccounts.errorhandling.response.ErrorCode;
import com.projectaccounts.errorhandling.response.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
        org.slf4j.LoggerFactory.getLogger(GlobalExceptionHandler.class).error(
            "Unhandled exception [requestId={}]", requestId, ex);

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
