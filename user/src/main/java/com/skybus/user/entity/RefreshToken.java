package com.skybus.user.entity;

import com.skybus.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Tracks issued JWT refresh tokens.
 *
 * Stored in: usersDB → table "refresh_tokens"
 *
 * How it works:
 *  1. On login, a short-lived access token (15 min) + a long-lived refresh
 *     token (7 days) are issued. Only the refresh token is stored here.
 *  2. When the access token expires, the client sends the refresh token to
 *     /api/auth/refresh. We validate it against this table and issue a new pair.
 *  3. On logout (or password change), the token is marked revoked = true so
 *     it cannot be used again even if it hasn't expired yet.
 *
 * A scheduled job should purge rows where expiresAt < NOW() to keep the table small.
 */
@Entity
@Table(
        name = "skybus_refresh_token",
        schema = "skybus_user_schema",
        indexes = {
                @Index(name = "idx_refresh_token_token",   columnList = "token",   unique = true),
                @Index(name = "idx_refresh_token_user_id", columnList = "user_id"),
                @Index(name = "idx_refresh_token_expires", columnList = "expires_at")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    /** The opaque token string (UUID v4, base64-encoded, etc.). */
    @Column(nullable = false, unique = true, length = 512)
    private String token;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    @Builder.Default
    private boolean revoked = false;

    // ─── Helpers ─────────────────────────────────────────────────────────────

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean isValid() {
        return !revoked && !isExpired();
    }
}