package com.bookstore.controller;

import com.bookstore.dto.request.ReviewRequest;
import com.bookstore.dto.response.PageResponse;
import com.bookstore.dto.response.ReviewResponse;
import com.bookstore.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/books/{bookId}/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping
    public ResponseEntity<PageResponse<ReviewResponse>> listReviews(
            @PathVariable UUID bookId,
            @RequestParam(required = false, defaultValue = "newest") String sortBy,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        return ResponseEntity.ok(reviewService.listReviews(bookId, sortBy, page, pageSize));
    }

    @PostMapping
    public ResponseEntity<ReviewResponse> createReview(@PathVariable UUID bookId,
                                                       @Valid @RequestBody ReviewRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reviewService.createReview(bookId, request));
    }

    @GetMapping("/{reviewId}")
    public ResponseEntity<ReviewResponse> getReviewById(@PathVariable UUID bookId,
                                                        @PathVariable UUID reviewId) {
        return ResponseEntity.ok(reviewService.getReviewById(bookId, reviewId));
    }

    @PutMapping("/{reviewId}")
    public ResponseEntity<ReviewResponse> updateReview(@PathVariable UUID bookId,
                                                       @PathVariable UUID reviewId,
                                                       @Valid @RequestBody ReviewRequest request) {
        return ResponseEntity.ok(reviewService.updateReview(bookId, reviewId, request));
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> deleteReview(@PathVariable UUID bookId,
                                             @PathVariable UUID reviewId) {
        reviewService.deleteReview(bookId, reviewId);
        return ResponseEntity.noContent().build();
    }
}
