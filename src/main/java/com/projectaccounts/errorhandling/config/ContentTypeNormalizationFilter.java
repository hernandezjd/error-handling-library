package com.projectaccounts.errorhandling.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;

/**
 * Normalizes Content-Type headers to remove charset parameters before Spring's content negotiation.
 *
 * Spring 6 strictly matches media types in endpoint @RequestMapping annotations.
 * When an endpoint declares consumes = { "application/json" }, Spring rejects
 * requests with Content-Type: application/json; charset=utf-8 because the charset
 * parameter doesn't match exactly.
 *
 * This filter removes charset (and other) parameters from Content-Type headers,
 * allowing Spring to match the base media type. Charset information is redundant
 * for JSON processing since UTF-8 is the standard.
 *
 * REF: FR-149
 */
@Component
public class ContentTypeNormalizationFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (request instanceof HttpServletRequest httpRequest) {
            chain.doFilter(new ContentTypeNormalizingRequestWrapper(httpRequest), response);
        } else {
            chain.doFilter(request, response);
        }
    }

    /**
     * HttpServletRequestWrapper that normalizes Content-Type headers by removing
     * parameters (e.g., charset, boundary).
     */
    private static class ContentTypeNormalizingRequestWrapper extends HttpServletRequestWrapper {

        private final String normalizedContentType;

        ContentTypeNormalizingRequestWrapper(HttpServletRequest request) {
            super(request);
            this.normalizedContentType = normalizeContentType(request.getContentType());
        }

        @Override
        public String getContentType() {
            return normalizedContentType;
        }

        @Override
        public Enumeration<String> getHeaders(String name) {
            if ("content-type".equalsIgnoreCase(name)) {
                String normalized = getContentType();
                if (normalized != null) {
                    return Collections.enumeration(Collections.singletonList(normalized));
                }
                return Collections.emptyEnumeration();
            }
            return super.getHeaders(name);
        }

        @Override
        public String getHeader(String name) {
            if ("content-type".equalsIgnoreCase(name)) {
                return getContentType();
            }
            return super.getHeader(name);
        }

        /**
         * Normalizes a Content-Type header by removing parameters.
         * For example: "application/json; charset=utf-8" -> "application/json"
         */
        private static String normalizeContentType(String contentType) {
            if (contentType == null || contentType.isEmpty()) {
                return contentType;
            }
            // Split by semicolon and take only the first part (the actual media type)
            int semicolonIndex = contentType.indexOf(';');
            if (semicolonIndex > 0) {
                return contentType.substring(0, semicolonIndex).trim();
            }
            return contentType;
        }
    }
}
