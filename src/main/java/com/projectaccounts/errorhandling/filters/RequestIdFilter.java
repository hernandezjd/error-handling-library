package com.projectaccounts.errorhandling.filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Servlet filter that generates a unique request ID for each request,
 * stores it in a ThreadLocal for access by exception handlers,
 * and adds it to the response header for client-side debugging.
 */
public class RequestIdFilter extends OncePerRequestFilter {
    public static final String REQUEST_ID_HEADER = "X-Request-Id";
    private static final ThreadLocal<String> requestIdHolder = new ThreadLocal<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String requestId = generateRequestId();
        requestIdHolder.set(requestId);

        try {
            // Add request ID to response header
            response.setHeader(REQUEST_ID_HEADER, requestId);
            filterChain.doFilter(request, response);
        } finally {
            requestIdHolder.remove();
        }
    }

    private String generateRequestId() {
        return UUID.randomUUID().toString();
    }

    /**
     * Retrieve the current request ID from ThreadLocal.
     * Returns a fallback ID if none exists (should not happen in normal operation).
     */
    public static String getCurrentRequestId() {
        String id = requestIdHolder.get();
        return id != null ? id : "unknown-" + UUID.randomUUID();
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        // Exempt health check endpoints
        return path.startsWith("/health") ||
               path.startsWith("/actuator") ||
               path.startsWith("/swagger") ||
               path.startsWith("/v3/api-docs") ||
               path.startsWith("/webjars");
    }
}
