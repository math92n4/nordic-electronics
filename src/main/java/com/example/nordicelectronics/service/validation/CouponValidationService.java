package com.example.nordicelectronics.service.validation;

import com.example.nordicelectronics.entity.Coupon;
import com.example.nordicelectronics.entity.enums.DiscountType;
import com.example.nordicelectronics.repositories.sql.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class CouponValidationService {

    private final CouponRepository couponRepository;

    /**
     * Validates a coupon code against business rules
     * @param couponCode The coupon code to validate
     * @param orderSubtotal The order subtotal to validate against
     * @return The validated Coupon entity
     * @throws IllegalArgumentException if validation fails
     */
    public Coupon validateCoupon(String couponCode, BigDecimal orderSubtotal) {
        // Find coupon by code
        Coupon coupon = couponRepository.findByCode(couponCode)
                .orElseThrow(() -> new IllegalArgumentException("Invalid coupon code: " + couponCode));

        // Check if active
        if (!coupon.isActive()) {
            throw new IllegalArgumentException("Coupon is not active");
        }

        // Check if deleted
        if (coupon.getDeletedAt() != null) {
            throw new IllegalArgumentException("Coupon has been deleted");
        }

        // Check expiry date
        if (coupon.getExpiryDate() != null && coupon.getExpiryDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Coupon has expired");
        }

        // Check usage limit
        if (coupon.getTimesUsed() >= coupon.getUsageLimit()) {
            throw new IllegalArgumentException("Coupon usage limit exceeded");
        }

        // Check minimum order value
        if (orderSubtotal.compareTo(coupon.getMinimumOrderValue()) < 0) {
            throw new IllegalArgumentException(
                    String.format("Order subtotal (%.2f) is below minimum required (%.2f)",
                            orderSubtotal, coupon.getMinimumOrderValue())
            );
        }

        return coupon;
    }

    /**
     * Calculates the discount amount based on coupon type
     * @param coupon The validated coupon
     * @param orderSubtotal The order subtotal
     * @return The calculated discount amount, capped at order subtotal
     */
    public BigDecimal calculateDiscount(Coupon coupon, BigDecimal orderSubtotal) {
        BigDecimal discount;

        if (coupon.getDiscountType() == DiscountType.percentage) {
            // Calculate percentage discount
            discount = orderSubtotal
                    .multiply(coupon.getDiscountValue())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        } else {
            // Fixed amount discount
            discount = coupon.getDiscountValue();
        }

        // Ensure discount doesn't exceed subtotal
        return discount.min(orderSubtotal);
    }
}