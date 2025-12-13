package com.example.nordicelectronics.unit.entity;

import com.example.nordicelectronics.entity.validators.CouponValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class CouponValidatorTest {

    @ParameterizedTest
    @DisplayName("EP: Coupon code in valid range (3-20)")
    @MethodSource("validCouponCodes")
    void validateCouponCode_validLengths_doesNotThrow(String code) {
        assertDoesNotThrow(() -> CouponValidator.validateCouponCode(code));
    }

    @ParameterizedTest
    @DisplayName("EP: Coupon code in invalid range (<3 || >20)")
    @MethodSource("invalidCouponCodes")
    void validateCouponCode_invalidRange_throws(String code) {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> CouponValidator.validateCouponCode(code)
        );
        assertTrue(
                ex.getMessage().contains("at least 3 characters") ||
                        ex.getMessage().contains("at most 20 characters")
        );
    }

    @Test
    @DisplayName("EP: Null coupon code - throws exception")
    void validateCouponCode_null_throws() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> CouponValidator.validateCouponCode(null)
        );
        assertTrue(ex.getMessage().contains("Coupon code cannot be null or empty"));
    }

    @Test
    @DisplayName("EP: Blank coupon code - throws exception")
    void validateCouponCode_blank_throws() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> CouponValidator.validateCouponCode("   ")
        );
        assertTrue(ex.getMessage().contains("cannot be null or empty"));
    }

    @ParameterizedTest
    @DisplayName("EP: Valid discount values - does not throw")
    @ValueSource(strings = {"0.01", "1", "50.00", "99.99", "100"})
    void validateDiscountValue_positive_doesNotThrow(String value) {
        assertDoesNotThrow(() -> CouponValidator.validateDiscountValue(new BigDecimal(value)));
    }

    @ParameterizedTest
    @DisplayName("EP: Invalid discount values - throws exception")
    @ValueSource(strings = {"0", "-0.01", "-1", "-100"})
    void validateDiscountValue_nonPositive_throws(String value) {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> CouponValidator.validateDiscountValue(new BigDecimal(value))
        );
        assertTrue(ex.getMessage().contains("Discount value must be greater than 0") || ex.getMessage().contains("Discount value must be less than 100"));
    }

    @Test
    @DisplayName("EP: Null discount value - throws exception")
    void validateDiscountValue_null_throws() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> CouponValidator.validateDiscountValue(null)
        );
        assertTrue(ex.getMessage().contains("cannot be null"));
    }

    @ParameterizedTest
    @DisplayName("EP: Valid minimum order values - does not throw")
    @ValueSource(strings = {"0", "0.01", "50", "999.99"})
    void validateMinimumOrderValue_nonNegative_doesNotThrow(String value) {
        assertDoesNotThrow(() -> CouponValidator.validateMinimumOrderValue(new BigDecimal(value)));
    }

    @ParameterizedTest
    @DisplayName("EP: Negative minimum order values - throws exception")
    @ValueSource(strings = {"-0.01", "-1", "-100"})
    void validateMinimumOrderValue_negative_throws(String value) {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> CouponValidator.validateMinimumOrderValue(new BigDecimal(value))
        );
        assertTrue(ex.getMessage().contains("cannot be negative"));
    }

    @Test
    @DisplayName("EP: Null minimum order value - throws exception")
    void validateMinimumOrderValue_null_throws() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> CouponValidator.validateMinimumOrderValue(null)
        );
        assertTrue(ex.getMessage().contains("cannot be null"));
    }

    @ParameterizedTest
    @DisplayName("EP & BVA: DateTime within valid range (inclusive) - does not throw")
    @MethodSource("validDateTimes")
    void validateDateTimeInRange_withinRange_doesNotThrow(LocalDateTime checkDateTime) {
        LocalDateTime start = LocalDateTime.of(2025, 11, 26, 3, 0);
        LocalDateTime expiry = LocalDateTime.of(2025, 12, 1, 3, 0);

        assertDoesNotThrow(() -> CouponValidator.validateDateTimeInRange(start, expiry, checkDateTime));
    }

    @ParameterizedTest
    @DisplayName("EP & BVA: DateTime outside valid range - throws exception")
    @MethodSource("invalidDateTimes")
    void validateDateTimeInRange_outsideRange_throws(LocalDateTime checkDateTime) {
        LocalDateTime start = LocalDateTime.of(2025, 11, 26, 3, 0);
        LocalDateTime expiry = LocalDateTime.of(2025, 12, 1, 3, 0);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> CouponValidator.validateDateTimeInRange(start, expiry, checkDateTime)
        );
        assertTrue(
                ex.getMessage().contains("not yet valid") ||
                        ex.getMessage().contains("has expired")
        );
    }

    @Test
    @DisplayName("EP: Null datetime parameters - throws exception")
    void validateDateTimeInRange_nullParams_throws() {
        LocalDateTime validDateTime = LocalDateTime.of(2025, 11, 26, 3, 0);

        assertThrows(IllegalArgumentException.class,
                () -> CouponValidator.validateDateTimeInRange(null, validDateTime, validDateTime));
        assertThrows(IllegalArgumentException.class,
                () -> CouponValidator.validateDateTimeInRange(validDateTime, null, validDateTime));
        assertThrows(IllegalArgumentException.class,
                () -> CouponValidator.validateDateTimeInRange(validDateTime, validDateTime, null));
    }

    @ParameterizedTest
    @DisplayName("EP & BVA: Valid percentage range (0-100) - does not throw")
    @MethodSource("validPercentages")
    void validatePercentage_validRange_doesNotThrow(BigDecimal percentage) {
        assertDoesNotThrow(() -> CouponValidator.validatePercentage(percentage));
    }

    @ParameterizedTest
    @DisplayName("EP & BVA: Invalid percentage range (<0 || >100) - throws exception")
    @MethodSource("invalidPercentages")
    void validatePercentage_invalidRange_throws(BigDecimal percentage) {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> CouponValidator.validatePercentage(percentage)
        );
        assertTrue(
                ex.getMessage().contains("cannot be negative") ||
                        ex.getMessage().contains("cannot exceed 100")
        );
    }

    @Test
    @DisplayName("EP: Null percentage - throws exception")
    void validatePercentage_null_throws() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> CouponValidator.validatePercentage(null)
        );
        assertTrue(ex.getMessage().contains("cannot be null"));
    }

    @ParameterizedTest
    @DisplayName("EP & BVA: Next use would be within limit - does not throw")
    @ValueSource(ints = {0, 1, 24, 49})
    void validateNextUse_withinLimit_doesNotThrow(int timesUsed) {
        int usageLimit = 50;
        assertDoesNotThrow(() -> CouponValidator.validateNextUse(usageLimit, timesUsed));
    }

    @ParameterizedTest
    @DisplayName("EP & BVA: Next use would exceed limit - throws exception")
    @ValueSource(ints = {50, 51, 100})
    void validateNextUse_wouldExceedLimit_throws(int timesUsed) {
        int usageLimit = 50;
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> CouponValidator.validateNextUse(usageLimit, timesUsed)
        );
        assertTrue(ex.getMessage().contains("would exceed usage limit"));
    }

    @Test
    @DisplayName("EP: Negative times used for next use - throws exception")
    void validateNextUse_negativeTimesUsed_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> CouponValidator.validateNextUse(50, -1));
    }

    @Test
    @DisplayName("Remaining uses calculation - valid scenarios")
    void remainingUses_validScenarios_returnsCorrectCount() {
        int usageLimit = 50;

        assertEquals(50, CouponValidator.remainingUses(usageLimit, 0));
        assertEquals(25, CouponValidator.remainingUses(usageLimit, 25));
        assertEquals(1, CouponValidator.remainingUses(usageLimit, 49));
        assertEquals(0, CouponValidator.remainingUses(usageLimit, 50));
        assertEquals(0, CouponValidator.remainingUses(usageLimit, 51)); // over-used, capped at 0
    }

    @Test
    @DisplayName("Remaining uses calculation - invalid inputs return 0")
    void remainingUses_invalidInputs_returnsZero() {
        assertEquals(0, CouponValidator.remainingUses(0, 0));
        assertEquals(0, CouponValidator.remainingUses(-1, 0));
        assertEquals(0, CouponValidator.remainingUses(50, -1));
    }

    static Stream<String> validCouponCodes() {
        return Stream.of(
                "ABC",              // 3 chars (minimum)
                "SALE",             // 4 chars
                "p".repeat(10),       // 10 chars (middle)
                "p".repeat(19), // 19 chars
                "p".repeat(20) // 20 chars (maximum)
        );
    }

    static Stream<String> invalidCouponCodes() {
        return Stream.of(
                "A",                           // 1 char
                "AB",                          // 2 chars
                "p".repeat(21),        // 21 chars
                "p".repeat(50)                 // 50 chars
        );
    }

    static Stream<LocalDateTime> validDateTimes() {
        // Start:  2025-11-26T03:00:00
        // Expiry: 2025-12-01T03:00:00
        return Stream.of(
                LocalDateTime.of(2025, 11, 26, 3, 0, 0),     // BVA: Exact start
                LocalDateTime.of(2025, 11, 28, 15, 30),       // EP: Middle of valid period
                LocalDateTime.of(2025, 12, 1, 2, 59, 59),    // BVA: 1 second before expiry
                LocalDateTime.of(2025, 12, 1, 3, 0, 0)       // BVA: Exact expiry (inclusive)
        );
    }

    static Stream<LocalDateTime> invalidDateTimes() {
        return Stream.of(
                LocalDateTime.of(2025, 11, 26, 2, 59, 59),   // BVA: 1 second before start
                LocalDateTime.of(2025, 11, 25, 0, 0),         // EP: Before start date
                LocalDateTime.of(2025, 12, 1, 3, 0, 1),      // BVA: 1 second after expiry
                LocalDateTime.of(2025, 12, 5, 0, 0)           // EP: After expiry date
        );
    }

    static Stream<BigDecimal> validPercentages() {
        return Stream.of(
                BigDecimal.valueOf(0),      // BVA: Minimum (0%)
                BigDecimal.valueOf(1),      // BVA: Just above minimum
                BigDecimal.valueOf(50),     // EP: Middle range
                BigDecimal.valueOf(99),     // BVA: Just below maximum
                BigDecimal.valueOf(100),    // BVA: Maximum (100%)
                BigDecimal.valueOf(25.5),   // EP: Decimal value
                BigDecimal.valueOf(99.99)   // EP: High decimal value
        );
    }

    static Stream<BigDecimal> invalidPercentages() {
        return Stream.of(
                BigDecimal.valueOf(-1),     // BVA: Just below minimum
                BigDecimal.valueOf(-100),   // EP: Negative
                BigDecimal.valueOf(101),    // BVA: Just above maximum
                BigDecimal.valueOf(150)     // EP: Way above maximum
        );
    }
}