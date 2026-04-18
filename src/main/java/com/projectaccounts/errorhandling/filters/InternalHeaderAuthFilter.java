package com.projectaccounts.errorhandling.filters;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.projectaccounts.errorhandling.security.InternalUserAuthentication;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class InternalHeaderAuthFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper;

    public InternalHeaderAuthFilter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String userId = request.getHeader("X-User-Id");

        if (userId != null && !userId.isBlank()) {
            String userName = request.getHeader("X-User-Name");
            List<String> workspaces = parseList(request.getHeader("X-User-Workspaces"));
            List<String> globalActions = parseList(request.getHeader("X-User-Global-Actions"));
            Map<String, List<String>> workspaceActions = parseMap(request.getHeader("X-User-Workspace-Actions"));

            InternalUserAuthentication auth = new InternalUserAuthentication(
                    userId, userName, workspaces, globalActions, workspaceActions);
            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        filterChain.doFilter(request, response);
    }

    private List<String> parseList(String json) {
        if (json == null || json.isBlank()) return List.of();
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (Exception e) {
            return List.of();
        }
    }

    private Map<String, List<String>> parseMap(String json) {
        if (json == null || json.isBlank()) return Map.of();
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (Exception e) {
            return Map.of();
        }
    }
}
