package com.projectaccounts.errorhandling.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.projectaccounts.errorhandling.filters.InternalHeaderAuthFilter;
import com.projectaccounts.errorhandling.filters.RequestIdFilter;
import com.projectaccounts.errorhandling.handlers.CustomAccessDeniedHandler;
import com.projectaccounts.errorhandling.handlers.GlobalExceptionHandler;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Spring Boot auto-configuration for error handling library.
 * Automatically registers:
 * - GlobalExceptionHandler for centralized error handling
 * - RequestIdFilter for request ID generation and tracking
 * - CORS configuration and CorsFilter for proper CORS headers on all responses (including errors)
 *
 * No configuration needed in client services — just add the dependency.
 * Override CORS settings via app.cors.* properties in application-{profile}.yml.
 */
@AutoConfiguration
@EnableConfigurationProperties(CorsProperties.class)
public class ErrorHandlingAutoConfiguration {

    /**
     * Register GlobalExceptionHandler as a bean.
     */
    @Bean
    public GlobalExceptionHandler globalExceptionHandler() {
        return new GlobalExceptionHandler();
    }

    /**
     * Register CustomAccessDeniedHandler as a bean for use in SecurityConfig.
     * This handler ensures 403 responses include proper error structure with request IDs.
     */
    @Bean
    public CustomAccessDeniedHandler customAccessDeniedHandler() {
        return new CustomAccessDeniedHandler();
    }

    /**
     * Register RequestIdFilter as a servlet filter.
     */
    @Bean
    public OncePerRequestFilter requestIdFilter() {
        return new RequestIdFilter();
    }

    /**
     * Register CorsConfigurationSource bean using CorsProperties.
     * Applies CORS configuration to all paths (/**).
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource(CorsProperties corsProperties) {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(corsProperties.getAllowedOrigins());
        configuration.setAllowedMethods(corsProperties.getAllowedMethods());
        configuration.setAllowedHeaders(corsProperties.getAllowedHeaders());
        configuration.setExposedHeaders(corsProperties.getExposedHeaders());
        configuration.setAllowCredentials(corsProperties.isAllowCredentials());
        configuration.setMaxAge(corsProperties.getMaxAge());

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * Register InternalHeaderAuthFilter to populate Spring Security context from gateway headers.
     */
    @Bean
    public InternalHeaderAuthFilter internalHeaderAuthFilter() {
        return new InternalHeaderAuthFilter(new ObjectMapper());
    }

    /**
     * Register CorsFilter bean to ensure CORS headers are applied to ALL responses,
     * including error responses (403, 401, 500, etc.).
     * This must run BEFORE Spring Security filters to work correctly.
     */
    @Bean
    public CorsFilter corsFilter(CorsConfigurationSource corsConfigurationSource) {
        return new CorsFilter(corsConfigurationSource);
    }
}
