package com.bookstore.service;

import com.bookstore.dto.request.ApplyDiscountRequest;
import com.bookstore.dto.request.CreateOrderRequest;
import com.bookstore.dto.response.DiscountResult;
import com.bookstore.dto.response.OrderResponse;
import com.bookstore.entity.*;
import com.bookstore.entity.enums.DiscountType;
import com.bookstore.entity.enums.OrderStatus;
import com.bookstore.entity.enums.PaymentMethod;
import com.bookstore.entity.enums.PaymentStatus;
import com.bookstore.exception.BusinessException;
import com.bookstore.exception.ResourceNotFoundException;
import com.bookstore.repository.AddressRepository;
import com.bookstore.repository.OrderRepository;
import com.bookstore.repository.PaymentRepository;
import com.bookstore.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CheckoutService {

    private static final BigDecimal SHIPPING_COST = new BigDecimal("5.99");
    private static final BigDecimal TAX_RATE      = new BigDecimal("0.08");

    private final CartService cartService;
    private final OrderService orderService;
    private final AddressRepository addressRepository;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final SecurityUtils securityUtils;

    public DiscountResult previewDiscount(ApplyDiscountRequest request) {
        User user = securityUtils.getCurrentUser();
        Cart cart = cartService.getOrCreate();
        if (cart.getItems().isEmpty()) {
            throw new BusinessException("Cart is empty.");
        }
        BigDecimal subtotal = cart.getSubtotal();
        return applyCoupon(request.couponCode(), subtotal);
    }

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        User user = securityUtils.getCurrentUser();
        Cart cart = cartService.getOrCreate();
        if (cart.getItems().isEmpty()) {
            throw new BusinessException("Cart is empty.");
        }

        // Validate stock and decrement
        for (CartItem ci : cart.getItems()) {
            Book book = ci.getBook();
            if (book.getStockQuantity() < ci.getQuantity()) {
                throw new BusinessException("Insufficient stock for: " + book.getTitle());
            }
            book.setStockQuantity(book.getStockQuantity() - ci.getQuantity());
        }

        Address shipping = findOwnedAddress(request.shippingAddressId(), user);
        Address billing = (request.billingAddressId() != null)
                ? findOwnedAddress(request.billingAddressId(), user)
                : shipping;

        BigDecimal subtotal = cart.getSubtotal();
        BigDecimal discountAmount = BigDecimal.ZERO;
        String couponCode = null;

        if (request.couponCode() != null && !request.couponCode().isBlank()) {
            DiscountResult dr = applyCoupon(request.couponCode(), subtotal);
            discountAmount = dr.discountAmount();
            couponCode = dr.couponCode();
        }

        BigDecimal afterDiscount = subtotal.subtract(discountAmount);
        BigDecimal tax = afterDiscount.multiply(TAX_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = afterDiscount.add(SHIPPING_COST).add(tax);

        Order order = Order.builder()
                .user(user)
                .shippingAddress(shipping)
                .billingAddress(billing)
                .couponCode(couponCode)
                .subtotal(subtotal)
                .discountAmount(discountAmount)
                .shippingCost(SHIPPING_COST)
                .tax(tax)
                .total(total)
                .build();

        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItem ci : cart.getItems()) {
            OrderItem oi = OrderItem.builder()
                    .order(order)
                    .book(ci.getBook())
                    .quantity(ci.getQuantity())
                    .unitPrice(ci.getUnitPrice())
                    .build();
            orderItems.add(oi);
        }
        order.getItems().addAll(orderItems);

        Order saved = orderRepository.save(order);

        // Process payment (stub — integrates with external payment gateway)
        Payment payment = Payment.builder()
                .order(saved)
                .method(resolvePaymentMethod(request.paymentMethodToken()))
                .amount(total)
                .status(PaymentStatus.CAPTURED)
                .transactionId("txn_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16))
                .processedAt(Instant.now())
                .build();
        paymentRepository.save(payment);
        saved.setPayment(payment);

        // Clear cart
        cartService.clearCart();

        return orderService.toResponse(saved);
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private DiscountResult applyCoupon(String code, BigDecimal subtotal) {
        // Stub implementation — replace with a real Coupon entity/repository
        if ("SUMMER20".equalsIgnoreCase(code)) {
            BigDecimal discountAmount = subtotal.multiply(new BigDecimal("0.20"))
                    .setScale(2, RoundingMode.HALF_UP);
            return new DiscountResult(code, DiscountType.PERCENTAGE, new BigDecimal("20.00"),
                    discountAmount, subtotal.subtract(discountAmount));
        }
        throw new BusinessException("Invalid or expired coupon: " + code);
    }

    private Address findOwnedAddress(UUID addressId, User user) {
        return addressRepository.findByIdAndUser(addressId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found: " + addressId));
    }

    private PaymentMethod resolvePaymentMethod(String token) {
        // In a real implementation inspect the token prefix / type from the payment SDK
        return PaymentMethod.CARD;
    }
}
