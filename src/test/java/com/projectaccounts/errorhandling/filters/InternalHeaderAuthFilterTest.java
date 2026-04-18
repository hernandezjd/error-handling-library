package com.projectaccounts.errorhandling.filters;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.projectaccounts.errorhandling.security.InternalUserAuthentication;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;

class InternalHeaderAuthFilterTest {

    private InternalHeaderAuthFilter filter;

    @BeforeEach
    void setUp() {
        filter = new InternalHeaderAuthFilter(new ObjectMapper());
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldSetAuthentication_whenXUserIdHeaderPresent() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/test");
        request.addHeader("X-User-Id", "user-123");
        request.addHeader("X-User-Name", "johndoe");
        request.addHeader("X-User-Workspaces", "[\"ws-1\",\"ws-2\"]");
        request.addHeader("X-User-Global-Actions", "[\"manage_users\"]");
        request.addHeader("X-User-Workspace-Actions", "{\"ws-1\":[\"create_account\"]}");

        filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());

        var auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isInstanceOf(InternalUserAuthentication.class);
        InternalUserAuthentication internal = (InternalUserAuthentication) auth;
        assertThat(internal.getUserId()).isEqualTo("user-123");
        assertThat(internal.getUserName()).isEqualTo("johndoe");
        assertThat(internal.getWorkspaces()).containsExactly("ws-1", "ws-2");
        assertThat(internal.getGlobalActions()).containsExactly("manage_users");
        assertThat(internal.getWorkspaceActions()).containsKey("ws-1");
        assertThat(internal.isAuthenticated()).isTrue();
    }

    @Test
    void shouldNotSetAuthentication_whenXUserIdHeaderAbsent() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/test");

        filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void shouldHandleMissingOptionalHeaders_gracefully() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/test");
        request.addHeader("X-User-Id", "user-456");

        filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());

        var auth = (InternalUserAuthentication) SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth.getUserId()).isEqualTo("user-456");
        assertThat(auth.getUserName()).isNull();
        assertThat(auth.getWorkspaces()).isEmpty();
        assertThat(auth.getGlobalActions()).isEmpty();
        assertThat(auth.getWorkspaceActions()).isEmpty();
    }

    @Test
    void shouldHandleMalformedJsonHeaders_gracefully() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/test");
        request.addHeader("X-User-Id", "user-789");
        request.addHeader("X-User-Workspaces", "not-valid-json");
        request.addHeader("X-User-Workspace-Actions", "{broken}");

        filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());

        var auth = (InternalUserAuthentication) SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth.getUserId()).isEqualTo("user-789");
        assertThat(auth.getWorkspaces()).isEmpty();
        assertThat(auth.getWorkspaceActions()).isEmpty();
    }

    @Test
    void shouldContinueFilterChain_regardlessOfHeaders() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/test");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(chain.getRequest()).isNotNull();
    }
}
