package com.bookstore.dto.response;

import com.bookstore.entity.enums.PaymentMethod;
import com.bookstore.entity.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentResponse(
        UUID id,
        UUID orderId,
        PaymentStatus status,
        PaymentMethod method,
        BigDecimal amount,
        String currency,
        String transactionId,
        Instant processedAt
) {}
