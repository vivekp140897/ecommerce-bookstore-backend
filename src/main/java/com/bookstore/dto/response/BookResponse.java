package com.bookstore.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record BookResponse(
        UUID id,
        String title,
        String author,
        String isbn,
        String description,
        BigDecimal price,
        Integer stockQuantity,
        String coverImageUrl,
        String language,
        Integer pageCount,
        LocalDate publishedDate,
        String publisher,
        UUID categoryId,
        Double averageRating,
        Long reviewCount,
        Instant createdAt,
        Instant updatedAt
) {}
