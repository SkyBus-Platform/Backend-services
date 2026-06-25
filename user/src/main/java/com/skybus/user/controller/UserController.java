package com.skybus.user.controller;

import com.skybus.user.dto.request.UpdateProfileRequest;
import com.skybus.user.dto.response.UserResponse;
import com.skybus.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * All endpoints require a valid JWT (enforced by SecurityConfig + JwtAuthFilter).
 *
 * The authenticated user's UUID is extracted from the SecurityContext via
 * @AuthenticationPrincipal — Spring resolves this from the principal set in
 * JwtAuthFilter (which used the JWT subject claim as the principal name).
 *
 * GET   /api/users/me         — get own profile
 * PATCH /api/users/me         — update own profile
 * DELETE /api/users/me        — deactivate own account
 * GET   /api/users/{id}       — admin only: get any user's profile
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMyProfile(
            @AuthenticationPrincipal String userId) {
        return ResponseEntity.ok(userService.getProfile(UUID.fromString(userId)));
    }

    @PatchMapping("/me")
    public ResponseEntity<UserResponse> updateMyProfile(
            @AuthenticationPrincipal String userId,
            @Valid @RequestBody UpdateProfileRequest req) {
        return ResponseEntity.ok(
                userService.updateProfile(UUID.fromString(userId), req));
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> deactivateMyAccount(
            @AuthenticationPrincipal String userId) {
        userService.deactivate(UUID.fromString(userId));
        return ResponseEntity.noContent().build();
    }

    /**
     * Admin-only endpoint — fetch any user by ID.
     * @PreAuthorize is evaluated after the JWT is set in SecurityContext.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> getUserById(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.getProfile(id));
    }
}