package com.bookstore.dto.response;

import java.time.Instant;
import java.util.UUID;

public record ReviewResponse(
        UUID id,
        UUID bookId,
        UUID userId,
        Integer rating,
        String title,
        String body,
        Boolean verifiedPurchase,
        Instant createdAt,
        Instant updatedAt
) {}
