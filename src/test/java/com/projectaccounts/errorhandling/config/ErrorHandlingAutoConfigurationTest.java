package com.projectaccounts.errorhandling.config;

import com.projectaccounts.errorhandling.handlers.CustomAccessDeniedHandler;
import com.projectaccounts.errorhandling.handlers.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.filter.OncePerRequestFilter;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for ErrorHandlingAutoConfiguration bean wiring.
 *
 * Tests verify that all beans are created and that CorsConfigurationSource
 * correctly maps CorsProperties onto CorsConfiguration, including the
 * critical X-Request-Id in exposedHeaders.
 *
 * No Spring context is started — beans are instantiated directly.
 */
class ErrorHandlingAutoConfigurationTest {

    private ErrorHandlingAutoConfiguration configuration;
    private CorsProperties defaultCorsProperties;

    @BeforeEach
    void setUp() {
        configuration = new ErrorHandlingAutoConfiguration();
        defaultCorsProperties = new CorsProperties();
    }

    @Test
    void shouldCreateGlobalExceptionHandlerBean() {
        GlobalExceptionHandler handler = configuration.globalExceptionHandler();

        assertThat(handler).isNotNull();
    }

    @Test
    void shouldCreateCustomAccessDeniedHandlerBean() {
        CustomAccessDeniedHandler handler = configuration.customAccessDeniedHandler();

        assertThat(handler).isNotNull();
    }

    @Test
    void shouldCreateRequestIdFilterBean() {
        OncePerRequestFilter filter = configuration.requestIdFilter();

        assertThat(filter).isNotNull();
    }

    @Test
    void shouldCreateCorsConfigurationSourceWithDefaultAllowedOrigins() {
        CorsConfigurationSource source = configuration.corsConfigurationSource(defaultCorsProperties);
        CorsConfiguration config = source.getCorsConfiguration(new MockHttpServletRequest("GET", "/api/test"));

        assertThat(config).isNotNull();
        assertThat(config.getAllowedOrigins()).contains("http://localhost:5173");
        assertThat(config.getAllowedOrigins()).contains("http://localhost:3000");
    }

    @Test
    void shouldCreateCorsConfigurationSourceWithXRequestIdInExposedHeaders() {
        // Critical invariant: X-Request-Id must be in exposedHeaders so the browser
        // allows the frontend to read it cross-origin.
        CorsConfigurationSource source = configuration.corsConfigurationSource(defaultCorsProperties);
        CorsConfiguration config = source.getCorsConfiguration(new MockHttpServletRequest("GET", "/api/test"));

        assertThat(config).isNotNull();
        assertThat(config.getExposedHeaders()).contains("X-Request-Id");
    }

    @Test
    void shouldCreateCorsConfigurationSourceWithAllowCredentialsTrue() {
        CorsConfigurationSource source = configuration.corsConfigurationSource(defaultCorsProperties);
        CorsConfiguration config = source.getCorsConfiguration(new MockHttpServletRequest("GET", "/api/test"));

        assertThat(config).isNotNull();
        assertThat(config.getAllowCredentials()).isTrue();
    }

    @Test
    void shouldCreateCorsConfigurationSourceWithMaxAge3600() {
        CorsConfigurationSource source = configuration.corsConfigurationSource(defaultCorsProperties);
        CorsConfiguration config = source.getCorsConfiguration(new MockHttpServletRequest("GET", "/api/test"));

        assertThat(config).isNotNull();
        assertThat(config.getMaxAge()).isEqualTo(3600L);
    }

    @Test
    void shouldRegisterCorsConfigurationForAllPaths() {
        CorsConfigurationSource source = configuration.corsConfigurationSource(defaultCorsProperties);

        // Config should resolve for any path, not just the root
        assertThat(source.getCorsConfiguration(new MockHttpServletRequest("GET", "/"))).isNotNull();
        assertThat(source.getCorsConfiguration(new MockHttpServletRequest("POST", "/accounts"))).isNotNull();
        assertThat(source.getCorsConfiguration(new MockHttpServletRequest("GET", "/api/v1/deep/nested/path"))).isNotNull();
    }

    @Test
    void shouldCreateCorsFilterBean() {
        CorsConfigurationSource source = configuration.corsConfigurationSource(defaultCorsProperties);
        CorsFilter filter = configuration.corsFilter(source);

        assertThat(filter).isNotNull();
    }

    @Test
    void shouldUseCorsPropertiesWhenDefaultsAreOverridden() {
        CorsProperties customProps = new CorsProperties();
        customProps.setAllowedOrigins(List.of("https://app.example.com"));
        customProps.setAllowedMethods(List.of("GET", "POST"));
        customProps.setAllowCredentials(false);
        customProps.setMaxAge(1800L);

        CorsConfigurationSource source = configuration.corsConfigurationSource(customProps);
        CorsConfiguration config = source.getCorsConfiguration(new MockHttpServletRequest("GET", "/api/test"));

        assertThat(config).isNotNull();
        assertThat(config.getAllowedOrigins()).containsExactly("https://app.example.com");
        assertThat(config.getAllowedMethods()).containsExactlyInAnyOrder("GET", "POST");
        assertThat(config.getAllowCredentials()).isFalse();
        assertThat(config.getMaxAge()).isEqualTo(1800L);
    }
}
