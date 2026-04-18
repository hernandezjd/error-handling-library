package com.projectaccounts.errorhandling.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.AbstractAuthenticationToken;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Spring Security Authentication populated from trusted internal headers set by the gateway.
 * Used when requests arrive via the API gateway, which validates JWT and forwards claims
 * as X-User-* headers. Backend services trust these headers on the internal network.
 */
public class InternalUserAuthentication extends AbstractAuthenticationToken {

    private final String userId;
    private final String userName;
    private final List<String> workspaces;
    private final List<String> globalActions;
    private final Map<String, List<String>> workspaceActions;

    public InternalUserAuthentication(String userId, String userName,
                                      List<String> workspaces,
                                      List<String> globalActions,
                                      Map<String, List<String>> workspaceActions) {
        super(buildAuthorities(globalActions));
        this.userId = userId;
        this.userName = userName;
        this.workspaces = workspaces != null ? workspaces : List.of();
        this.globalActions = globalActions != null ? globalActions : List.of();
        this.workspaceActions = workspaceActions != null ? workspaceActions : Map.of();
        setAuthenticated(true);
    }

    private static Collection<GrantedAuthority> buildAuthorities(List<String> globalActions) {
        if (globalActions == null) return List.of();
        return globalActions.stream()
            .map(action -> (GrantedAuthority) new SimpleGrantedAuthority("ACTION_" + action.toUpperCase()))
            .collect(Collectors.toList());
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return userId;
    }

    @Override
    public String getName() {
        return userId;
    }

    public String getUserId() { return userId; }
    public String getUserName() { return userName; }
    public List<String> getWorkspaces() { return workspaces; }
    public List<String> getGlobalActions() { return globalActions; }
    public Map<String, List<String>> getWorkspaceActions() { return workspaceActions; }
}
