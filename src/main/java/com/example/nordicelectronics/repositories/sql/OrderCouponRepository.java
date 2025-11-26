package com.example.nordicelectronics.repositories.sql;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderCouponRepository extends JpaRepository<OrderCoupon, OrderCouponKey> {
}

