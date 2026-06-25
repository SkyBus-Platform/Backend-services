package com.skybus.user.dto.response;

import com.skybus.user.entity.User;
import com.skybus.user.enums.Role;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Safe user representation — password hash is NEVER included.
 */
public record UserResponse(
        UUID          id,
        String        email,
        String        firstName,
        String        lastName,
        String        phone,
        Role          role,
        boolean       isActive,
        LocalDateTime createdAt
) {
    /** Factory method — converts entity to response DTO in one place. */
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhone(),
                user.getRole(),
                user.isActive(),
                user.getCreatedAt()
        );
    }
}