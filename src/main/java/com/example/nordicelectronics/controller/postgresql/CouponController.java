package com.example.nordicelectronics.controller.postgresql;

import com.example.nordicelectronics.entity.Coupon;
import com.example.nordicelectronics.entity.dto.coupon.CouponRequestDTO;
import com.example.nordicelectronics.entity.dto.coupon.CouponResponseDTO;
import com.example.nordicelectronics.entity.dto.coupon.CouponValidationRequestDTO;
import com.example.nordicelectronics.service.CouponService;
import com.example.nordicelectronics.service.validation.CouponValidationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Tag(name = "PostgreSQL Coupon Controller", description = "Handles operations related to coupons in PostgreSQL")
@RestController
@RequiredArgsConstructor
@RequestMapping("api/postgresql/coupons")
public class CouponController {

    private final CouponService couponService;
    private final CouponValidationService couponValidationService;

    @Operation(summary = "Get PostgreSQL coupons by order ID", description = "Fetches all coupons associated with a specific order ID.")
    @GetMapping("/get-by-order-id")
    public ResponseEntity<List<CouponResponseDTO>> getCouponsByOrderId(@RequestParam UUID orderId) {
        return ResponseEntity.ok(couponService.getAllCouponsByOrderId(orderId));
    }

    @Operation(summary = "Get PostgreSQL coupon by ID", description = "Fetches a coupon based on its unique ID.")
    @GetMapping("/get-by-id")
    public ResponseEntity<CouponResponseDTO> getCouponById(@RequestParam UUID couponId) {
        return ResponseEntity.ok(couponService.getCouponById(couponId));
    }

    @Operation(summary = "Get active PostgreSQL coupons", description = "Fetches all active coupons.")
    @GetMapping("/get-active")
    public ResponseEntity<List<CouponResponseDTO>> getActiveCoupons() {
        return ResponseEntity.ok(couponService.getAllActiveCoupons());
    }

    @Operation(summary = "Get inactive PostgreSQL coupons", description = "Fetches all inactive coupons.")
    @GetMapping("/get-inactive")
    public ResponseEntity<List<CouponResponseDTO>> getInactiveCoupons() {
        return ResponseEntity.ok(couponService.getAllInactiveCoupons());
    }

    @Operation(summary = "Create a new PostgreSQL coupon", description = "Creates a new coupon and returns the created coupon.")
    @PostMapping("/create")
    public ResponseEntity<CouponResponseDTO> createCoupon(@RequestBody CouponRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(couponService.save(dto));
    }

    @Operation(summary = "Validate a coupon", description = "Validates a coupon and returns the total amount.")
    @PostMapping("/validate")
    public ResponseEntity<?> validateCoupon(@RequestBody CouponValidationRequestDTO dto) {
        Coupon coupon = couponValidationService.validateCoupon(dto.getCouponCode(), dto.getOrderSubtotal());
        BigDecimal discount;
        if (coupon != null) {
            discount = couponValidationService.calculateDiscount(coupon, dto.getOrderSubtotal());
            return ResponseEntity.ok(discount);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid coupon code or order subtotal");
    }
}
