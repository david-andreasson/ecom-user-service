package se.moln.ecommerceintegration.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.security.crypto.password.PasswordEncoder;
import se.moln.ecommerceintegration.model.User;
import se.moln.ecommerceintegration.repository.UserRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class UserServiceTest {

    private UserRepository users;
    private PasswordEncoder encoder;
    private UserService service;

    @BeforeEach
    void setup() {
        users = Mockito.mock(UserRepository.class);
        encoder = Mockito.mock(PasswordEncoder.class);
        service = new UserService(users, encoder);
    }

    @Test
    void register_success_encodesPassword_andSaves() {
        when(users.existsByEmail("user@example.com")).thenReturn(false);
        when(encoder.encode("Password123!")).thenReturn("ENC_HASH");
        when(users.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User u = service.register("user@example.com", "Password123!", "King", "Kong");

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(users).save(captor.capture());
        User saved = captor.getValue();

        assertThat(saved.getEmail()).isEqualTo("user@example.com");
        assertThat(saved.getPasswordHash()).isEqualTo("ENC_HASH");
        assertThat(u.getFirstName()).isEqualTo("King");
        assertThat(u.getLastName()).isEqualTo("Kong");
    }

    @Test
    void register_whenEmailExists_throwsIllegalState() {
        when(users.existsByEmail("user@example.com")).thenReturn(true);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                service.register("user@example.com", "Password123!", "D", "A"));

        assertThat(ex.getMessage()).contains("Email is already registered");
        verify(users, never()).save(any());
    }
}