package com.skybus.user.service;

import com.skybus.user.dto.request.UpdateProfileRequest;
import com.skybus.user.dto.response.UserResponse;
import com.skybus.user.entity.User;
import com.skybus.user.exception.UserNotFoundException;
import com.skybus.user.repository.RefreshTokenRepository;
import com.skybus.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository         userRepo;
    private final RefreshTokenRepository tokenRepo;

    // ── Profile read ──────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public UserResponse getProfile(UUID userId) {
        return UserResponse.from(findActiveUser(userId));
    }

    // ── Profile update ────────────────────────────────────────────────────────

    @Transactional
    public UserResponse updateProfile(UUID userId, UpdateProfileRequest req) {
        User user = findActiveUser(userId);

        // Only update fields that were actually sent (non-null)
        if (StringUtils.hasText(req.getFirstName())) user.setFirstName(req.getFirstName());
        if (StringUtils.hasText(req.getLastName()))  user.setLastName(req.getLastName());
        if (StringUtils.hasText(req.getPhone()))     user.setPhone(req.getPhone());

        return UserResponse.from(userRepo.save(user));
    }

    // ── Deactivation ──────────────────────────────────────────────────────────

    @Transactional
    public void deactivate(UUID userId) {
        findActiveUser(userId);          // throws 404 if not found
        userRepo.deactivate(userId);
        tokenRepo.revokeAllByUser(userRepo.getReferenceById(userId));
        log.info("User deactivated: {}", userId);
    }

    // ── Scheduled cleanup ─────────────────────────────────────────────────────

    /**
     * Runs every night at 02:00.
     * Deletes refresh tokens that have passed their expiry date.
     * Keeps the refresh_tokens table lean without manual intervention.
     */
    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void cleanupExpiredTokens() {
        int deleted = tokenRepo.deleteExpiredBefore(LocalDateTime.now());
        if (deleted > 0) {
            log.info("Cleaned up {} expired refresh tokens", deleted);
        }
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private User findActiveUser(UUID userId) {
        return userRepo.findById(userId)
                .filter(User::isActive)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));
    }
}