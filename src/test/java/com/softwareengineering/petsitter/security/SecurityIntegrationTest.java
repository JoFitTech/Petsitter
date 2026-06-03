package com.softwareengineering.petsitter.security;

import static org.assertj.core.api.Assertions.assertThat;

import com.softwareengineering.petsitter.user.repository.UserRepository;
import java.lang.reflect.Proxy;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;

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
    void profileImageRouteIsPublic() throws Exception {
        MockHttpServletResponse response = execute(request(
                "GET",
                "/media/images/11111111-1111-1111-1111-111111111111/avatar"));

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
        assertThat(request.getSession(false)
                .getAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY))
                .isNotNull();
    }

    @Test
    void logoutInvalidatesSessionAndRedirectsToLogin() throws Exception {
        MockHttpServletRequest loginRequest = loginRequest();
        execute(loginRequest);
        MockHttpSession session = (MockHttpSession) loginRequest.getSession(false);

        MockHttpServletRequest logoutRequest = request("POST", "/logout");
        logoutRequest.setSession(session);
        MockHttpServletResponse response = execute(logoutRequest);

        assertThat(response.getStatus()).isEqualTo(302);
        assertThat(response.getRedirectedUrl()).isEqualTo("/?logout=true");
        assertThat(session.isInvalid()).isTrue();
    }

    private MockHttpServletRequest loginRequest() {
        MockHttpServletRequest request = request("POST", "/login");
        request.addParameter("email", "localuser");
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

    @TestConfiguration
    static class UserRepositoryTestConfig {

        @Bean
        @Primary
        UserRepository userRepositoryTestDouble() {
            return (UserRepository) Proxy.newProxyInstance(
                    UserRepository.class.getClassLoader(),
                    new Class<?>[] {UserRepository.class},
                    (proxy, method, args) -> {
                        if ("findByEmail".equals(method.getName())) {
                            return Optional.empty();
                        }
                        if ("equals".equals(method.getName())) {
                            return proxy == args[0];
                        }
                        if ("hashCode".equals(method.getName())) {
                            return System.identityHashCode(proxy);
                        }
                        if ("toString".equals(method.getName())) {
                            return "SecurityIntegrationUserRepositoryTestDouble";
                        }
                        throw new UnsupportedOperationException(
                                "Unsupported repository method in security integration test: "
                                        + method.getName());
                    }
            );
        }
    }
}
