package com.example.nordicelectronics.entity.validators;

import com.example.nordicelectronics.entity.enums.DiscountType;
import jakarta.persistence.EnumType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Validation utilities for Coupon-related rules.
 *
 * Notes:
 * - Date-based checks have two overloads: LocalDate (date-only, inclusive) and LocalDateTime (time-aware, inclusive).
 * - Usage checks provide two semantics:
 *   - canBeUsed: checks whether coupon can be applied now based on current timesUsed (true when timesUsed &lt; usageLimit).
 *   - canBeUsedCountingNextUse: checks whether applying one more use would remain within the usageLimit (true when timesUsed + 1 &lt;= usageLimit).
     */
    public final class CouponValidator {

        private CouponValidator() {}

        /**
         * Checks if a Coupons generated UUID is valid or not.
         */
        public static boolean hasValidIdLength(String id) {
            if (id == null || id.isBlank()) return false;

            if (id.length() != 36) {
                return false;
            } else {
                return true;
            }
        }

        /**
         * Checks if a Coupons code length is valid or not.
         */
        public static boolean hasValidCouponCodeLength(String code) {
            if (code == null || code.isBlank()) return false;

            if (code.length() < 3 || code.length() > 20) {
                return false;
            } else {
                return true;
            }
        }

        /**
         * Checks if a Coupons discount type is valid or not.
         *
         */
        public static boolean isValidDiscountType(DiscountType discountType) {
            if (discountType == null) {
                return false;
            }
            return discountType == DiscountType.percentage || discountType == DiscountType.fixed_amount;
        }

        /**
         * Checks if a Coupons discount value is positive or not.
         * In other words, if it's valid or not.
         */
        public static boolean discountValueIsPositive(BigDecimal discountValue) {
            if (discountValue == null) return false;

            try {
                return discountValue.compareTo(BigDecimal.ZERO) > 0;
            } catch (ArithmeticException ex) {
                return false;
            }
        }

        /**
         * Checks if a Coupons minimum order value is positive or not.
         * In other words, if it's valid or not.
         */
        public static boolean minimumOrderValueIsPositive(BigDecimal minimumOrderValue) {
            if (minimumOrderValue == null) return false;

            try {
                return minimumOrderValue.compareTo(BigDecimal.ZERO) >= 0;
            } catch (ArithmeticException ex) {
                return false;
            }
        }

        /**
         * Checks if a Coupons expiry date is valid or not.
         */
        public static boolean isExpiryDateValid(LocalDate expiryDate) {
            if (expiryDate == null) return false; // we do not allow null expiry dates

            LocalDate today = LocalDate.now();
            return !expiryDate.isBefore(today); // Expiry date should be today or in the future
        }

        /**
         * Returns true when coupon can be applied now based on current timesUsed and usageLimit.
         * Semantics: valid when usageLimit &gt; 0, timesUsed &gt;= 0 and timesUsed &lt; usageLimit.
         * Example: usageLimit=50 -> timesUsed 0..49 allowed, 50 not allowed.
         */
        public static boolean canBeUsed(int usageLimit, int timesUsed) {
            if (usageLimit <= 0) return false;
            if (timesUsed < 0) return false;
            return timesUsed < usageLimit;
        }

    /**
     * Check date-only validity (both ends inclusive).
     *
     * Returns true when the supplied checkDate falls between startDate and expiryDate (inclusive).
     * Returns false if any argument is null.
     */
    public static boolean isValidOnDate(LocalDate startDate, LocalDate expiryDate, LocalDate checkDate) {
        if (startDate == null || expiryDate == null || checkDate == null) {
            return false;
        }

        boolean isBeforeStart = checkDate.isBefore(startDate);
        boolean isAfterExpiry = checkDate.isAfter(expiryDate);

        // valid when it's not before the start and not after the expiry
        return !isBeforeStart && !isAfterExpiry;
    }

    /**
     * Check datetime validity (both ends inclusive).
     *
     * Returns true when checkDateTime falls between startDateTime and expiryDateTime (inclusive).
     * Returns false if any argument is null.
     */
    public static boolean isValidAtTimeAndDate(LocalDateTime startDateTime, LocalDateTime expiryDateTime, LocalDateTime checkDateTime) {
        if (startDateTime == null || expiryDateTime == null || checkDateTime == null) {
            return false;
        }

        boolean isBeforeStart = checkDateTime.isBefore(startDateTime);
        boolean isAfterExpiry = checkDateTime.isAfter(expiryDateTime);

        // valid when it's not before the start and not after the expiry
        return !isBeforeStart && !isAfterExpiry;
    }

    /**
     * Discount percentage validity: 0..100 inclusive.
     */
    public static boolean isValidPercentage(BigDecimal percentage) {
        if (Objects.isNull(percentage)) return false;
        try {
            return percentage.compareTo(BigDecimal.ZERO) > 0 && percentage.compareTo(BigDecimal.valueOf(100)) <= 0;
        } catch (ArithmeticException ex) {
            return false;
        }
    }

    /**
     * Returns true when applying one more use would still be within limit.
     * Semantics: valid when usageLimit &gt;= 1 and timesUsed &gt;= 0 and timesUsed + 1 &lt;= usageLimit.
     * Example: usageLimit=50 -> timesUsed 0..49 will return true (because after use they'd be 1..50), timesUsed=50 returns false.
     */
    public static boolean canBeUsedCountingNextUse(int usageLimit, int timesUsed) {
        if (usageLimit <= 0) return false;
        if (timesUsed < 0) return false;
        return (long) timesUsed + 1L <= (long) usageLimit;
    }

    /**
     * Returns how many uses remain (>= 0). If usageLimit or timesUsed are invalid returns 0.
     */
    public static int remainingUses(int usageLimit, int timesUsed) {
        if (usageLimit <= 0 || timesUsed < 0) return 0;
        int remaining = usageLimit - timesUsed;
        return Math.max(0, remaining);
    }
}
