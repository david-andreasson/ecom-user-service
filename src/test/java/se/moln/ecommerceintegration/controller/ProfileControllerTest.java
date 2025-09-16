package se.moln.ecommerceintegration.controller;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import se.moln.ecommerceintegration.dto.UserProfileResponse;
import se.moln.ecommerceintegration.model.User;
import se.moln.ecommerceintegration.repository.UserRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class ProfileControllerTest {

    @Test
    void me_returnsProfile_forUserDetailsPrincipal() {
        UserRepository repo1 = Mockito.mock(UserRepository.class);
        UserRepository repo2 = Mockito.mock(UserRepository.class);
        ProfileController controller = new ProfileController(repo1, repo2);

        var ud = org.springframework.security.core.userdetails.User
                .withUsername("user@example.com").password("x").roles("USER").build();

        User entity = User.newUser("user@example.com", "HASH", "Anna", "Andersson");
        when(repo2.findByEmail("user@example.com")).thenReturn(Optional.of(entity));

        UserProfileResponse resp = controller.me(ud);
        assertThat(resp.email()).isEqualTo("user@example.com");
        assertThat(resp.firstName()).isEqualTo("Anna");
        assertThat(resp.role()).isEqualTo("USER");
        assertThat(resp.isActive()).isTrue();
    }

    @Test
    void me_unsupportedPrincipal_throws() {
        UserRepository repo1 = Mockito.mock(UserRepository.class);
        UserRepository repo2 = Mockito.mock(UserRepository.class);
        ProfileController controller = new ProfileController(repo1, repo2);

        assertThrows(IllegalStateException.class, () -> controller.me(new Object()));
    }
}
