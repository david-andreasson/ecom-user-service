package se.moln.ecommerceintegration.model;

import jakarta.persistence.*;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "users")
public class User {
    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Setter
    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Setter
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Setter
    @Column(length = 100) private String firstName;
    @Setter
    @Column(length = 100) private String lastName;

    @Setter
    @Column(nullable = false, length = 30) private String role = "USER";
    @Setter
    @Column(nullable = false) private Boolean isActive = true;
    @Column(nullable = false) private Instant createdAt;
    @Column(nullable = false) private Instant updatedAt;

    public User() {}

    public static User newUser(String email, String passwordHash, String firstName, String lastName) {
        User u = new User();
        u.id = UUID.randomUUID();
        u.email = email;
        u.passwordHash = passwordHash;
        u.firstName = firstName;
        u.lastName = lastName;
        return u;
    }

    @PrePersist void onCreate() { var now = Instant.now(); createdAt = now; updatedAt = now; if (id == null) id = UUID.randomUUID(); }
    @PreUpdate  void onUpdate() { updatedAt = Instant.now(); }

    // getters
    public UUID getId() { return id; } public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; } public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; } public String getRole() { return role; }
    public Boolean getIsActive() { return isActive; } public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}