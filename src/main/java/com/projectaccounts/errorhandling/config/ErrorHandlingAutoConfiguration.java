package com.projectaccounts.errorhandling.config;

import com.projectaccounts.errorhandling.filters.RequestIdFilter;
import com.projectaccounts.errorhandling.handlers.GlobalExceptionHandler;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Spring Boot auto-configuration for error handling library.
 * Automatically registers GlobalExceptionHandler and RequestIdFilter
 * when the library is on the classpath.
 *
 * No configuration needed in client services — just add the dependency.
 */
@AutoConfiguration
public class ErrorHandlingAutoConfiguration {

    /**
     * Register GlobalExceptionHandler as a bean.
     */
    @Bean
    public GlobalExceptionHandler globalExceptionHandler() {
        return new GlobalExceptionHandler();
    }

    /**
     * Register RequestIdFilter as a servlet filter.
     */
    @Bean
    public OncePerRequestFilter requestIdFilter() {
        return new RequestIdFilter();
    }
}
