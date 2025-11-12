package com.example.nordicelectronics.repositories.sql;

import com.example.nordicelectronics.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CouponRepository extends JpaRepository<Coupon, UUID> {
    List<Coupon> findAllByIsActive(boolean isActive);
}