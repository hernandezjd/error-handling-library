package com.projectaccounts.errorhandling.response;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ErrorResponseTest {

    @Test
    void shouldCreateErrorResponseWithRequestIdAndCode() {
        String requestId = "test-123";
        ErrorCode code = ErrorCode.VALIDATION_ERROR;
        String message = "Invalid input";
        long timestamp = System.currentTimeMillis();

        ErrorResponse response = new ErrorResponse(requestId, code, message, timestamp);

        assertThat(response.requestId()).isEqualTo(requestId);
        assertThat(response.code()).isEqualTo(code.name());
        assertThat(response.message()).isEqualTo(message);
        assertThat(response.timestamp()).isEqualTo(timestamp);
        assertThat(response.details()).isNull();
    }

    @Test
    void shouldCreateErrorResponseWithDetails() {
        String requestId = "test-456";
        ErrorCode code = ErrorCode.VALIDATION_ERROR;
        String message = "Validation failed";
        Map<String, String> details = Map.of("field1", "error1", "field2", "error2");
        long timestamp = System.currentTimeMillis();

        ErrorResponse response = new ErrorResponse(requestId, code, message, details, timestamp);

        assertThat(response.requestId()).isEqualTo(requestId);
        assertThat(response.code()).isEqualTo(code.name());
        assertThat(response.message()).isEqualTo(message);
        assertThat(response.details()).isEqualTo(details);
        assertThat(response.timestamp()).isEqualTo(timestamp);
    }

    @Test
    void shouldCreateErrorResponseWithStringCode() {
        String requestId = "test-789";
        String code = ErrorCode.NOT_FOUND.name();
        String message = "Resource not found";
        long timestamp = System.currentTimeMillis();

        ErrorResponse response = new ErrorResponse(requestId, code, message, timestamp);

        assertThat(response.requestId()).isEqualTo(requestId);
        assertThat(response.code()).isEqualTo(code);
        assertThat(response.message()).isEqualTo(message);
        assertThat(response.timestamp()).isEqualTo(timestamp);
    }
}
