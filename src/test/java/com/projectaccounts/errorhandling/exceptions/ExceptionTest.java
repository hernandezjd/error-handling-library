package com.projectaccounts.errorhandling.exceptions;

import com.projectaccounts.errorhandling.response.ErrorCode;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ExceptionTest {

    @Test
    void validationExceptionShouldReturnBadRequestStatus() {
        ValidationException ex = new ValidationException("Invalid field");

        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.VALIDATION_ERROR);
        assertThat(ex.getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(ex.getMessage()).isEqualTo("Invalid field");
        assertThat(ex.getDetails()).isNull();
    }

    @Test
    void validationExceptionWithDetailsShouldIncludeFieldErrors() {
        Map<String, String> details = Map.of("email", "Invalid email format");
        ValidationException ex = new ValidationException("Validation failed", details);

        assertThat(ex.getDetails()).isEqualTo(details);
    }

    @Test
    void resourceNotFoundExceptionShouldReturn404() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Account", "123");

        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.RESOURCE_NOT_FOUND);
        assertThat(ex.getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(ex.getMessage()).contains("Account", "123");
    }

    @Test
    void unauthorizedExceptionShouldReturn401() {
        UnauthorizedException ex = new UnauthorizedException("Missing authentication token");

        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.UNAUTHORIZED);
        assertThat(ex.getHttpStatus()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(ex.getMessage()).isEqualTo("Missing authentication token");
    }

    @Test
    void forbiddenExceptionShouldReturn403() {
        ForbiddenException ex = new ForbiddenException("User lacks permission");

        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.FORBIDDEN);
        assertThat(ex.getHttpStatus()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(ex.getMessage()).isEqualTo("User lacks permission");
    }

    @Test
    void conflictExceptionShouldReturn409() {
        ConflictException ex = new ConflictException("Duplicate entry");

        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.CONFLICT);
        assertThat(ex.getHttpStatus()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(ex.getMessage()).isEqualTo("Duplicate entry");
    }

    @Test
    void exceptionWithCustomErrorCodeShouldUseProvidedCode() {
        ValidationException ex = new ValidationException(ErrorCode.INVALID_FORMAT, "Bad format");

        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.INVALID_FORMAT);
        assertThat(ex.getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
