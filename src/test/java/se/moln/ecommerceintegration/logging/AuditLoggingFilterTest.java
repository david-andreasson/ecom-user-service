package se.moln.ecommerceintegration.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import se.moln.ecommerceintegration.service.AuditLogService;

import java.io.IOException;

import static org.mockito.Mockito.*;

class AuditLoggingFilterTest {

    private AuditLogService audit;
    private AuditLoggingFilter filter;

    @BeforeEach
    void setup() {
        audit = mock(AuditLogService.class);
        filter = new AuditLoggingFilter(audit);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void savesAudit_withResolvedUser_andActionMappings_okStatus() throws ServletException, IOException {
        // authenticated user
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("user@example.com", "N/A")
        );

        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/me");
        req.addHeader("X-Forwarded-For", "203.0.113.10, 70.41.3.18");
        req.addHeader("User-Agent", "JUnit/5");
        MockHttpServletResponse res = new MockHttpServletResponse();
        res.setStatus(200);

        FilterChain chain = (r, s) -> { /* nothing */ };

        filter.doFilter(req, res, chain);

        // verify audit persisted
        verify(audit, times(1)).save(eq("PROFILE_VIEW"), eq("user@example.com"), eq("GET"), eq("/me"), eq(200), eq("203.0.113.10"), eq("JUnit/5"), anyLong());
    }

    @Test
    void savesAudit_anonUser_warnWhenStatus400plus() throws ServletException, IOException {
        // no auth in security context -> anon
        MockHttpServletRequest req = new MockHttpServletRequest("POST", "/auth/login");
        req.setRemoteAddr("127.0.0.1");
        MockHttpServletResponse res = new MockHttpServletResponse();
        res.setStatus(401);

        FilterChain chain = (r, s) -> { /* nothing */ };

        filter.doFilter(req, res, chain);

        verify(audit).save(eq("AUTH_LOGIN"), eq(null), eq("POST"), eq("/auth/login"), eq(401), eq("127.0.0.1"), eq("-"), anyLong());
    }

    @Test
    void actionMapping_fallsBackTo_REQUEST() throws ServletException, IOException {
        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/other");
        MockHttpServletResponse res = new MockHttpServletResponse();

        filter.doFilter(req, res, (r, s) -> {});

        verify(audit).save(eq("REQUEST"), any(), eq("GET"), eq("/other"), anyInt(), anyString(), anyString(), anyLong());
    }

    @Test
    void actionMapping_userUpdate_forUsersPut() throws ServletException, IOException {
        MockHttpServletRequest req = new MockHttpServletRequest("PUT", "/users/123");
        MockHttpServletResponse res = new MockHttpServletResponse();

        filter.doFilter(req, res, (r, s) -> {});

        verify(audit).save(eq("USER_UPDATE"), any(), eq("PUT"), eq("/users/123"), anyInt(), anyString(), anyString(), anyLong());
    }
}
