package com.bookstore.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record CartItemResponse(
        UUID id,
        UUID cartId,
        UUID bookId,
        BookResponse book,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal subtotal
) {}
