package com.skybus.user.entity;

import com.skybus.user.audit.Auditable;
import com.skybus.user.enums.Role;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Core user account entity.
 *
 * Stored in: usersDB → table "users"
 *
 * Relationships:
 *  - User 1─────* RefreshToken  (a user may have multiple active sessions)
 *
 * Design notes:
 *  - The password is NEVER stored in plain text. AuthService must hash it
 *    with BCrypt (strength ≥ 12) before calling save().
 *  - "email" is the natural unique identifier used for login. The UUID "id"
 *    is what every other service stores as userId (no cross-DB foreign key).
 *  - isActive = false is a soft-delete / account suspension flag. Deactivated
 *    users cannot log in but their data is retained for audit purposes.
 */
@Entity
@Table(
        name = "skybus_user",
        schema = "skybus_user_schema",
        indexes = {
                @Index(name = "idx_user_email",     columnList = "email",     unique = true),
                @Index(name = "idx_user_role",      columnList = "role"),
                @Index(name = "idx_user_is_active", columnList = "is_active")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User extends Auditable {

    // ─── Primary Key ────────────────────────────────────────────────────────
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    // ─── Identity ───────────────────────────────────────────────────────────
    @Email(message = "Must be a valid email address")
    @NotBlank(message = "Email is required")
    @Column(nullable = false, unique = true, length = 255)
    private String email;

    /**
     * BCrypt hash. Never expose this field in API responses.
     * Use @JsonIgnore on your DTO or exclude from projection.
     */
    @NotBlank
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    // ─── Personal Details ────────────────────────────────────────────────────
    @NotBlank(message = "First name is required")
    @Size(max = 100)
    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 100)
    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Size(max = 20)
    @Column(length = 20)
    private String phone;

    // ─── Role & Status ───────────────────────────────────────────────────────
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private Role role = Role.PASSENGER;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean isActive = true;

    // ─── Relationships ───────────────────────────────────────────────────────
    /**
     * A user can have multiple refresh tokens (one per device / session).
     * All tokens are invalidated when the user logs out from all devices
     * or changes their password.
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<RefreshToken> refreshTokens = new ArrayList<>();

    // ─── Helpers ─────────────────────────────────────────────────────────────
    public String getFullName() {
        return firstName + " " + lastName;
    }

    public void addRefreshToken(RefreshToken token) {
        refreshTokens.add(token);
        token.setUser(this);
    }

    public void revokeAllTokens() {
        refreshTokens.forEach(t -> t.setRevoked(true));
    }
}