package com.bookstore.dto.response;

import java.time.Instant;
import java.util.UUID;

public record WishlistItemResponse(
        UUID id,
        UUID bookId,
        BookResponse book,
        Instant addedAt
) {}
