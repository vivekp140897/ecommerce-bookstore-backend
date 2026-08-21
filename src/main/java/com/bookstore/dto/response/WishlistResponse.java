package com.bookstore.dto.response;

import java.util.List;
import java.util.UUID;

public record WishlistResponse(
        UUID id,
        UUID userId,
        List<WishlistItemResponse> items,
        int itemCount
) {}
