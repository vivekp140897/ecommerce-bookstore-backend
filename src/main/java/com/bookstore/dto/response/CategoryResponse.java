package com.bookstore.dto.response;

import java.time.Instant;
import java.util.UUID;

public record CategoryResponse(
        UUID id,
        String name,
        String slug,
        String description,
        UUID parentId,
        Instant createdAt,
        Instant updatedAt
) {}
