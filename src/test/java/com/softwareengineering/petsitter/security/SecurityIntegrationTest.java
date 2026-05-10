package com.softwareengineering.petsitter.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.web.FilterChainProxy;

@SpringBootTest(properties = {
        "spring.docker.compose.enabled=false",
        "petsitter.security.demo.enabled=true",
        "petsitter.security.demo.username=localuser",
        "petsitter.security.demo.password=localpass",
        "petsitter.security.demo.role=USER"
})
class SecurityIntegrationTest {

    @Autowired
    private FilterChainProxy springSecurityFilterChain;

    @Test
    void startPageIsPublic() throws Exception {
        MockHttpServletResponse response = execute(request("GET", "/"));

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getRedirectedUrl()).isNull();
    }

    @Test
    void protectedRouteRedirectsAnonymousUserToLogin() throws Exception {
        MockHttpServletResponse response = execute(request("GET", "/profile"));

        assertThat(response.getStatus()).isEqualTo(302);
        assertThat(response.getRedirectedUrl()).isEqualTo("/login");
    }

    @Test
    void demoLoginCreatesAuthenticatedSession() throws Exception {
        MockHttpServletRequest request = loginRequest();

        MockHttpServletResponse response = execute(request);

        assertThat(response.getStatus()).isEqualTo(302);
        assertThat(response.getRedirectedUrl()).isEqualTo("/");
        assertThat(request.getSession(false)).isInstanceOf(MockHttpSession.class);
    }

    @Test
    void logoutInvalidatesSessionAndRedirectsToLogin() throws Exception {
        MockHttpServletRequest loginRequest = loginRequest();
        execute(loginRequest);
        MockHttpSession session = (MockHttpSession) loginRequest.getSession(false);

        MockHttpServletRequest logoutRequest = request("GET", "/logout");
        logoutRequest.setSession(session);
        MockHttpServletResponse response = execute(logoutRequest);

        assertThat(response.getStatus()).isEqualTo(302);
        assertThat(response.getRedirectedUrl()).isEqualTo("/login?logout");
        assertThat(session.isInvalid()).isTrue();
    }

    private MockHttpServletRequest loginRequest() {
        MockHttpServletRequest request = request("POST", "/login");
        request.addParameter("username", "localuser");
        request.addParameter("password", "localpass");
        return request;
    }

    private MockHttpServletRequest request(String method, String path) {
        MockHttpServletRequest request = new MockHttpServletRequest(method, path);
        request.setServletPath(path);
        return request;
    }

    private MockHttpServletResponse execute(MockHttpServletRequest request) throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        springSecurityFilterChain.doFilter(request, response, new MockFilterChain());
        return response;
    }
}
