package se.moln.ecommerceintegration.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import se.moln.ecommerceintegration.model.User;
import se.moln.ecommerceintegration.repository.UserRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class UserServiceUpdateSettingsTest {

    private UserRepository users;
    private UserService service;

    @BeforeEach
    void setup() {
        users = Mockito.mock(UserRepository.class);
        // PasswordEncoder not used in updateSettings path; pass a dummy mock
        var encoder = Mockito.mock(org.springframework.security.crypto.password.PasswordEncoder.class);
        service = new UserService(users, encoder);
    }

    @Test
    void updateSettings_updatesFirstAndLastName_whenProvided() {
        User u = User.newUser("user@example.com", "H", "OldF", "OldL");
        when(users.findByEmail("user@example.com")).thenReturn(Optional.of(u));
        when(users.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User res = service.updateSettings("user@example.com", "NewF", "NewL");

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(users).save(captor.capture());
        User saved = captor.getValue();

        assertThat(saved.getFirstName()).isEqualTo("NewF");
        assertThat(saved.getLastName()).isEqualTo("NewL");
        assertThat(res.getFirstName()).isEqualTo("NewF");
    }

    @Test
    void updateSettings_nulls_doNotChangeFields() {
        User u = User.newUser("user@example.com", "H", "OldF", "OldL");
        when(users.findByEmail("user@example.com")).thenReturn(Optional.of(u));
        when(users.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User res = service.updateSettings("user@example.com", null, null);
        assertThat(res.getFirstName()).isEqualTo("OldF");
        assertThat(res.getLastName()).isEqualTo("OldL");
    }

    @Test
    void updateSettings_userNotFound_throws() {
        when(users.findByEmail("missing@example.com")).thenReturn(Optional.empty());
        assertThrows(IllegalStateException.class, () -> service.updateSettings("missing@example.com", "A", "B"));
    }
}
