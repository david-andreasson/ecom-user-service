package se.moln.ecommerceintegration.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
@Order(Integer.MAX_VALUE)
public class AuditLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(AuditLoggingFilter.class);
    private final se.moln.ecommerceintegration.service.AuditLogService audit;

    public AuditLoggingFilter(se.moln.ecommerceintegration.service.AuditLogService audit) {
        this.audit = audit;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {

        String reqId = UUID.randomUUID().toString().substring(0, 8);
        MDC.put("reqId", reqId);
        MDC.put("ip", clientIp(req));

        long started = System.currentTimeMillis();
        try {
            chain.doFilter(req, res);
        } finally {
            String user = resolveUserEmail();
            MDC.put("userEmail", user != null ? user : "anon");

            int status = res.getStatus();
            long took = System.currentTimeMillis() - started;
            String action = actionFor(req);

            String msg = String.format("%s %s status=%d took=%dms ua=\"%s\"",
                    req.getMethod(), req.getRequestURI(), status, took, userAgent(req));

            if (status >= 400) log.warn("{} - {}", action, msg);
            else               log.info("{} - {}", action, msg);

            // persist if enabled
            audit.save(action, user, req.getMethod(), req.getRequestURI(), status, clientIp(req), userAgent(req), took);

            MDC.clear();
        }
    }

    private String actionFor(HttpServletRequest req) {
        String p = req.getRequestURI();
        String m = req.getMethod();
        if (p.startsWith("/auth/login"))    return "AUTH_LOGIN";
        if (p.startsWith("/auth/register")) return "AUTH_REGISTER";
        if (p.equals("/me"))                return "PROFILE_VIEW";
        if (p.startsWith("/users") && ("PUT".equals(m) || "PATCH".equals(m)))
            return "USER_UPDATE";
        return "REQUEST";
    }
    private String resolveUserEmail() {
        var a = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        return (a != null) ? a.getName() : null;
    }
    private String clientIp(HttpServletRequest req) {
        String xff = req.getHeader("X-Forwarded-For");
        return (xff != null && !xff.isBlank()) ? xff.split(",")[0].trim() : req.getRemoteAddr();
    }
    private String userAgent(HttpServletRequest req) {
        String ua = req.getHeader("User-Agent");
        return ua != null ? ua : "-";
    }
}
