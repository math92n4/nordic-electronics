package com.example.nordicelectronics.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.example.nordicelectronics.entity.Coupon;
import com.example.nordicelectronics.service.CouponService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Coupon Controller", description = "Handles operations related to coupons")
@RestController
@RequestMapping("api/coupons")
public class CouponController {

    @Autowired
    public CouponService couponService;

    @Operation(summary = "Get coupons by order ID", description = "Fetches all coupons associated with a specific order ID.")
    @GetMapping("/get-by-order-id")
    public List<Coupon> getCouponsByOrderId(@RequestParam UUID orderId) {
        return couponService.getAllCouponsByOrderId(orderId);
    }

    @Operation(summary = "Get coupon by ID", description = "Fetches a coupon based on its unique ID.")
    @GetMapping("/get-by-id")
    public Coupon getCouponById(@RequestParam UUID couponId) {
        return couponService.getCouponById(couponId);
    }

    @Operation(summary = "Get active coupons", description = "Fetches all active coupons.")
    @GetMapping("/get-active")
    public List<Coupon> getActiveCoupons() {
        return couponService.getAllActiveCoupons();
    }

    @Operation(summary = "Get inactive coupons", description = "Fetches all inactive coupons.")
    @GetMapping("/get-inactive")
    public List<Coupon> getInactiveCoupons() {
        return couponService.getAllInactiveCoupons();
    }

    @Operation(summary = "Create a new coupon", description = "Creates a new coupon and returns the created coupon.")
    @PostMapping("/create")
    public Coupon createCoupon(@RequestBody Coupon coupon) {
        return couponService.save(coupon);
    }
}
