package com.example.nordicelectronics.unit.service;


import com.example.nordicelectronics.entity.Coupon;
import com.example.nordicelectronics.entity.enums.DiscountType;
import com.example.nordicelectronics.repositories.sql.CouponRepository;
import com.example.nordicelectronics.service.validation.CouponValidationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CouponValidationServiceTest {

    private CouponRepository couponRepository;
    private CouponValidationService couponValidationService;

    @BeforeEach
    void setUp() {
        couponRepository = mock(CouponRepository.class);
        couponValidationService = new CouponValidationService(couponRepository);
    }

    // ==============================
    // validateCoupon tests
    // ==============================

    @Test
    void validateCoupon_validCoupon_shouldReturnCoupon() {
        Coupon coupon = Coupon.builder()
                .code("DISCOUNT10")
                .isActive(true)
                .usageLimit(5)
                .timesUsed(2)
                .minimumOrderValue(BigDecimal.valueOf(50))
                .expiryDate(LocalDate.now().plusDays(1))
                .build();

        when(couponRepository.findByCode("DISCOUNT10")).thenReturn(Optional.of(coupon));

        Coupon result = couponValidationService.validateCoupon("DISCOUNT10", BigDecimal.valueOf(100));

        assertNotNull(result);
        assertEquals("DISCOUNT10", result.getCode());
    }

    @Test
    void validateCoupon_couponNotFound_shouldThrow() {
        when(couponRepository.findByCode("INVALID")).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> couponValidationService.validateCoupon("INVALID", BigDecimal.valueOf(100)));

        assertEquals("Invalid coupon code: INVALID", ex.getMessage());
    }

    @Test
    void validateCoupon_couponNotActive_shouldThrow() {
        Coupon coupon = Coupon.builder().isActive(false).build();
        when(couponRepository.findByCode("CODE")).thenReturn(Optional.of(coupon));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> couponValidationService.validateCoupon("CODE", BigDecimal.valueOf(100)));

        assertEquals("Coupon is not active", ex.getMessage());
    }

    @Test
    void validateCoupon_couponDeleted_shouldThrow() {
        Coupon coupon = new Coupon();
        coupon.setActive(true);
        coupon.setDeletedAt(LocalDate.now().atStartOfDay());
        when(couponRepository.findByCode("CODE")).thenReturn(Optional.of(coupon));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> couponValidationService.validateCoupon("CODE", BigDecimal.valueOf(100)));

        assertEquals("Coupon has been deleted", ex.getMessage());
    }

    @Test
    void validateCoupon_couponExpired_shouldThrow() {
        Coupon coupon = Coupon.builder()
                .isActive(true)
                .expiryDate(LocalDate.now().minusDays(1))
                .build();
        when(couponRepository.findByCode("CODE")).thenReturn(Optional.of(coupon));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> couponValidationService.validateCoupon("CODE", BigDecimal.valueOf(100)));

        assertEquals("Coupon has expired", ex.getMessage());
    }

    @Test
    void validateCoupon_usageLimitExceeded_shouldThrow() {
        Coupon coupon = Coupon.builder()
                .isActive(true)
                .usageLimit(1)
                .timesUsed(1)
                .build();
        when(couponRepository.findByCode("CODE")).thenReturn(Optional.of(coupon));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> couponValidationService.validateCoupon("CODE", BigDecimal.valueOf(100)));

        assertEquals("Coupon usage limit exceeded", ex.getMessage());
    }

    @Test
    void validateCoupon_minimumOrderValueNotMet_shouldThrow() {
        Coupon coupon = Coupon.builder()
                .isActive(true)
                .usageLimit(5)
                .timesUsed(0)
                .minimumOrderValue(BigDecimal.valueOf(200))
                .build();
        when(couponRepository.findByCode("CODE")).thenReturn(Optional.of(coupon));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> couponValidationService.validateCoupon("CODE", BigDecimal.valueOf(100)));

        assertTrue(ex.getMessage().contains("Order subtotal"));
    }

    // ==============================
    // calculateDiscount tests
    // ==============================

    @Test
    void calculateDiscount_percentageCoupon_shouldReturnCorrectAmount() {
        Coupon coupon = Coupon.builder()
                .discountType(DiscountType.percentage)
                .discountValue(BigDecimal.valueOf(10)) // 10%
                .build();

        BigDecimal subtotal = BigDecimal.valueOf(200);

        BigDecimal discount = couponValidationService.calculateDiscount(coupon, subtotal);

        assertEquals(BigDecimal.valueOf(20.00).setScale(2, RoundingMode.HALF_UP), discount.setScale(2,RoundingMode.HALF_UP));
    }

    @Test
    void calculateDiscount_fixedCoupon_shouldReturnCorrectAmount() {
        Coupon coupon = Coupon.builder()
                .discountType(DiscountType.fixed_amount)
                .discountValue(BigDecimal.valueOf(50))
                .build();

        BigDecimal subtotal = BigDecimal.valueOf(200);

        BigDecimal discount = couponValidationService.calculateDiscount(coupon, subtotal);

        assertEquals(BigDecimal.valueOf(50).setScale(2,RoundingMode.HALF_UP), discount.setScale(2,RoundingMode.HALF_UP));
    }

    @Test
    void calculateDiscount_discountExceedsSubtotal_shouldCapDiscount() {
        Coupon coupon = Coupon.builder()
                .discountType(DiscountType.fixed_amount)
                .discountValue(BigDecimal.valueOf(300))
                .build();

        BigDecimal subtotal = BigDecimal.valueOf(200);

        BigDecimal discount = couponValidationService.calculateDiscount(coupon, subtotal);

        assertEquals(subtotal.setScale(2, RoundingMode.HALF_UP), discount.setScale(2, RoundingMode.HALF_UP));
    }
}
