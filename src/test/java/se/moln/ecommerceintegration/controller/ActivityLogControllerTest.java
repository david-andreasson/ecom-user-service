package se.moln.ecommerceintegration.controller;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import se.moln.ecommerceintegration.model.ActivityLog;
import se.moln.ecommerceintegration.repository.ActivityLogRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ActivityLogControllerTest {

    @Test
    void myHistory_withUserDetailsPrincipal_returnsLogs() {
        ActivityLogRepository repo = mock(ActivityLogRepository.class);
        ActivityLogController controller = new ActivityLogController(repo);
        var ud = User.withUsername("user@example.com").password("x").roles("USER").build();
        var log = ActivityLog.of("AUTH_LOGIN", ud.getUsername(), "POST", "/auth/login", 200, "127.0.0.1", "UA", 10);
        when(repo.findTop50ByUserEmailOrderByCreatedAtDesc(anyString())).thenReturn(List.of(log));

        var result = controller.myHistory(ud);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAction()).isEqualTo("AUTH_LOGIN");
        assertThat(result.get(0).getUserEmail()).isEqualTo("user@example.com");
    }

    @Test
    void myHistory_withStringPrincipal_returnsLogs() {
        ActivityLogRepository repo = mock(ActivityLogRepository.class);
        ActivityLogController controller = new ActivityLogController(repo);
        String email = "user2@example.com";
        var log = ActivityLog.of("ORDER_CREATE", email, "POST", "/orders", 201, "127.0.0.1", "UA", 20);
        when(repo.findTop50ByUserEmailOrderByCreatedAtDesc(anyString())).thenReturn(List.of(log));

        var result = controller.myHistory(email);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAction()).isEqualTo("ORDER_CREATE");
        assertThat(result.get(0).getUserEmail()).isEqualTo(email);
    }

    @Test
    void myHistory_withUnsupportedPrincipal_throws() {
        ActivityLogRepository repo = mock(ActivityLogRepository.class);
        ActivityLogController controller = new ActivityLogController(repo);
        assertThrows(IllegalStateException.class, () -> controller.myHistory(new Object()));
    }
}
