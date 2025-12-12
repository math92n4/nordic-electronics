package com.example.nordicelectronics.entity.validators;

import com.example.nordicelectronics.entity.enums.DiscountType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public final class CouponValidator {

    private CouponValidator() {}

    public static void validateCouponCode(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Coupon code cannot be null or empty");
        }

        if (code.length() < 3) {
            throw new IllegalArgumentException("Coupon code must be at least 3 characters long");
        }

        if (code.length() > 20) {
            throw new IllegalArgumentException("Coupon code must be at most 20 characters long");
        }
    }

    public static void validateDiscountValue(BigDecimal discountValue) {
        if (discountValue == null) {
            throw new IllegalArgumentException("Discount value cannot be null");
        }

        if (discountValue.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Discount value must be greater than 0");
        }
    }

    public static void validateMinimumOrderValue(BigDecimal minimumOrderValue) {
        if (minimumOrderValue == null) {
            throw new IllegalArgumentException("Minimum order value cannot be null");
        }

        if (minimumOrderValue.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Minimum order value cannot be negative");
        }
    }

    public static void validateDateTimeInRange(LocalDateTime startDateTime, LocalDateTime expiryDateTime, LocalDateTime checkDateTime) {
        if (startDateTime == null) {
            throw new IllegalArgumentException("Start datetime cannot be null");
        }
        if (expiryDateTime == null) {
            throw new IllegalArgumentException("Expiry datetime cannot be null");
        }
        if (checkDateTime == null) {
            throw new IllegalArgumentException("Check datetime cannot be null");
        }

        if (checkDateTime.isBefore(startDateTime)) {
            throw new IllegalArgumentException("Coupon is not yet valid (starts at " + startDateTime + ")");
        }

        if (checkDateTime.isAfter(expiryDateTime)) {
            throw new IllegalArgumentException("Coupon has expired (expired at " + expiryDateTime + ")");
        }
    }

    public static void validatePercentage(BigDecimal percentage) {
        if (percentage == null) {
            throw new IllegalArgumentException("Percentage cannot be null");
        }

        if (percentage.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Percentage cannot be negative");
        }

        if (percentage.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new IllegalArgumentException("Percentage cannot exceed 100");
        }
    }

    public static void validateNextUse(int usageLimit, int timesUsed) {
        if (usageLimit <= 0) {
            throw new IllegalArgumentException("Usage limit must be greater than 0");
        }

        if (timesUsed < 0) {
            throw new IllegalArgumentException("Times used cannot be negative");
        }

        if (timesUsed + 1 > usageLimit) {
            throw new IllegalArgumentException("Applying coupon would exceed usage limit (" + (timesUsed + 1) + "/" + usageLimit + ")");
        }
    }

    public static int remainingUses(int usageLimit, int timesUsed) {
        if (usageLimit <= 0 || timesUsed < 0) {
            return 0;
        }
        return Math.max(0, usageLimit - timesUsed);
    }
}