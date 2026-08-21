package com.bookstore.service;

import com.bookstore.dto.request.ReviewRequest;
import com.bookstore.dto.response.PageResponse;
import com.bookstore.dto.response.ReviewResponse;
import com.bookstore.entity.Book;
import com.bookstore.entity.Review;
import com.bookstore.entity.User;
import com.bookstore.entity.enums.OrderStatus;
import com.bookstore.exception.BusinessException;
import com.bookstore.exception.ConflictException;
import com.bookstore.exception.ResourceNotFoundException;
import com.bookstore.repository.OrderRepository;
import com.bookstore.repository.ReviewRepository;
import com.bookstore.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final BookService bookService;
    private final OrderRepository orderRepository;
    private final SecurityUtils securityUtils;

    public PageResponse<ReviewResponse> listReviews(UUID bookId, String sortBy,
                                                    int page, int pageSize) {
        Book book = bookService.findById(bookId);
        Sort sort = resolveSort(sortBy);
        Page<Review> result = reviewRepository.findByBook(book, PageRequest.of(page - 1, pageSize, sort));
        return new PageResponse<>(
                result.getContent().stream().map(this::toResponse).toList(),
                new PageResponse.PaginationMeta(result.getNumber() + 1, result.getSize(),
                        result.getTotalElements(), result.getTotalPages()));
    }

    @Transactional
    public ReviewResponse createReview(UUID bookId, ReviewRequest request) {
        User user = securityUtils.getCurrentUser();
        Book book = bookService.findById(bookId);
        if (reviewRepository.existsByBookAndUser(book, user)) {
            throw new ConflictException("You have already reviewed this book.");
        }
        boolean verified = orderRepository.findByUser(user, PageRequest.of(0, Integer.MAX_VALUE))
                .getContent().stream()
                .filter(o -> o.getStatus() == OrderStatus.DELIVERED)
                .flatMap(o -> o.getItems().stream())
                .anyMatch(oi -> oi.getBook().getId().equals(bookId));

        Review review = Review.builder()
                .book(book)
                .user(user)
                .rating(request.rating())
                .title(request.title())
                .body(request.body())
                .verifiedPurchase(verified)
                .build();
        return toResponse(reviewRepository.save(review));
    }

    public ReviewResponse getReviewById(UUID bookId, UUID reviewId) {
        return toResponse(findOwned(bookId, reviewId));
    }

    @Transactional
    public ReviewResponse updateReview(UUID bookId, UUID reviewId, ReviewRequest request) {
        User user = securityUtils.getCurrentUser();
        Review review = findOwned(bookId, reviewId);
        if (!review.getUser().getId().equals(user.getId())) {
            throw new BusinessException("You can only update your own reviews.");
        }
        review.setRating(request.rating());
        review.setTitle(request.title());
        review.setBody(request.body());
        return toResponse(reviewRepository.save(review));
    }

    @Transactional
    public void deleteReview(UUID bookId, UUID reviewId) {
        User user = securityUtils.getCurrentUser();
        Review review = findOwned(bookId, reviewId);
        boolean isOwner = review.getUser().getId().equals(user.getId());
        boolean isAdmin = user.getRole().name().equals("ADMIN");
        if (!isOwner && !isAdmin) {
            throw new BusinessException("Access denied.");
        }
        reviewRepository.delete(review);
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private Review findOwned(UUID bookId, UUID reviewId) {
        Book book = bookService.findById(bookId);
        return reviewRepository.findById(reviewId)
                .filter(r -> r.getBook().getId().equals(bookId))
                .orElseThrow(() -> new ResourceNotFoundException("Review not found: " + reviewId));
    }

    private ReviewResponse toResponse(Review r) {
        return new ReviewResponse(r.getId(), r.getBook().getId(), r.getUser().getId(),
                r.getRating(), r.getTitle(), r.getBody(), r.getVerifiedPurchase(),
                r.getCreatedAt(), r.getUpdatedAt());
    }

    private Sort resolveSort(String sortBy) {
        if (sortBy == null) return Sort.by(Sort.Direction.DESC, "createdAt");
        return switch (sortBy) {
            case "oldest"     -> Sort.by(Sort.Direction.ASC,  "createdAt");
            case "rating_asc" -> Sort.by(Sort.Direction.ASC,  "rating");
            case "rating_desc"-> Sort.by(Sort.Direction.DESC, "rating");
            default           -> Sort.by(Sort.Direction.DESC, "createdAt");
        };
    }
}
