package com.bookstore.dto.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record BookRequest(
        @NotBlank @Size(max = 255) String title,
        @NotBlank @Size(max = 255) String author,
        @NotBlank @Pattern(regexp = "^(?:\\d{9}[\\dX]|97[89]\\d{10})$", message = "Invalid ISBN") String isbn,
        String description,
        @NotNull @DecimalMin("0.00") BigDecimal price,
        @NotNull @Min(0) Integer stockQuantity,
        String coverImageUrl,
        @Size(max = 10) String language,
        @Min(1) Integer pageCount,
        LocalDate publishedDate,
        @Size(max = 255) String publisher,
        @NotNull UUID categoryId
) {}
