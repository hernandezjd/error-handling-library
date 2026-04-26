package com.projectaccounts.errorhandling.config;

import java.util.Arrays;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * CORS configuration properties.
 * Prefix: app.cors
 *
 * Can be overridden per-environment via application-{profile}.yml.
 * Defaults allow localhost development on ports 5173 (frontend) and 3000 (alternate frontend).
 */
@Component
@ConfigurationProperties(prefix = "app.cors")
public class CorsProperties {
    private boolean enabled = false;
    private List<String> allowedOrigins = Arrays.asList("http://localhost:5173", "http://localhost:3000");
    private List<String> allowedMethods = Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH");
    private List<String> allowedHeaders = Arrays.asList("*");
    private List<String> exposedHeaders = Arrays.asList("X-RateLimit-Limit", "X-RateLimit-Remaining", "X-RateLimit-Reset-After", "X-Request-Id");
    private boolean allowCredentials = true;
    private long maxAge = 3600;

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public List<String> getAllowedOrigins() { return allowedOrigins; }
    public void setAllowedOrigins(List<String> allowedOrigins) { this.allowedOrigins = allowedOrigins; }
    public List<String> getAllowedMethods() { return allowedMethods; }
    public void setAllowedMethods(List<String> allowedMethods) { this.allowedMethods = allowedMethods; }
    public List<String> getAllowedHeaders() { return allowedHeaders; }
    public void setAllowedHeaders(List<String> allowedHeaders) { this.allowedHeaders = allowedHeaders; }
    public List<String> getExposedHeaders() { return exposedHeaders; }
    public void setExposedHeaders(List<String> exposedHeaders) { this.exposedHeaders = exposedHeaders; }
    public boolean isAllowCredentials() { return allowCredentials; }
    public void setAllowCredentials(boolean allowCredentials) { this.allowCredentials = allowCredentials; }
    public long getMaxAge() { return maxAge; }
    public void setMaxAge(long maxAge) { this.maxAge = maxAge; }
}
