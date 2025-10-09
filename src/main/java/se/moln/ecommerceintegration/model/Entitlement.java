package se.moln.ecommerceintegration.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "entitlements")
public class Entitlement {
    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false, length = 100)
    private String sku;

    @Column(nullable = false)
    private int remaining = 0; // for one-off packs

    private OffsetDateTime expiresAt; // for subscriptions (optional)

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    public Entitlement() {}

    public static Entitlement of(UUID userId, String sku, int remaining, OffsetDateTime expiresAt) {
        Entitlement e = new Entitlement();
        e.id = UUID.randomUUID();
        e.userId = userId;
        e.sku = sku;
        e.remaining = Math.max(remaining, 0);
        e.expiresAt = expiresAt;
        return e;
        }

    @PrePersist
    void onCreate() {
        var now = Instant.now();
        createdAt = now;
        updatedAt = now;
        if (id == null) id = UUID.randomUUID();
    }

    @PreUpdate
    void onUpdate() { updatedAt = Instant.now(); }

    // getters/setters
    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public String getSku() { return sku; }
    public int getRemaining() { return remaining; }
    public OffsetDateTime getExpiresAt() { return expiresAt; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    public void setRemaining(int remaining) { this.remaining = remaining; }
}
