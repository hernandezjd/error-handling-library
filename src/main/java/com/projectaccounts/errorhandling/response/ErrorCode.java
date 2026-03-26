package com.projectaccounts.errorhandling.response;

/**
 * Standard error codes used across all REST services.
 * These are machine-readable codes that clients can use for error handling logic.
 */
public enum ErrorCode {
    // Validation errors
    VALIDATION_ERROR,
    MISSING_REQUIRED_FIELD,
    INVALID_FORMAT,
    DUPLICATE_ENTRY,

    // Resource errors
    NOT_FOUND,
    RESOURCE_NOT_FOUND,

    // Authorization errors
    UNAUTHORIZED,
    FORBIDDEN,

    // Conflict errors
    CONFLICT,
    STALE_OBJECT_STATE,

    // Business logic errors
    INVALID_OPERATION,
    INVALID_STATE_TRANSITION,
    INVALID_INITIAL_DATE,
    TRANSACTION_DATE_BEFORE_INITIAL_DATE,
    CLOSED_PERIOD_MODIFICATION,
    TENANT_DEACTIVATED,
    PARENT_ACCOUNT_CANNOT_HAVE_SAME_CODE,
    INVALID_PARENT_ACCOUNT,
    PARENT_ACCOUNT_HAS_CHILDREN,
    ACCOUNT_HAS_TRANSACTIONS,
    THIRD_PARTY_NOT_PAIRED,
    CIRCULAR_PARENT_REFERENCE,

    // Server errors
    INTERNAL_SERVER_ERROR,
    SERVICE_UNAVAILABLE,
    OPERATION_TIMEOUT,

    // Generic
    UNKNOWN_ERROR
}
