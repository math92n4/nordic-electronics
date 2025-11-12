package com.example.nordicelectronics.controller;

import com.example.nordicelectronics.entity.Coupon;
import com.example.nordicelectronics.service.CouponService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api/coupons")
public class CouponController {

    @Autowired
    public CouponService couponService;

    @GetMapping("/get-by-order-id")
    public List<Coupon> getCouponsByOrderId(@RequestParam UUID orderId) {
        return couponService.getAllCouponsByOrderId(orderId);
    }

    @GetMapping("/get-by-id")
    public Coupon getCouponById(@RequestParam UUID couponId) {
        return couponService.getCouponById(couponId);
    }

    @GetMapping("/get-active")
    public List<Coupon> getActiveCoupons() {
        return couponService.getAllActiveCoupons();
    }

    @GetMapping("/get-inactive")
    public List<Coupon> getInactiveCoupons() {
        return couponService.getAllInactiveCoupons();
    }

    @PostMapping("/create")
    public Coupon createCoupon(@RequestBody Coupon coupon) {
        return couponService.save(coupon);
    }


}
