package com.projectaccounts.errorhandling.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for CorsProperties default values and setters.
 *
 * Critical invariant: exposedHeaders must include "X-Request-Id" so browsers
 * can read this header cross-origin (required by frontend apiClient).
 */
class CorsPropertiesTest {

    private CorsProperties corsProperties;

    @BeforeEach
    void setUp() {
        corsProperties = new CorsProperties();
    }

    @Test
    void shouldDefaultEnabledToTrue() {
        assertThat(corsProperties.isEnabled()).isTrue();
    }

    @Test
    void shouldDefaultAllowedOriginsToLocalhostPorts() {
        List<String> origins = corsProperties.getAllowedOrigins();

        assertThat(origins).contains("http://localhost:5173");
        assertThat(origins).contains("http://localhost:3000");
    }

    @Test
    void shouldDefaultAllowedMethodsToStandardHttpVerbs() {
        List<String> methods = corsProperties.getAllowedMethods();

        assertThat(methods).containsExactlyInAnyOrder("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH");
    }

    @Test
    void shouldDefaultAllowedHeadersToWildcard() {
        assertThat(corsProperties.getAllowedHeaders()).containsExactly("*");
    }

    @Test
    void shouldDefaultExposedHeadersToIncludeRequestId() {
        // Critical invariant: X-Request-Id must be exposed so the frontend can read it cross-origin.
        // Without this, apiClient cannot extract the request ID from error responses.
        assertThat(corsProperties.getExposedHeaders()).contains("X-Request-Id");
    }

    @Test
    void shouldDefaultExposedHeadersToIncludeAllRateLimitHeaders() {
        List<String> exposedHeaders = corsProperties.getExposedHeaders();

        assertThat(exposedHeaders).containsExactlyInAnyOrder(
                "X-RateLimit-Limit",
                "X-RateLimit-Remaining",
                "X-RateLimit-Reset-After",
                "X-Request-Id"
        );
    }

    @Test
    void shouldDefaultAllowCredentialsToTrue() {
        assertThat(corsProperties.isAllowCredentials()).isTrue();
    }

    @Test
    void shouldDefaultMaxAgeTo3600() {
        assertThat(corsProperties.getMaxAge()).isEqualTo(3600L);
    }

    @Test
    void shouldMutateAllowedOriginsViaSetter() {
        List<String> custom = List.of("https://app.example.com", "https://staging.example.com");
        corsProperties.setAllowedOrigins(custom);

        assertThat(corsProperties.getAllowedOrigins()).isEqualTo(custom);
    }

    @Test
    void shouldMutateAllowedMethodsViaSetter() {
        corsProperties.setAllowedMethods(List.of("GET", "POST"));

        assertThat(corsProperties.getAllowedMethods()).containsExactly("GET", "POST");
    }

    @Test
    void shouldMutateExposedHeadersViaSetter() {
        corsProperties.setExposedHeaders(List.of("X-Custom-Header"));

        assertThat(corsProperties.getExposedHeaders()).containsExactly("X-Custom-Header");
    }

    @Test
    void shouldMutateAllowCredentialsViaSetter() {
        corsProperties.setAllowCredentials(false);

        assertThat(corsProperties.isAllowCredentials()).isFalse();
    }

    @Test
    void shouldMutateMaxAgeViaSetter() {
        corsProperties.setMaxAge(7200L);

        assertThat(corsProperties.getMaxAge()).isEqualTo(7200L);
    }

    @Test
    void shouldMutateEnabledViaSetter() {
        corsProperties.setEnabled(false);

        assertThat(corsProperties.isEnabled()).isFalse();
    }
}
