package com.bookstore.repository;

import com.bookstore.entity.Review;
import com.bookstore.entity.Book;
import com.bookstore.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface ReviewRepository extends JpaRepository<Review, UUID> {
    Page<Review> findByBook(Book book, Pageable pageable);
    Optional<Review> findByBookAndUser(Book book, User user);
    boolean existsByBookAndUser(Book book, User user);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.book.id = :bookId")
    Double findAverageRatingByBookId(@Param("bookId") UUID bookId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.book.id = :bookId")
    long countByBookId(@Param("bookId") UUID bookId);
}
