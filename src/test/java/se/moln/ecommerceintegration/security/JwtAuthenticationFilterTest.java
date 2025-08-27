package se.moln.ecommerceintegration.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import se.moln.ecommerceintegration.service.JwtService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import java.io.IOException;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class JwtAuthenticationFilterTest {

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void setsSecurityContext_whenBearerTokenValid() throws ServletException, IOException {
        String secret = "super-secret-key-that-is-long-enough-32-bytes-minimum!";
        JwtService jwt = new JwtService(secret, "test-issuer", 5);

        String token = jwt.createAccessToken(UUID.randomUUID(), "user@example.com", "ADMIN");
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwt);

        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/any");
        req.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse res = new MockHttpServletResponse();

        FilterChain chain = (request, response) -> { /* no-op */ };

        filter.doFilter(req, res, chain);

        var auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNotNull();
        assertThat(auth.getName()).isEqualTo("user@example.com");
        assertThat(auth.getAuthorities()).extracting("authority").contains("ROLE_ADMIN");
    }
}