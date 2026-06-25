package com.skybus.user.security;

import com.skybus.user.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;

/**
 * Stateless JWT utility — no database calls.
 *
 * Token anatomy (payload claims):
 *   sub  — user UUID (used by downstream services as userId)
 *   email — for informational display; never trust for auth decisions
 *   role  — PASSENGER | ADMIN
 *   iat  — issued at (epoch seconds)
 *   exp  — expiry (epoch seconds)
 *
 * Algorithm: HS256 (HMAC-SHA256).
 * The jwt.secret must be a Base64-encoded string of at least 32 bytes.
 * Generate one with: openssl rand -base64 32
 */
@Slf4j
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiry-ms:86400000}")        // default 24 h
    private long expiryMs;

    // ── Token generation ────────────────────────────────────────────────────

    public String generateAccessToken(User user) {
        return buildToken(user, expiryMs);
    }

    private String buildToken(User user, long ttlMs) {
        Date now    = new Date();
        Date expiry = new Date(now.getTime() + ttlMs);

        return Jwts.builder()
                .subject(user.getId().toString())
                .claim("email", user.getEmail())
                .claim("role",  user.getRole().name())
                .claim("name",  user.getFirstName() + " " + user.getLastName())
                .issuedAt(now)
                .expiration(expiry)
                .signWith(secretKey())
                .compact();
    }

    // ── Token validation ─────────────────────────────────────────────────────

    /**
     * Validates the token signature and expiry.
     * Returns true only if the token is correctly signed AND not expired.
     */
    public boolean isValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.debug("JWT expired: {}", e.getMessage());
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("JWT invalid: {}", e.getMessage());
        }
        return false;
    }

    // ── Claims extraction ─────────────────────────────────────────────────────

    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public UUID extractUserId(String token) {
        return UUID.fromString(parseClaims(token).getSubject());
    }

    public String extractEmail(String token) {
        return parseClaims(token).get("email", String.class);
    }

    public String extractRole(String token) {
        return parseClaims(token).get("role", String.class);
    }

    // ── Internal ──────────────────────────────────────────────────────────────

    private SecretKey secretKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    }
}