package com.example.nordicelectronics.repositories.sql;

import com.example.nordicelectronics.entity.Order;
import com.example.nordicelectronics.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {
    Optional<Order> findTopByUserOrderByCreatedAtDesc(User user);
}
