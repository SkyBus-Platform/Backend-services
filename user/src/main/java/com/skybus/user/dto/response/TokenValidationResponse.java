package com.skybus.user.dto.response;

import java.util.UUID;

/**
 * Returned by GET /api/auth/validate.
 *
 * The API gateway calls this endpoint to verify a token and retrieve the
 * user's id and role, which it then forwards as X-User-Id and X-User-Role
 * headers to downstream services.
 *
 * This endpoint is internal — it should be protected at the network/gateway
 * level so that only the gateway can call it (e.g. via a shared internal
 * API key or mTLS in production).
 */
public record TokenValidationResponse(
        UUID   userId,
        String email,
        String role,
        String fullName
) {}