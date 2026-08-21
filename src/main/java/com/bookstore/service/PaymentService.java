package com.bookstore.service;

import com.bookstore.dto.response.PaymentResponse;
import com.bookstore.entity.Payment;
import com.bookstore.entity.User;
import com.bookstore.exception.ResourceNotFoundException;
import com.bookstore.repository.PaymentRepository;
import com.bookstore.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final SecurityUtils securityUtils;

    public PaymentResponse getPaymentById(UUID id) {
        User user = securityUtils.getCurrentUser();
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found: " + id));
        // Only the order owner or admin may view the payment
        boolean isOwner = payment.getOrder().getUser().getId().equals(user.getId());
        boolean isAdmin = user.getRole().name().equals("ADMIN");
        if (!isOwner && !isAdmin) {
            throw new ResourceNotFoundException("Payment not found: " + id);
        }
        return toResponse(payment);
    }

    private PaymentResponse toResponse(Payment p) {
        return new PaymentResponse(p.getId(), p.getOrder().getId(), p.getStatus(),
                p.getMethod(), p.getAmount(), p.getCurrency(),
                p.getTransactionId(), p.getProcessedAt());
    }
}
