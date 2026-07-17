package com.ministore.order.repository;

import com.ministore.order.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserEmailOrderByCreatedAtDesc(String userEmail);
}
