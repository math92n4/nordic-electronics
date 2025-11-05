package com.example.nordicelectronics.repositories.sql;

import com.example.nordicelectronics.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CouponRepository extends JpaRepository<Coupon, UUID> {
}
