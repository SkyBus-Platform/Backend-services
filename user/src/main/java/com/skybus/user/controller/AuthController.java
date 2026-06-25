package com.skybus.user.controller;

import com.skybus.user.dto.request.LoginRequest;
import com.skybus.user.dto.request.RefreshTokenRequest;
import com.skybus.user.dto.request.RegisterRequest;
import com.skybus.user.dto.response.AuthResponse;
import com.skybus.user.dto.response.TokenValidationResponse;
import com.skybus.user.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * All endpoints here are public (permitted in SecurityConfig).
 *
 * POST /api/auth/register   — create account, returns token pskybus
 * POST /api/auth/login      — authenticate, returns token pskybus
 * POST /api/auth/refresh    — exchange refresh token for new access token
 * POST /api/auth/logout     — revoke one refresh token
 * GET  /api/auth/validate   — verify a token (called by the API gateway)
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(req));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest req) {
        return ResponseEntity.ok(authService.login(req));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest req) {
        return ResponseEntity.ok(authService.refresh(req));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshTokenRequest req) {
        authService.logout(req.refreshToken());
        return ResponseEntity.noContent().build();
    }

    /**
     * Called by the API gateway to validate a Bearer token and get user info.
     * Returns 200 + user info if valid, 401 if not.
     * The gateway forwards userId and role as X-User-Id / X-User-Role headers.
     */
    @GetMapping("/validate")
    public ResponseEntity<TokenValidationResponse> validate(
            @RequestHeader("Authorization") String bearerToken) {
        return ResponseEntity.ok(authService.validate(bearerToken));
    }
}