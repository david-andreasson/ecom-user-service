package se.moln.ecommerceintegration.model;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.util.UUID;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
        name = "activity_logs",
        indexes = {
                @Index(name = "ix_activity_logs_user_ts", columnList = "user_email, created_at")
        }
)
public class ActivityLog {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(nullable = false, length = 32)
    private String action;

    @Column(name = "user_email", length = 255)
    private String userEmail;

    @Column(length = 10)
    private String method;

    @Column(length = 512)
    private String path;

    private int status;

    @Column(length = 64)
    private String ip;

    @Column(name = "user_agent", length = 256)
    private String userAgent;

    @Column(name = "duration_ms")
    private long durationMs;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        if (id == null) id = UUID.randomUUID();
    }

    public static ActivityLog of(
            String action, String userEmail, String method, String path,
            int status, String ip, String userAgent, long durationMs
    ) {
        return ActivityLog.builder()
                .action(action)
                .userEmail(userEmail)
                .method(method)
                .path(path)
                .status(status)
                .ip(ip)
                .userAgent(userAgent)
                .durationMs(durationMs)
                .build();
    }
}
