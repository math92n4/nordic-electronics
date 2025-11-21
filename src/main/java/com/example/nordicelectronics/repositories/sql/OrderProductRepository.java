package com.example.nordicelectronics.repositories.sql;

import com.example.nordicelectronics.entity.OrderProduct;
import com.example.nordicelectronics.entity.OrderProductKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderProductRepository extends JpaRepository<OrderProduct, OrderProductKey> {
}

