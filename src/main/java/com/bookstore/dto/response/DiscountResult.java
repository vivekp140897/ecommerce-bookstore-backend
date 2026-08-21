package com.bookstore.dto.response;

import com.bookstore.entity.enums.DiscountType;

import java.math.BigDecimal;

public record DiscountResult(
        String couponCode,
        DiscountType discountType,
        BigDecimal discountValue,
        BigDecimal discountAmount,
        BigDecimal newTotal
) {}
