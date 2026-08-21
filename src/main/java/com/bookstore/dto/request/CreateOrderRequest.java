package com.bookstore.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateOrderRequest(
        @NotNull UUID shippingAddressId,
        UUID billingAddressId,
        String couponCode,
        @NotBlank String paymentMethodToken
) {}
