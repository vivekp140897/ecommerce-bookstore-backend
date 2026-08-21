package com.bookstore.controller;

import com.bookstore.dto.request.ApplyDiscountRequest;
import com.bookstore.dto.request.CreateOrderRequest;
import com.bookstore.dto.response.DiscountResult;
import com.bookstore.dto.response.OrderResponse;
import com.bookstore.service.CheckoutService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/checkout")
@RequiredArgsConstructor
public class CheckoutController {

    private final CheckoutService checkoutService;

    @PostMapping("/discount")
    public ResponseEntity<DiscountResult> applyDiscount(
            @Valid @RequestBody ApplyDiscountRequest request) {
        return ResponseEntity.ok(checkoutService.previewDiscount(request));
    }

    @PostMapping("/orders")
    public ResponseEntity<OrderResponse> createOrder(
            @Valid @RequestBody CreateOrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(checkoutService.createOrder(request));
    }
}
