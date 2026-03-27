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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
        assertThat(response.getBody().errorCode()).isEqualTo(ErrorCode.VALIDATION_ERROR.name());
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
        assertThat(response.getBody().errorCode()).isEqualTo(ErrorCode.RESOURCE_NOT_FOUND.name());
    }

    @Test
    void shouldHandleForbiddenException() {
        ForbiddenException ex = new ForbiddenException("Access denied");

        ResponseEntity<ErrorResponse> response = handler.handleCustomException(ex, null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().errorCode()).isEqualTo(ErrorCode.FORBIDDEN.name());
    }

    @Test
    void shouldHandleConflictException() {
        ConflictException ex = new ConflictException("Duplicate entry");

        ResponseEntity<ErrorResponse> response = handler.handleCustomException(ex, null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().errorCode()).isEqualTo(ErrorCode.CONFLICT.name());
    }

    @Test
    void shouldHandleGeneralExceptionWithInternalServerError() {
        Exception ex = new RuntimeException("Unexpected error");

        ResponseEntity<ErrorResponse> response = handler.handleGeneralException(ex, null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().errorCode()).isEqualTo(ErrorCode.INTERNAL_SERVER_ERROR.name());
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
            .andExpect(jsonPath("$.errorCode").value(ErrorCode.VALIDATION_ERROR.name()))
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
            .andExpect(jsonPath("$.errorCode").value(ErrorCode.RESOURCE_NOT_FOUND.name()))
            .andExpect(jsonPath("$.requestId").isNotEmpty());
    }

    @Test
    void shouldHandleAccessDeniedExceptionWithForbiddenStatus() {
        AccessDeniedException ex = new AccessDeniedException("Access denied by security policy");

        ResponseEntity<ErrorResponse> response = handler.handleAccessDeniedException(ex, null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().errorCode()).isEqualTo(ErrorCode.FORBIDDEN.name());
        assertThat(response.getBody().message()).isEqualTo("You do not have permission to perform this action");
        assertThat(response.getBody().requestId()).isNotNull();
        assertThat(response.getBody().timestamp()).isGreaterThan(0);
    }

    @Test
    void shouldIncludeRequestIdHeaderInAccessDeniedResponse() {
        AccessDeniedException ex = new AccessDeniedException("Access denied");

        ResponseEntity<ErrorResponse> response = handler.handleAccessDeniedException(ex, null);

        assertThat(response.getHeaders().get(RequestIdFilter.REQUEST_ID_HEADER)).isNotNull();
        assertThat(response.getHeaders().get(RequestIdFilter.REQUEST_ID_HEADER)).hasSize(1);
        String headerValue = response.getHeaders().get(RequestIdFilter.REQUEST_ID_HEADER).get(0);
        String bodyRequestId = response.getBody().requestId();
        assertThat(headerValue).isEqualTo(bodyRequestId);
    }

    @Test
    void shouldReturn403WithAccessDeniedViaEndpoint() throws Exception {
        MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(new TestController())
            .setControllerAdvice(handler)
            .addFilters(new RequestIdFilter())
            .build();

        mockMvc.perform(get("/test/access-denied"))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.errorCode").value(ErrorCode.FORBIDDEN.name()))
            .andExpect(jsonPath("$.requestId").isNotEmpty())
            .andExpect(jsonPath("$.message").value("You do not have permission to perform this action"));
    }

    @Test
    void shouldIncludeRequestIdHeaderIn403Response() throws Exception {
        MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(new TestController())
            .setControllerAdvice(handler)
            .addFilters(new RequestIdFilter())
            .build();

        var result = mockMvc.perform(get("/test/access-denied"))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.errorCode").value(ErrorCode.FORBIDDEN.name()))
            .andExpect(jsonPath("$.requestId").isNotEmpty())
            .andReturn();

        // Verify X-Request-Id header is present and is a valid UUID
        String headerValue = result.getResponse().getHeader(RequestIdFilter.REQUEST_ID_HEADER);
        assertThat(headerValue).isNotNull();
        assertThat(headerValue).matches(Pattern.compile("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$"));

        // Verify header value matches the requestId in response body
        String responseBody = result.getResponse().getContentAsString();
        assertThat(responseBody).contains(headerValue);
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

        @GetMapping("/test/access-denied")
        void throwAccessDenied() {
            throw new AccessDeniedException("Access denied by security policy");
        }
    }
}
