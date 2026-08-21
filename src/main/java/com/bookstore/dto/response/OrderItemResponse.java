package com.bookstore.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderItemResponse(
        UUID id,
        UUID orderId,
        UUID bookId,
        BookResponse book,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal subtotal
) {}
