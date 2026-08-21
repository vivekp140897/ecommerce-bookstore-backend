package com.bookstore.service;

import com.bookstore.dto.response.*;
import com.bookstore.entity.Order;
import com.bookstore.entity.OrderItem;
import com.bookstore.entity.Payment;
import com.bookstore.entity.User;
import com.bookstore.entity.enums.OrderStatus;
import com.bookstore.exception.BusinessException;
import com.bookstore.exception.ResourceNotFoundException;
import com.bookstore.repository.OrderRepository;
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
public class OrderService {

    private final OrderRepository orderRepository;
    private final SecurityUtils securityUtils;
    private final BookService bookService;
    private final AddressService addressService;

    public PageResponse<OrderResponse> listOrders(OrderStatus status, int page, int pageSize) {
        User user = securityUtils.getCurrentUser();
        var pageable = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "placedAt"));
        Page<Order> result = (status != null)
                ? orderRepository.findByUserAndStatus(user, status, pageable)
                : orderRepository.findByUser(user, pageable);
        return new PageResponse<>(
                result.getContent().stream().map(this::toResponse).toList(),
                new PageResponse.PaginationMeta(result.getNumber() + 1, result.getSize(),
                        result.getTotalElements(), result.getTotalPages()));
    }

    public OrderResponse getOrderById(UUID id) {
        User user = securityUtils.getCurrentUser();
        Order order = orderRepository.findByIdAndUser(id, user)
                .orElseGet(() -> {
                    // Admin can access any order
                    if (user.getRole().name().equals("ADMIN")) {
                        return orderRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + id));
                    }
                    throw new ResourceNotFoundException("Order not found: " + id);
                });
        return toResponse(order);
    }

    @Transactional
    public OrderResponse confirmOrder(UUID id) {
        Order order = findById(id);
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BusinessException("Order is not in PENDING state.");
        }
        order.setStatus(OrderStatus.CONFIRMED);
        return toResponse(orderRepository.save(order));
    }

    @Transactional
    public OrderResponse cancelOrder(UUID id) {
        User user = securityUtils.getCurrentUser();
        Order order = orderRepository.findByIdAndUser(id, user)
                .orElseGet(() -> {
                    if (user.getRole().name().equals("ADMIN")) {
                        return findById(id);
                    }
                    throw new ResourceNotFoundException("Order not found: " + id);
                });
        if (order.getStatus() == OrderStatus.SHIPPED
                || order.getStatus() == OrderStatus.DELIVERED) {
            if (!user.getRole().name().equals("ADMIN")) {
                throw new BusinessException("Cannot cancel an order that has already shipped.");
            }
        }
        if (order.getStatus() == OrderStatus.CANCELLED
                || order.getStatus() == OrderStatus.REFUNDED) {
            throw new BusinessException("Order is already " + order.getStatus());
        }
        order.setStatus(OrderStatus.CANCELLED);
        return toResponse(orderRepository.save(order));
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    public Order findById(UUID id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + id));
    }

    public OrderResponse toResponse(Order o) {
        var items = o.getItems().stream().map(this::toItemResponse).toList();
        PaymentResponse paymentResponse = o.getPayment() != null
                ? toPaymentResponse(o.getPayment()) : null;
        return new OrderResponse(o.getId(), o.getUser().getId(), o.getStatus(),
                items, addressService.toResponse(o.getShippingAddress()),
                o.getBillingAddress() != null ? addressService.toResponse(o.getBillingAddress()) : null,
                o.getCouponCode(), o.getSubtotal(), o.getDiscountAmount(),
                o.getShippingCost(), o.getTax(), o.getTotal(),
                paymentResponse, o.getPlacedAt(), o.getUpdatedAt());
    }

    private OrderItemResponse toItemResponse(OrderItem oi) {
        return new OrderItemResponse(oi.getId(), oi.getOrder().getId(), oi.getBook().getId(),
                bookService.toResponse(oi.getBook()), oi.getQuantity(),
                oi.getUnitPrice(), oi.getSubtotal());
    }

    private PaymentResponse toPaymentResponse(Payment p) {
        return new PaymentResponse(p.getId(), p.getOrder().getId(), p.getStatus(),
                p.getMethod(), p.getAmount(), p.getCurrency(),
                p.getTransactionId(), p.getProcessedAt());
    }
}
