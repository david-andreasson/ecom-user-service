package se.moln.ecommerceintegration.utils;

import org.springframework.security.core.userdetails.UserDetails;

public final class PrincipalUtils {
    private PrincipalUtils() {}

    // Extract email/username from Spring Security principal
    public static String extractEmail(Object principal) {
        if (principal instanceof UserDetails ud) return ud.getUsername(); // username = email
        if (principal instanceof String s) return s;                       // sometimes set as raw email
        throw new IllegalStateException("Unsupported principal type: " + principal);
    }
}
