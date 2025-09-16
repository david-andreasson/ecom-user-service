package se.moln.ecommerceintegration.service;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import se.moln.ecommerceintegration.model.ActivityLog;
import se.moln.ecommerceintegration.repository.ActivityLogRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class AuditLogServiceTest {

    @Test
    void save_whenDisabled_doesNothing() {
        ActivityLogRepository repo = mock(ActivityLogRepository.class);
        AuditLogService svc = new AuditLogService(repo, false);

        svc.save("ACTION", "user@example.com", "GET", "/x", 200, "127.0.0.1", "UA", 1);
        verify(repo, never()).save(any());
    }

    @Test
    void save_whenEnabled_persistsActivityLog() {
        ActivityLogRepository repo = mock(ActivityLogRepository.class);
        AuditLogService svc = new AuditLogService(repo, true);

        ArgumentCaptor<ActivityLog> captor = ArgumentCaptor.forClass(ActivityLog.class);
        svc.save("ACTION", "user@example.com", "GET", "/x", 200, "127.0.0.1", "UA", 42);

        verify(repo).save(captor.capture());
        ActivityLog saved = captor.getValue();
        assertThat(saved.getAction()).isEqualTo("ACTION");
        assertThat(saved.getUserEmail()).isEqualTo("user@example.com");
        assertThat(saved.getMethod()).isEqualTo("GET");
        assertThat(saved.getPath()).isEqualTo("/x");
        assertThat(saved.getStatus()).isEqualTo(200);
        assertThat(saved.getIp()).isEqualTo("127.0.0.1");
        assertThat(saved.getUserAgent()).isEqualTo("UA");
        assertThat(saved.getDurationMs()).isEqualTo(42);
    }
}
