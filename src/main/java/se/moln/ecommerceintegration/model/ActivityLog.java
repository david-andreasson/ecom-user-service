package se.moln.ecommerceintegration.model;
import jakarta.persistence.*;
import java.util.UUID;
import java.time.Instant;

@Entity
@Table(name = "activity_logs", indexes = {
        @Index(name = "ix_activity_logs_user_ts", columnList = "userEmail,createdAt DESC")
})
public class ActivityLog {
    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(nullable = false, length = 32)
    private String action;

    @Column(length = 255)
    private String userEmail;

    @Column(length = 10)
    private String method;

    @Column(length = 512)
    private String path;

    private int status;
    @Column(length = 64)  private String ip;
    @Column(length = 256) private String userAgent;

    private long durationMs;
    @Column(nullable = false) private Instant createdAt;

    @PrePersist void onCreate() {
        if (id == null) id = UUID.randomUUID();
        if (createdAt == null) createdAt = Instant.now();
    }

    // getters/setters
    public static ActivityLog of(String action, String userEmail, String method, String path, int status, String ip, String userAgent, long durationMs) {
        ActivityLog activityLog = new ActivityLog();
        activityLog.action = action;
        activityLog.userEmail = userEmail;
        activityLog.method = method;
        activityLog.path = path;
        activityLog.status = status;
        activityLog.ip = ip;
        activityLog.userAgent = userAgent;
        activityLog.durationMs = durationMs;
        return activityLog;

    }
}
