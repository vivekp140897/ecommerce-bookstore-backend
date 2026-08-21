package com.bookstore.dto.response;

import com.bookstore.entity.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderResponse(
        UUID id,
        UUID userId,
        OrderStatus status,
        List<OrderItemResponse> items,
        AddressResponse shippingAddress,
        AddressResponse billingAddress,
        String couponCode,
        BigDecimal subtotal,
        BigDecimal discountAmount,
        BigDecimal shippingCost,
        BigDecimal tax,
        BigDecimal total,
        PaymentResponse payment,
        Instant placedAt,
        Instant updatedAt
) {}
