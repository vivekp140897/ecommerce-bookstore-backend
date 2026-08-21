package com.bookstore.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AddressRequest(
        @Size(max = 100) String label,
        @NotBlank String line1,
        String line2,
        @NotBlank @Size(max = 100) String city,
        @NotBlank @Size(max = 100) String state,
        @NotBlank @Size(max = 20) String postalCode,
        @NotBlank @Size(min = 2, max = 2) String country,
        @NotNull Boolean isDefault
) {}
