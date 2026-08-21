package com.bookstore.service;

import com.bookstore.dto.request.BookRequest;
import com.bookstore.dto.response.BookResponse;
import com.bookstore.dto.response.PageResponse;
import com.bookstore.entity.Book;
import com.bookstore.entity.Category;
import com.bookstore.exception.ConflictException;
import com.bookstore.exception.ResourceNotFoundException;
import com.bookstore.repository.BookRepository;
import com.bookstore.repository.CategoryRepository;
import com.bookstore.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;
    private final CategoryRepository categoryRepository;
    private final ReviewRepository reviewRepository;

    public PageResponse<BookResponse> listBooks(String q, UUID categoryId,
                                                BigDecimal minPrice, BigDecimal maxPrice,
                                                Boolean inStock, String sortBy,
                                                int page, int pageSize) {
        Sort sort = resolveSort(sortBy);
        var pageable = PageRequest.of(page - 1, pageSize, sort);
        // Normalise to empty string — the JPQL guard uses :q = '' to skip LOWER/CONCAT
        // when there is no search term, avoiding Hibernate's null→bytea type inference.
        String qNorm = (q == null || q.isBlank()) ? "" : q.trim();
        Page<Book> result = bookRepository.search(qNorm, categoryId, minPrice, maxPrice, inStock, pageable);
        return toPage(result);
    }

    @Transactional
    public BookResponse createBook(BookRequest request) {
        if (bookRepository.existsByIsbn(request.isbn())) {
            throw new ConflictException("ISBN already exists: " + request.isbn());
        }
        Category category = findCategory(request.categoryId());
        Book book = Book.builder()
                .title(request.title())
                .author(request.author())
                .isbn(request.isbn())
                .description(request.description())
                .price(request.price())
                .stockQuantity(request.stockQuantity())
                .coverImageUrl(request.coverImageUrl())
                .language(request.language())
                .pageCount(request.pageCount())
                .publishedDate(request.publishedDate())
                .publisher(request.publisher())
                .category(category)
                .build();
        return toResponse(bookRepository.save(book));
    }

    public BookResponse getBookById(UUID id) {
        return toResponse(findById(id));
    }

    @Transactional
    public BookResponse updateBook(UUID id, BookRequest request) {
        Book book = findById(id);
        if (!book.getIsbn().equals(request.isbn()) && bookRepository.existsByIsbn(request.isbn())) {
            throw new ConflictException("ISBN already exists: " + request.isbn());
        }
        applyFields(book, request);
        return toResponse(bookRepository.save(book));
    }

    @Transactional
    public void deleteBook(UUID id) {
        bookRepository.delete(findById(id));
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    public Book findById(UUID id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found: " + id));
    }

    private Category findCategory(UUID id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + id));
    }

    private void applyFields(Book book, BookRequest request) {
        book.setTitle(request.title());
        book.setAuthor(request.author());
        book.setIsbn(request.isbn());
        book.setDescription(request.description());
        book.setPrice(request.price());
        book.setStockQuantity(request.stockQuantity());
        book.setCoverImageUrl(request.coverImageUrl());
        book.setLanguage(request.language());
        book.setPageCount(request.pageCount());
        book.setPublishedDate(request.publishedDate());
        book.setPublisher(request.publisher());
        book.setCategory(findCategory(request.categoryId()));
    }

    public BookResponse toResponse(Book b) {
        Double avg = reviewRepository.findAverageRatingByBookId(b.getId());
        long count = reviewRepository.countByBookId(b.getId());
        return new BookResponse(b.getId(), b.getTitle(), b.getAuthor(), b.getIsbn(),
                b.getDescription(), b.getPrice(), b.getStockQuantity(), b.getCoverImageUrl(),
                b.getLanguage(), b.getPageCount(), b.getPublishedDate(), b.getPublisher(),
                b.getCategory().getId(), avg, count, b.getCreatedAt(), b.getUpdatedAt());
    }

    private Sort resolveSort(String sortBy) {
        if (sortBy == null) return Sort.by(Sort.Direction.DESC, "createdAt");
        return switch (sortBy) {
            case "price_asc"  -> Sort.by(Sort.Direction.ASC,  "price");
            case "price_desc" -> Sort.by(Sort.Direction.DESC, "price");
            case "title_asc"  -> Sort.by(Sort.Direction.ASC,  "title");
            default           -> Sort.by(Sort.Direction.DESC, "createdAt");
        };
    }

    private PageResponse<BookResponse> toPage(Page<Book> page) {
        return new PageResponse<>(
                page.getContent().stream().map(this::toResponse).toList(),
                new PageResponse.PaginationMeta(
                        page.getNumber() + 1, page.getSize(),
                        page.getTotalElements(), page.getTotalPages()));
    }
}
