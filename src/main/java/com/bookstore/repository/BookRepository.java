package com.bookstore.repository;

import com.bookstore.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.UUID;

public interface BookRepository extends JpaRepository<Book, UUID> {

    boolean existsByIsbn(String isbn);

    @Query("""
            SELECT b FROM Book b
            WHERE (:q = '' OR LOWER(b.title) LIKE LOWER(CONCAT('%', :q, '%'))
                           OR LOWER(b.author) LIKE LOWER(CONCAT('%', :q, '%')))
              AND (:categoryId IS NULL OR b.category.id = :categoryId)
              AND (:minPrice IS NULL OR b.price >= :minPrice)
              AND (:maxPrice IS NULL OR b.price <= :maxPrice)
              AND (:inStock IS NULL OR (:inStock = TRUE AND b.stockQuantity > 0)
                                   OR (:inStock = FALSE))
            """)
    Page<Book> search(
            @Param("q") String q,
            @Param("categoryId") UUID categoryId,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("inStock") Boolean inStock,
            Pageable pageable);
}
