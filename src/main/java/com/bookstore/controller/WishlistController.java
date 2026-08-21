package com.bookstore.controller;

import com.bookstore.dto.request.AddWishlistItemRequest;
import com.bookstore.dto.response.WishlistResponse;
import com.bookstore.service.WishlistService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/wishlist")
@RequiredArgsConstructor
public class WishlistController {

    private final WishlistService wishlistService;

    @GetMapping
    public ResponseEntity<WishlistResponse> getWishlist() {
        return ResponseEntity.ok(wishlistService.getWishlist());
    }

    @PostMapping("/items")
    public ResponseEntity<WishlistResponse> addItem(
            @Valid @RequestBody AddWishlistItemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(wishlistService.addItem(request));
    }

    @DeleteMapping("/items/{wishlistItemId}")
    public ResponseEntity<WishlistResponse> removeItem(@PathVariable UUID wishlistItemId) {
        return ResponseEntity.ok(wishlistService.removeItem(wishlistItemId));
    }
}
