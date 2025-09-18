package se.moln.ecommerceintegration.utils;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PrincipalUtilsTest {

    @Test
    void extractEmail_fromUserDetails() {
        var ud = User.withUsername("user@example.com").password("x").roles("USER").build();
        assertThat(PrincipalUtils.extractEmail(ud)).isEqualTo("user@example.com");
    }

    @Test
    void extractEmail_fromString() {
        assertThat(PrincipalUtils.extractEmail("someone@example.com")).isEqualTo("someone@example.com");
    }

    @Test
    void extractEmail_unsupported_throws() {
        assertThrows(IllegalStateException.class, () -> PrincipalUtils.extractEmail(new Object()));
    }
}
