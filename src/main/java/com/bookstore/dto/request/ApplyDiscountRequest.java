package com.bookstore.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ApplyDiscountRequest(@NotBlank String couponCode) {}
