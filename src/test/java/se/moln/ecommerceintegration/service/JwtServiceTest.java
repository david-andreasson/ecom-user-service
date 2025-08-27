package se.moln.ecommerceintegration.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    @Test
    void createAndParseToken_roundtrip_ok() {
        String secret = "super-secret-key-that-is-long-enough-32-bytes-minimum!";
        JwtService jwt = new JwtService(secret, "test-issuer", 5);

        UUID uid = UUID.randomUUID();
        String token = jwt.createAccessToken(uid, "user@example.com", "USER");

        Jws<Claims> parsed = jwt.parse(token);
        Claims c = parsed.getBody();

        assertThat(c.getSubject()).isEqualTo("user@example.com");
        assertThat(c.getIssuer()).isEqualTo("test-issuer");
        assertThat(c.get("uid")).isEqualTo(uid.toString());
        assertThat(c.get("role")).isEqualTo("USER");
    }
}