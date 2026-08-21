package com.bookstore.repository;

import com.bookstore.entity.WishlistItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface WishlistItemRepository extends JpaRepository<WishlistItem, UUID> {
    Optional<WishlistItem> findByIdAndWishlistId(UUID id, UUID wishlistId);
    boolean existsByWishlistIdAndBookId(UUID wishlistId, UUID bookId);
}
