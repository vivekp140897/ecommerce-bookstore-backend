package com.bookstore.repository;

import com.bookstore.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {
    boolean existsBySlug(String slug);
    Optional<Category> findBySlug(String slug);
    Page<Category> findByParentId(UUID parentId, Pageable pageable);
    Page<Category> findByParentIsNull(Pageable pageable);
}
