package com.projectaccounts.errorhandling.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

/**
 * Standard error response format returned by all REST services.
 * Includes request ID for debugging and error code for client-side logic.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
    String requestId,
    String errorCode,
    String message,
    Map<String, String> details,
    long timestamp
) {
    public ErrorResponse(String requestId, String errorCode, String message, long timestamp) {
        this(requestId, errorCode, message, null, timestamp);
    }

    public ErrorResponse(String requestId, ErrorCode errorCode, String message, long timestamp) {
        this(requestId, errorCode.name(), message, null, timestamp);
    }

    public ErrorResponse(String requestId, ErrorCode errorCode, String message, Map<String, String> details, long timestamp) {
        this(requestId, errorCode.name(), message, details, timestamp);
    }
}
