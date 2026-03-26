package com.projectaccounts.errorhandling.handlers;

import com.projectaccounts.errorhandling.exceptions.ConflictException;
import com.projectaccounts.errorhandling.exceptions.ForbiddenException;
import com.projectaccounts.errorhandling.exceptions.ResourceNotFoundException;
import com.projectaccounts.errorhandling.exceptions.ValidationException;
import com.projectaccounts.errorhandling.filters.RequestIdFilter;
import com.projectaccounts.errorhandling.response.ErrorCode;
import com.projectaccounts.errorhandling.response.ErrorResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        // Set up a request ID for tests
        System.setProperty("com.projectaccounts.errorhandling.test", "true");
    }

    @Test
    void shouldHandleCustomExceptionWithErrorResponse() {
        ValidationException ex = new ValidationException("Invalid input");

        ResponseEntity<ErrorResponse> response = handler.handleCustomException(ex, null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo(ErrorCode.VALIDATION_ERROR.name());
        assertThat(response.getBody().message()).isEqualTo("Invalid input");
        assertThat(response.getBody().requestId()).isNotNull();
        assertThat(response.getBody().timestamp()).isGreaterThan(0);
    }

    @Test
    void shouldIncludeDetailsInValidationException() {
        Map<String, String> details = Map.of("email", "Invalid email");
        ValidationException ex = new ValidationException("Validation failed", details);

        ResponseEntity<ErrorResponse> response = handler.handleCustomException(ex, null);

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().details()).isEqualTo(details);
    }

    @Test
    void shouldHandleResourceNotFoundException() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Account", "123");

        ResponseEntity<ErrorResponse> response = handler.handleCustomException(ex, null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo(ErrorCode.RESOURCE_NOT_FOUND.name());
    }

    @Test
    void shouldHandleForbiddenException() {
        ForbiddenException ex = new ForbiddenException("Access denied");

        ResponseEntity<ErrorResponse> response = handler.handleCustomException(ex, null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo(ErrorCode.FORBIDDEN.name());
    }

    @Test
    void shouldHandleConflictException() {
        ConflictException ex = new ConflictException("Duplicate entry");

        ResponseEntity<ErrorResponse> response = handler.handleCustomException(ex, null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo(ErrorCode.CONFLICT.name());
    }

    @Test
    void shouldHandleGeneralExceptionWithInternalServerError() {
        Exception ex = new RuntimeException("Unexpected error");

        ResponseEntity<ErrorResponse> response = handler.handleGeneralException(ex, null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo(ErrorCode.INTERNAL_SERVER_ERROR.name());
        // Should not expose stack trace
        assertThat(response.getBody().message()).doesNotContain("RuntimeException");
    }

    /**
     * Integration test with MockMvc to verify full error handling flow.
     */
    @Test
    void shouldReturn400WithValidationErrorViaEndpoint() throws Exception {
        MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(new TestController())
            .setControllerAdvice(handler)
            .addFilters(new RequestIdFilter())
            .build();

        mockMvc.perform(get("/test/validation-error"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(ErrorCode.VALIDATION_ERROR.name()))
            .andExpect(jsonPath("$.requestId").isNotEmpty())
            .andExpect(jsonPath("$.timestamp").isNumber());
    }

    @Test
    void shouldReturn404WithNotFoundErrorViaEndpoint() throws Exception {
        MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(new TestController())
            .setControllerAdvice(handler)
            .addFilters(new RequestIdFilter())
            .build();

        mockMvc.perform(get("/test/not-found"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value(ErrorCode.RESOURCE_NOT_FOUND.name()))
            .andExpect(jsonPath("$.requestId").isNotEmpty());
    }

    /**
     * Test controller for integration tests.
     */
    @RestController
    static class TestController {
        @GetMapping("/test/validation-error")
        void throwValidationError() {
            throw new ValidationException("Test validation error");
        }

        @GetMapping("/test/not-found")
        void throwNotFound() {
            throw new ResourceNotFoundException("TestResource", "123");
        }
    }
}
