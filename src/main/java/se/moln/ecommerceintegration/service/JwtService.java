package se.moln.ecommerceintegration.service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Component
public class JwtService {
    private final SecretKey key;
    private final String issuer;
    private final int accessMinutes;

    public JwtService(@Value("${jwt.secret}") String secret,
                      @Value("${jwt.issuer:user-service}") String issuer,
                      @Value("${jwt.access-token-minutes:30}") int accessMinutes) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
        this.issuer = issuer;
        this.accessMinutes = accessMinutes;
    }

    public String createAccessToken(UUID userId, String email, String role) {
        Instant now = Instant.now();
        return Jwts.builder()
                .setSubject(email).setIssuer(issuer)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(accessMinutes * 60L)))
                .addClaims(Map.of("uid", userId.toString(), "role", role))
                .signWith(key, SignatureAlgorithm.HS256).compact();
    }

    public Jws<Claims> parse(String token) {
        return Jwts.parserBuilder().setSigningKey(key).requireIssuer(issuer).build().parseClaimsJws(token);
    }

    /**
     * Validate token signature, issuer and expiration.
     */
    public boolean isTokenValid(String token) {
        try {
            if (token == null || token.isBlank()) return false;
            Jws<Claims> jws = parse(token);
            Date exp = jws.getBody().getExpiration();
            return exp != null && exp.after(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Extracts the email stored in the JWT subject ("sub").
     */
    public String extractEmail(String token) {
        return parse(token).getBody().getSubject();
    }
}