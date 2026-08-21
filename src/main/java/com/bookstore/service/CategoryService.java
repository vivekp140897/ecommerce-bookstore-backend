package com.bookstore.service;

import com.bookstore.dto.request.CategoryRequest;
import com.bookstore.dto.response.CategoryResponse;
import com.bookstore.dto.response.PageResponse;
import com.bookstore.entity.Category;
import com.bookstore.exception.ConflictException;
import com.bookstore.exception.ResourceNotFoundException;
import com.bookstore.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public PageResponse<CategoryResponse> listCategories(UUID parentId, int page, int pageSize) {
        var pageable = PageRequest.of(page - 1, pageSize);
        Page<Category> result = (parentId != null)
                ? categoryRepository.findByParentId(parentId, pageable)
                : categoryRepository.findByParentIsNull(pageable);
        return toPage(result);
    }

    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        if (categoryRepository.existsBySlug(request.slug())) {
            throw new ConflictException("Slug already in use: " + request.slug());
        }
        Category parent = request.parentId() != null ? findById(request.parentId()) : null;
        Category category = Category.builder()
                .name(request.name())
                .slug(request.slug())
                .description(request.description())
                .parent(parent)
                .build();
        return toResponse(categoryRepository.save(category));
    }

    public CategoryResponse getCategoryById(UUID id) {
        return toResponse(findById(id));
    }

    @Transactional
    public CategoryResponse updateCategory(UUID id, CategoryRequest request) {
        Category category = findById(id);
        if (!category.getSlug().equals(request.slug())
                && categoryRepository.existsBySlug(request.slug())) {
            throw new ConflictException("Slug already in use: " + request.slug());
        }
        Category parent = request.parentId() != null ? findById(request.parentId()) : null;
        category.setName(request.name());
        category.setSlug(request.slug());
        category.setDescription(request.description());
        category.setParent(parent);
        return toResponse(categoryRepository.save(category));
    }

    @Transactional
    public void deleteCategory(UUID id) {
        categoryRepository.delete(findById(id));
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private Category findById(UUID id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + id));
    }

    public CategoryResponse toResponse(Category c) {
        return new CategoryResponse(c.getId(), c.getName(), c.getSlug(), c.getDescription(),
                c.getParent() != null ? c.getParent().getId() : null,
                c.getCreatedAt(), c.getUpdatedAt());
    }

    private PageResponse<CategoryResponse> toPage(Page<Category> page) {
        return new PageResponse<>(
                page.getContent().stream().map(this::toResponse).toList(),
                new PageResponse.PaginationMeta(
                        page.getNumber() + 1, page.getSize(),
                        page.getTotalElements(), page.getTotalPages()));
    }
}
