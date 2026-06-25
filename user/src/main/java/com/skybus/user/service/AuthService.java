package com.skybus.user.service;

import com.skybus.user.dto.request.LoginRequest;
import com.skybus.user.dto.request.RefreshTokenRequest;
import com.skybus.user.dto.request.RegisterRequest;
import com.skybus.user.dto.response.AuthResponse;
import com.skybus.user.dto.response.TokenValidationResponse;
import com.skybus.user.dto.response.UserResponse;
import com.skybus.user.entity.RefreshToken;
import com.skybus.user.entity.User;
import com.skybus.user.exception.EmailAlreadyExistsException;
import com.skybus.user.exception.InvalidTokenException;
import com.skybus.user.repository.RefreshTokenRepository;
import com.skybus.user.repository.UserRepository;
import com.skybus.user.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository         userRepo;
    private final RefreshTokenRepository tokenRepo;
    private final PasswordEncoder        encoder;
    private final JwtUtil                jwtUtil;

    @Value("${jwt.expiry-ms:86400000}")
    private long accessTokenExpiryMs;

    @Value("${jwt.refresh-expiry-days:7}")
    private int refreshExpiryDays;

    // ── Register ─────────────────────────────────────────────────────────────

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        if (userRepo.existsByEmail(req.email())) {
            throw new EmailAlreadyExistsException(req.email());
        }

        User user = User.builder()
                .email(req.email())
                .passwordHash(encoder.encode(req.password()))
                .firstName(req.firstName())
                .lastName(req.lastName())
                .phone(req.phone())
                .build();

        userRepo.save(user);
        log.info("New user registered: {}", user.getEmail());

        return buildAuthResponse(user);
    }

    // ── Login ─────────────────────────────────────────────────────────────────

    @Transactional
    public AuthResponse login(LoginRequest req) {
        User user = userRepo.findByEmail(req.email())
                .filter(User::isActive)
                .orElseThrow(() -> new InvalidTokenException("Invalid credentials"));

        if (!encoder.matches(req.password(), user.getPasswordHash())) {
            // Same exception message — don't leak whether the email exists
            throw new InvalidTokenException("Invalid credentials");
        }

        log.info("User logged in: {}", user.getEmail());
        return buildAuthResponse(user);
    }

    // ── Refresh ────────────────────────────────────────────────────────────────

    @Transactional
    public AuthResponse refresh(RefreshTokenRequest req) {
        RefreshToken stored = tokenRepo.findByToken(req.refreshToken())
                .orElseThrow(() -> new InvalidTokenException("Refresh token not found"));

        if (!stored.isValid()) {
            // Token expired or revoked — reject and clean up
            tokenRepo.delete(stored);
            throw new InvalidTokenException("Refresh token is invalid or expired");
        }

        // Rotate: revoke old token, issue a new pskybus
        stored.setRevoked(true);
        tokenRepo.save(stored);

        return buildAuthResponse(stored.getUser());
    }

    // ── Validate (called by API gateway) ─────────────────────────────────────

    public TokenValidationResponse validate(String bearerToken) {
        String token = bearerToken.startsWith("Bearer ")
                ? bearerToken.substring(7) : bearerToken;

        if (!jwtUtil.isValid(token)) {
            throw new InvalidTokenException("Token is invalid or expired");
        }

        return new TokenValidationResponse(
                jwtUtil.extractUserId(token),
                jwtUtil.extractEmail(token),
                jwtUtil.extractRole(token),
                null   // fullName not in JWT claims for brevity; add if needed
        );
    }

    // ── Logout ────────────────────────────────────────────────────────────────

    @Transactional
    public void logout(String refreshToken) {
        tokenRepo.findByToken(refreshToken)
                .ifPresent(t -> {
                    t.setRevoked(true);
                    tokenRepo.save(t);
                });
    }

    @Transactional
    public void logoutAll(UUID userId) {
        userRepo.findById(userId)
                .ifPresent(tokenRepo::revokeAllByUser);
    }

    // ── Internal helpers ──────────────────────────────────────────────────────

    private AuthResponse buildAuthResponse(User user) {
        String accessToken  = jwtUtil.generateAccessToken(user);
        String refreshToken = issueRefreshToken(user);

        return new AuthResponse(
                accessToken,
                refreshToken,
                accessTokenExpiryMs / 1000,
                UserResponse.from(user)
        );
    }

    private String issueRefreshToken(User user) {
        RefreshToken token = RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .user(user)
                .expiresAt(LocalDateTime.now().plusDays(refreshExpiryDays))
                .build();
        tokenRepo.save(token);
        return token.getToken();
    }
}