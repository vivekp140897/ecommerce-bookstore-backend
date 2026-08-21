package com.bookstore.repository;

import com.bookstore.entity.Order;
import com.bookstore.entity.User;
import com.bookstore.entity.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {
    Page<Order> findByUser(User user, Pageable pageable);
    Page<Order> findByUserAndStatus(User user, OrderStatus status, Pageable pageable);
    Optional<Order> findByIdAndUser(UUID id, User user);
}
