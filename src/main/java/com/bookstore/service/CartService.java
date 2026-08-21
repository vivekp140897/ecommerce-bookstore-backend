package com.bookstore.service;

import com.bookstore.dto.request.AddCartItemRequest;
import com.bookstore.dto.request.UpdateCartItemRequest;
import com.bookstore.dto.response.BookResponse;
import com.bookstore.dto.response.CartItemResponse;
import com.bookstore.dto.response.CartResponse;
import com.bookstore.entity.Book;
import com.bookstore.entity.Cart;
import com.bookstore.entity.CartItem;
import com.bookstore.entity.User;
import com.bookstore.exception.BusinessException;
import com.bookstore.exception.ResourceNotFoundException;
import com.bookstore.repository.CartItemRepository;
import com.bookstore.repository.CartRepository;
import com.bookstore.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final BookService bookService;
    private final SecurityUtils securityUtils;

    public CartResponse getCart() {
        return toResponse(getOrCreate());
    }

    @Transactional
    public CartResponse addItem(AddCartItemRequest request) {
        Cart cart = getOrCreate();
        Book book = bookService.findById(request.bookId());
        if (book.getStockQuantity() < request.quantity()) {
            throw new BusinessException("Insufficient stock for book: " + book.getId());
        }
        CartItem existing = cartItemRepository
                .findByCartIdAndBookId(cart.getId(), book.getId()).orElse(null);
        if (existing != null) {
            int newQty = existing.getQuantity() + request.quantity();
            if (book.getStockQuantity() < newQty) {
                throw new BusinessException("Insufficient stock for book: " + book.getId());
            }
            existing.setQuantity(newQty);
            cartItemRepository.save(existing);
        } else {
            CartItem item = CartItem.builder()
                    .cart(cart)
                    .book(book)
                    .quantity(request.quantity())
                    .unitPrice(book.getPrice())
                    .build();
            cart.getItems().add(cartItemRepository.save(item));
        }
        return toResponse(cartRepository.save(cart));
    }

    @Transactional
    public CartResponse updateItem(UUID itemId, UpdateCartItemRequest request) {
        Cart cart = getOrCreate();
        CartItem item = cartItemRepository.findByIdAndCartId(itemId, cart.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found: " + itemId));
        Book book = item.getBook();
        if (book.getStockQuantity() < request.quantity()) {
            throw new BusinessException("Insufficient stock for book: " + book.getId());
        }
        item.setQuantity(request.quantity());
        cartItemRepository.save(item);
        return toResponse(cartRepository.save(cart));
    }

    @Transactional
    public CartResponse removeItem(UUID itemId) {
        Cart cart = getOrCreate();
        CartItem item = cartItemRepository.findByIdAndCartId(itemId, cart.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found: " + itemId));
        cart.getItems().remove(item);
        cartItemRepository.delete(item);
        return toResponse(cartRepository.save(cart));
    }

    @Transactional
    public void clearCart() {
        Cart cart = getOrCreate();
        cart.getItems().clear();
        cartRepository.save(cart);
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    public Cart getOrCreate() {
        User user = securityUtils.getCurrentUser();
        return cartRepository.findByUser(user).orElseGet(() -> {
            Cart cart = Cart.builder().user(user).build();
            return cartRepository.save(cart);
        });
    }

    public CartResponse toResponse(Cart cart) {
        var items = cart.getItems().stream().map(this::toItemResponse).toList();
        return new CartResponse(cart.getId(), cart.getUser().getId(), items,
                items.size(), cart.getSubtotal(), cart.getUpdatedAt());
    }

    private CartItemResponse toItemResponse(CartItem ci) {
        BookResponse book = bookService.toResponse(ci.getBook());
        return new CartItemResponse(ci.getId(), ci.getCart().getId(), ci.getBook().getId(),
                book, ci.getQuantity(), ci.getUnitPrice(), ci.getSubtotal());
    }
}
