package com.projectaccounts.errorhandling.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.projectaccounts.errorhandling.filters.RequestIdFilter;
import com.projectaccounts.errorhandling.response.ErrorCode;
import com.projectaccounts.errorhandling.response.ErrorResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;

/**
 * Custom AccessDeniedHandler for Spring Security that returns structured error responses
 * with request IDs, ensuring observability for 403 Forbidden errors.
 *
 * This handler is necessary because Spring Security's ExceptionTranslationFilter
 * catches AccessDeniedException before it can reach the GlobalExceptionHandler.
 */
public class CustomAccessDeniedHandler implements AccessDeniedHandler {
    private static final Logger logger = LoggerFactory.getLogger(CustomAccessDeniedHandler.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                      AccessDeniedException accessDeniedException) throws IOException, ServletException {
        String requestId = RequestIdFilter.getCurrentRequestId();
        long timestamp = System.currentTimeMillis();

        ErrorResponse errorResponse = new ErrorResponse(
            requestId,
            ErrorCode.FORBIDDEN,
            "You do not have permission to perform this action",
            timestamp
        );

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setHeader(RequestIdFilter.REQUEST_ID_HEADER, requestId);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        try {
            response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
        } catch (IOException e) {
            logger.error("Error writing AccessDeniedException response", e);
        }
    }
}
