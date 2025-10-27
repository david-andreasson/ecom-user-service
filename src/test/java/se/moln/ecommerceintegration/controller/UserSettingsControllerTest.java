package se.moln.ecommerceintegration.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.core.userdetails.User;
import se.moln.ecommerceintegration.dto.UpdateSettingsRequest;
import se.moln.ecommerceintegration.service.UserService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class UserSettingsControllerTest {

    private UserService userService;
    private UserSettingsController controller;

    @BeforeEach
    void setup() {
        userService = Mockito.mock(UserService.class);
        controller = new UserSettingsController(userService);
    }

    @Test
    void updateSettings_trimsNames_andReturnsProfile() {
        var ud = User.withUsername("user@example.com").password("x").roles("USER").build();
        var req = new UpdateSettingsRequest("  Anna  ", "  Andersson  ");

        se.moln.ecommerceintegration.model.User saved = se.moln.ecommerceintegration.model.User.newUser("user@example.com", "HASH", "Anna", "Andersson");
        when(userService.updateSettings(anyString(), anyString(), anyString())).thenReturn(saved);
        var resp = controller.updateSettings(ud, req);
        assertThat(resp.email()).isEqualTo("user@example.com");
        assertThat(resp.firstName()).isEqualTo("Anna");
        assertThat(resp.lastName()).isEqualTo("Andersson");
        assertThat(resp.role()).isEqualTo("USER");
        assertThat(resp.isActive()).isTrue();
    }

    @Test
    void updateSettings_blankFirstName_throwsResponseStatusException() {
        var ud = User.withUsername("user@example.com").password("x").roles("USER").build();
        var req = new UpdateSettingsRequest("   ", null);

        assertThrows(org.springframework.web.server.ResponseStatusException.class,
                () -> controller.updateSettings(ud, req));
    }

    @Test
    void updateSettings_blankLastName_throwsResponseStatusException() {
        var ud = User.withUsername("user@example.com").password("x").roles("USER").build();
        var req = new UpdateSettingsRequest(null, "   ");

        assertThrows(org.springframework.web.server.ResponseStatusException.class,
                () -> controller.updateSettings(ud, req));
    }
}
