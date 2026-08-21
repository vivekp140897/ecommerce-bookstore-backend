package com.bookstore.service;

import com.bookstore.dto.request.AddWishlistItemRequest;
import com.bookstore.dto.response.BookResponse;
import com.bookstore.dto.response.WishlistItemResponse;
import com.bookstore.dto.response.WishlistResponse;
import com.bookstore.entity.Book;
import com.bookstore.entity.User;
import com.bookstore.entity.Wishlist;
import com.bookstore.entity.WishlistItem;
import com.bookstore.exception.ConflictException;
import com.bookstore.exception.ResourceNotFoundException;
import com.bookstore.repository.WishlistItemRepository;
import com.bookstore.repository.WishlistRepository;
import com.bookstore.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final WishlistItemRepository wishlistItemRepository;
    private final BookService bookService;
    private final SecurityUtils securityUtils;

    public WishlistResponse getWishlist() {
        return toResponse(getOrCreate());
    }

    @Transactional
    public WishlistResponse addItem(AddWishlistItemRequest request) {
        Wishlist wishlist = getOrCreate();
        if (wishlistItemRepository.existsByWishlistIdAndBookId(
                wishlist.getId(), request.bookId())) {
            throw new ConflictException("Book already in wishlist.");
        }
        Book book = bookService.findById(request.bookId());
        WishlistItem item = WishlistItem.builder()
                .wishlist(wishlist)
                .book(book)
                .build();
        wishlist.getItems().add(wishlistItemRepository.save(item));
        return toResponse(wishlistRepository.save(wishlist));
    }

    @Transactional
    public WishlistResponse removeItem(UUID wishlistItemId) {
        Wishlist wishlist = getOrCreate();
        WishlistItem item = wishlistItemRepository
                .findByIdAndWishlistId(wishlistItemId, wishlist.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Wishlist item not found: " + wishlistItemId));
        wishlist.getItems().remove(item);
        wishlistItemRepository.delete(item);
        return toResponse(wishlistRepository.save(wishlist));
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private Wishlist getOrCreate() {
        User user = securityUtils.getCurrentUser();
        return wishlistRepository.findByUser(user).orElseGet(() -> {
            Wishlist w = Wishlist.builder().user(user).build();
            return wishlistRepository.save(w);
        });
    }

    private WishlistResponse toResponse(Wishlist w) {
        var items = w.getItems().stream().map(this::toItemResponse).toList();
        return new WishlistResponse(w.getId(), w.getUser().getId(), items, items.size());
    }

    private WishlistItemResponse toItemResponse(WishlistItem wi) {
        BookResponse book = bookService.toResponse(wi.getBook());
        return new WishlistItemResponse(wi.getId(), wi.getBook().getId(), book, wi.getAddedAt());
    }
}
