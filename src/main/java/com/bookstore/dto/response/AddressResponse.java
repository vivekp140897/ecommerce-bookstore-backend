package com.bookstore.dto.response;

import java.time.Instant;
import java.util.UUID;

public record AddressResponse(
        UUID id,
        UUID userId,
        String label,
        String line1,
        String line2,
        String city,
        String state,
        String postalCode,
        String country,
        Boolean isDefault,
        Instant createdAt
) {}
