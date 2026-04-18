package com.projectaccounts.errorhandling.security;

import com.projectaccounts.errorhandling.exceptions.UnauthorizedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public abstract class AuthorizationSupport {

    protected List<String> getClaimAsList(String claimName) {
        Authentication auth = requireAuthentication();
        if (auth instanceof InternalUserAuthentication internal) {
            return switch (claimName) {
                case "workspaces" -> internal.getWorkspaces();
                case "global_actions" -> internal.getGlobalActions();
                default -> List.of();
            };
        }
        if (auth instanceof JwtAuthenticationToken jwtAuth) {
            List<String> values = jwtAuth.getToken().getClaimAsStringList(claimName);
            return values != null ? values : List.of();
        }
        throw new UnauthorizedException("No valid authentication present");
    }

    @SuppressWarnings("unchecked")
    protected Map<String, List<String>> getClaimAsMap(String claimName) {
        Authentication auth = requireAuthentication();
        if (auth instanceof InternalUserAuthentication internal) {
            return switch (claimName) {
                case "workspace_actions" -> internal.getWorkspaceActions();
                default -> Map.of();
            };
        }
        if (auth instanceof JwtAuthenticationToken jwtAuth) {
            Object claim = jwtAuth.getToken().getClaim(claimName);
            if (claim instanceof Map) {
                return (Map<String, List<String>>) claim;
            }
            return Map.of();
        }
        throw new UnauthorizedException("No valid authentication present");
    }

    protected UUID getUserId() {
        Authentication auth = requireAuthentication();
        if (auth instanceof InternalUserAuthentication internal) {
            return UUID.fromString(internal.getUserId());
        }
        if (auth instanceof JwtAuthenticationToken jwtAuth) {
            Object sub = jwtAuth.getToken().getClaim("sub");
            return UUID.fromString(sub.toString());
        }
        throw new UnauthorizedException("No valid authentication present");
    }

    protected String getUsername() {
        Authentication auth = requireAuthentication();
        if (auth instanceof InternalUserAuthentication internal) {
            return internal.getUserName() != null ? internal.getUserName() : "unknown";
        }
        if (auth instanceof JwtAuthenticationToken jwtAuth) {
            Object username = jwtAuth.getToken().getClaim("username");
            return username != null ? username.toString() : "unknown";
        }
        throw new UnauthorizedException("No valid authentication present");
    }

    private Authentication requireAuthentication() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new UnauthorizedException("No valid authentication present");
        }
        return auth;
    }
}
