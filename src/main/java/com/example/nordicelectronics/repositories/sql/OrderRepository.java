package com.example.nordicelectronics.repositories.sql;

import com.example.nordicelectronics.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {
}
