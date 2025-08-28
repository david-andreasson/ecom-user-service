package se.moln.ecommerceintegration.model;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static se.moln.ecommerceintegration.model.Role.USER;
import static se.moln.ecommerceintegration.model.Role.ADMIN;

class UserTest {

    @Test
    void newUser_setsFields_andDefaults() {
        User u = User.newUser("user@example.com", "HASH", "David", "Andreasson");

        assertThat(u.getId()).isNotNull();
        assertThat(u.getEmail()).isEqualTo("user@example.com");
        assertThat(u.getPasswordHash()).isEqualTo("HASH");
        assertThat(u.getFirstName()).isEqualTo("David");
        assertThat(u.getLastName()).isEqualTo("Andreasson");

        // defaults from entity
        assertThat(u.getRole()).isEqualTo(USER);      // default role
        assertThat(u.getIsActive()).isTrue();           // default active
        assertThat(u.getCreatedAt()).isNull();          // not set until @PrePersist
        assertThat(u.getUpdatedAt()).isNull();          // not set until @PrePersist
    }

    @Test
    void onCreate_setsTimestamps_andKeepsId() {
        User u = User.newUser("u@e.com", "H", "F", "L");
        UUID idBefore = u.getId();

        u.onCreate(); // package-private @PrePersist method

        assertThat(u.getId()).isEqualTo(idBefore);
        assertThat(u.getCreatedAt()).isNotNull();
        assertThat(u.getUpdatedAt()).isNotNull();
        assertThat(u.getCreatedAt()).isEqualTo(u.getUpdatedAt()); // set to same "now" at create
    }

    @Test
    void onCreate_generatesId_whenIdMissing() {
        User u = new User(); // no id set

        u.onCreate(); // should assign id and timestamps

        assertThat(u.getId()).isNotNull();
        assertThat(u.getCreatedAt()).isNotNull();
        assertThat(u.getUpdatedAt()).isNotNull();
    }

    @Test
    void onUpdate_onlyUpdates_updatedAt() throws InterruptedException {
        User u = User.newUser("u@e.com", "H", "F", "L");
        u.onCreate();
        Instant createdAt = u.getCreatedAt();
        Instant firstUpdatedAt = u.getUpdatedAt();

        // ensure time moves forward
        Thread.sleep(5);
        u.onUpdate();

        assertThat(u.getCreatedAt()).isEqualTo(createdAt);
        assertThat(u.getUpdatedAt()).isAfter(firstUpdatedAt);
        assertThat(Duration.between(firstUpdatedAt, u.getUpdatedAt()).toMillis())
                .isGreaterThanOrEqualTo(1);
    }

    @Test
    void setters_updateFields() {
        User u = User.newUser("old@e.com", "H", "OldF", "OldL");

        u.setEmail("new@e.com");
        u.setPasswordHash("NEW_HASH");
        u.setFirstName("NewF");
        u.setLastName("NewL");
        u.setRole(ADMIN);
        u.setIsActive(false);

        assertThat(u.getEmail()).isEqualTo("new@e.com");
        assertThat(u.getPasswordHash()).isEqualTo("NEW_HASH");
        assertThat(u.getFirstName()).isEqualTo("NewF");
        assertThat(u.getLastName()).isEqualTo("NewL");
        assertThat(u.getRole()).isEqualTo(ADMIN);
        assertThat(u.getIsActive()).isFalse();
    }

    @Test
    void newUser_generatesUniqueIds() {
        User u1 = User.newUser("a@e.com", "H", "A", "A");
        User u2 = User.newUser("b@e.com", "H", "B", "B");

        assertThat(u1.getId()).isNotEqualTo(u2.getId());
    }
}