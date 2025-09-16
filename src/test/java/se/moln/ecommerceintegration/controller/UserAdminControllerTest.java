package se.moln.ecommerceintegration.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.security.crypto.password.PasswordEncoder;
import se.moln.ecommerceintegration.model.Role;
import se.moln.ecommerceintegration.model.User;
import se.moln.ecommerceintegration.repository.UserRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class UserAdminControllerTest {

    private UserRepository users;
    private PasswordEncoder encoder;
    private UserAdminController controller;

    @BeforeEach
    void setup() {
        users = Mockito.mock(UserRepository.class);
        encoder = Mockito.mock(PasswordEncoder.class);
        controller = new UserAdminController(users, encoder);
    }

    @Test
    void list_returnsAll() {
        when(users.findAll()).thenReturn(List.of(
                User.newUser("a@e.com", "H", "A", "A"),
                User.newUser("b@e.com", "H", "B", "B")
        ));

        var result = controller.list();
        assertThat(result).hasSize(2);
        assertThat(result.get(0).email()).isEqualTo("a@e.com");
    }

    @Test
    void get_ok_returnsDto() {
        UUID id = UUID.randomUUID();
        User u = User.newUser("u@e.com", "H", "F", "L");
        when(users.findById(id)).thenReturn(Optional.of(u));

        var dto = controller.get(id);
        assertThat(dto.email()).isEqualTo("u@e.com");
    }

    @Test
    void get_notFound_throws404() {
        UUID id = UUID.randomUUID();
        when(users.findById(id)).thenReturn(Optional.empty());
        assertThrows(org.springframework.web.server.ResponseStatusException.class, () -> controller.get(id));
    }

    @Test
    void create_success_encodesPassword_andSetsDefaults() {
        when(encoder.encode("Pwd123!"))
                .thenReturn("ENC");
        when(users.existsByEmail("new@e.com")).thenReturn(false);
        when(users.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        var req = new UserAdminController.CreateUserRequest("new@e.com", "Pwd123!", "NewF", "NewL", Role.ADMIN);
        var dto = controller.create(req);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(users).save(captor.capture());
        User saved = captor.getValue();
        assertThat(saved.getEmail()).isEqualTo("new@e.com");
        assertThat(saved.getPasswordHash()).isEqualTo("ENC");
        assertThat(saved.getRole()).isEqualTo(Role.ADMIN);
        assertThat(dto.email()).isEqualTo("new@e.com");
    }

    @Test
    void create_missingEmail_badRequest() {
        var req = new UserAdminController.CreateUserRequest(" ", "Pwd123!", "F", "L", Role.USER);
        assertThrows(org.springframework.web.server.ResponseStatusException.class, () -> controller.create(req));
    }

    @Test
    void create_emailExists_conflict() {
        when(users.existsByEmail("dup@e.com")).thenReturn(true);
        var req = new UserAdminController.CreateUserRequest("dup@e.com", "Pwd123!", "F", "L", Role.USER);
        assertThrows(org.springframework.web.server.ResponseStatusException.class, () -> controller.create(req));
        verify(users, never()).save(any());
    }

    @Test
    void update_updatesProvidedFieldsOnly() {
        UUID id = UUID.randomUUID();
        User u = User.newUser("u@e.com", "H", "OldF", "OldL");
        when(users.findById(id)).thenReturn(Optional.of(u));
        when(users.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        var req = new UserAdminController.UpdateUserRequest("NewF", null, false);
        var dto = controller.update(id, req);

        assertThat(dto.firstName()).isEqualTo("NewF");
        assertThat(dto.lastName()).isEqualTo("OldL");
        assertThat(dto.isActive()).isFalse();
    }

    @Test
    void setRole_requiresRole_andSetsIt() {
        UUID id = UUID.randomUUID();
        User u = User.newUser("u@e.com", "H", "F", "L");
        when(users.findById(id)).thenReturn(Optional.of(u));
        when(users.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        // missing role -> bad request
        var bad = new UserAdminController.UpdateRoleRequest(null);
        assertThrows(org.springframework.web.server.ResponseStatusException.class, () -> controller.setRole(id, bad));

        var ok = new UserAdminController.UpdateRoleRequest(Role.ADMIN);
        var dto = controller.setRole(id, ok);
        assertThat(dto.role()).isEqualTo(Role.ADMIN);
    }

    @Test
    void delete_notFound_throws404() {
        UUID id = UUID.randomUUID();
        when(users.existsById(id)).thenReturn(false);
        assertThrows(org.springframework.web.server.ResponseStatusException.class, () -> controller.delete(id));
    }

    @Test
    void delete_ok_deletes() {
        UUID id = UUID.randomUUID();
        when(users.existsById(id)).thenReturn(true);
        controller.delete(id);
        verify(users).deleteById(id);
    }
}
