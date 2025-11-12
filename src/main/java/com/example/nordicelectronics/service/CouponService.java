package com.example.nordicelectronics.service;

import com.example.nordicelectronics.entity.Coupon;
import com.example.nordicelectronics.repositories.sql.CouponRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class CouponService {

    @Autowired
    public CouponRepository couponRepository;

    public Coupon getCouponById(UUID couponId) {
        return couponRepository.findById(couponId).orElse(null);
    }

    public List<Coupon> getAllCouponsByOrderId(UUID orderId) {
        return couponRepository.findAllByOrderId(orderId);
    }

    public List<Coupon> getAllActiveCoupons() {
        return couponRepository.findAllByActive(true);
    }

    public List<Coupon> getAllInactiveCoupons() {
        return couponRepository.findAllByActive(false);
    }

    public Coupon save(Coupon coupon) {
        return couponRepository.save(coupon);
    }

}
