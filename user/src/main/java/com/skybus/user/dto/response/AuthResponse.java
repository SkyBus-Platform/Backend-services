package com.skybus.user.dto.response;

/**
 * Returned by /api/auth/login and /api/auth/register.
 *
 * accessToken  — short-lived JWT (15 min – 24 h, configurable).
 *               Sent by the client in every subsequent request:
 *               Authorization: Bearer <accessToken>
 *
 * refreshToken — long-lived opaque token (7 days).
 *               Sent ONLY to POST /api/auth/refresh to get a new access token.
 *               Store it securely (httpOnly cookie preferred; localStorage in demos).
 *
 * expiresIn    — access token TTL in seconds (helps the client schedule refresh).
 */
public record AuthResponse(
        String accessToken,
        String refreshToken,
        long   expiresIn,     // seconds
        UserResponse user
) {}