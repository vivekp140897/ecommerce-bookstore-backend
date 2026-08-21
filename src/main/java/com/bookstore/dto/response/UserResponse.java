package com.bookstore.dto.response;

import com.bookstore.entity.enums.UserRole;

import java.time.Instant;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String firstName,
        String lastName,
        String email,
        String phone,
        UserRole role,
        Instant createdAt,
        Instant updatedAt
) {}
