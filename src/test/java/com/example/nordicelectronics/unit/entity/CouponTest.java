package com.example.nordicelectronics.unit.entity;

import com.example.nordicelectronics.entity.Coupon;
import com.example.nordicelectronics.entity.enums.DiscountType;
import com.example.nordicelectronics.entity.validators.CouponValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CouponTest {

    @Test
    void hasValidIdLength_shouldReturnTrue_forValidUUID() {
        UUID validUuid = UUID.randomUUID();
        String validUuidString = validUuid.toString();

        assertTrue(CouponValidator.hasValidIdLength(validUuidString));
        assertEquals(36, validUuidString.length()); // Confirm UUID string length is 36
    }

    @Test
    void hasValidIdLength_shouldReturnFalse_forNullAndBlankIds() {
        // BOUNDARY VALUE TESTING: Testing null and edge cases with real null values
        assertFalse(CouponValidator.hasValidIdLength(null));
        assertFalse(CouponValidator.hasValidIdLength(""));
        assertFalse(CouponValidator.hasValidIdLength("   "));
    }

    @Test
    void hasValidIdLength_shouldReturnFalse_forInvalidLengths() {
        // REAL STRING DATA: Testing actual string lengths, no stubs needed
        // Too short
        assertFalse(CouponValidator.hasValidIdLength("123")); // 3 chars
        assertFalse(CouponValidator.hasValidIdLength("12345678-1234-1234-1234-12345678901")); // 35 chars

        // Too long
        assertFalse(CouponValidator.hasValidIdLength("12345678-1234-1234-1234-1234567890123")); // 37 chars
        assertFalse(CouponValidator.hasValidIdLength("12345678-1234-1234-1234-1234567890123456")); // 41 chars
    }

    // ===== COUPON CODE VALIDATION TESTS =====
    // Uses PARAMETERIZED TESTING with real string data

    @ParameterizedTest
    @ValueSource(strings = {"ABC", "SALE20", "DISCOUNT", "WINTER2024SALE"})
    void hasValidCouponCodeLength_shouldReturnTrue_forValidCodes(String code) {
        // REAL DATA INPUT: JUnit provides real strings, no mocking required
        assertTrue(CouponValidator.hasValidCouponCodeLength(code));
        assertTrue(code.length() >= 3 && code.length() <= 20);
    }

    @Test
    void hasValidCouponCodeLength_shouldReturnFalse_forNullAndBlankCodes() {
        // BOUNDARY VALUE TESTING: Testing null and edge cases with real null values
        assertFalse(CouponValidator.hasValidCouponCodeLength(null));
        assertFalse(CouponValidator.hasValidCouponCodeLength(""));
        assertFalse(CouponValidator.hasValidCouponCodeLength("   "));
    }

    @Test
    void hasValidCouponCodeLength_shouldReturnFalse_forInvalidLengths() {
        // REAL STRING DATA: Testing actual string lengths, no stubs needed
        // Too short (< 3 chars)
        assertFalse(CouponValidator.hasValidCouponCodeLength("AB"));
        assertFalse(CouponValidator.hasValidCouponCodeLength("X"));

        // Too long (> 20 chars)
        assertFalse(CouponValidator.hasValidCouponCodeLength("VERYLONGCOUPONCODE123")); // 21 chars
        assertFalse(CouponValidator.hasValidCouponCodeLength("THISISSUPERLONGCOUPONCODE")); // 25 chars

        // Boundary values
        assertTrue(CouponValidator.hasValidCouponCodeLength("ABC")); // exactly 3 chars
        assertTrue(CouponValidator.hasValidCouponCodeLength("12345678901234567890")); // exactly 20 chars
    }

    // ===== DISCOUNT TYPE VALIDATION TESTS =====
    // Uses REAL ENUM VALUES - no mocking of enum types needed

    @Test
    void isValidDiscountType_shouldReturnTrue_forValidTypes() {
        // REAL ENUM DATA: Using actual enum constants from DiscountType
        assertTrue(CouponValidator.isValidDiscountType(DiscountType.percentage));
        assertTrue(CouponValidator.isValidDiscountType(DiscountType.fixed_amount));
    }

    @Test
    void isValidDiscountType_shouldReturnFalse_forNull() {
        // NULL VALUE TESTING: Testing real null input, no stub needed
        assertFalse(CouponValidator.isValidDiscountType(null));
    }

    // ===== DISCOUNT VALUE VALIDATION TESTS =====
    // Uses REAL BIGDECIMAL VALUES - no mocking of number types needed

    @Test
    void discountValueIsPositive_shouldReturnTrue_forPositiveValues() {
        // REAL BIGDECIMAL DATA: Testing with actual BigDecimal values
        assertTrue(CouponValidator.discountValueIsPositive(BigDecimal.valueOf(0.01)));
        assertTrue(CouponValidator.discountValueIsPositive(BigDecimal.valueOf(1)));
        assertTrue(CouponValidator.discountValueIsPositive(BigDecimal.valueOf(25.50)));
        assertTrue(CouponValidator.discountValueIsPositive(BigDecimal.valueOf(100)));
        assertTrue(CouponValidator.discountValueIsPositive(BigDecimal.valueOf(999.99)));
    }

    @Test
    void discountValueIsPositive_shouldReturnFalse_forZeroNegativeAndNull() {
        // BOUNDARY VALUE ANALYSIS: Testing edge cases with real data
        assertFalse(CouponValidator.discountValueIsPositive(null));
        assertFalse(CouponValidator.discountValueIsPositive(BigDecimal.ZERO));
        assertFalse(CouponValidator.discountValueIsPositive(BigDecimal.valueOf(-1)));
        assertFalse(CouponValidator.discountValueIsPositive(BigDecimal.valueOf(-0.01)));
    }

    // ===== MINIMUM ORDER VALUE VALIDATION TESTS =====
    // Uses REAL BIGDECIMAL VALUES - different business rules from discount value

    @Test
    void minimumOrderValueIsPositive_shouldReturnTrue_forZeroAndPositiveValues() {
        // REAL DATA: Minimum order value allows zero (free shipping threshold)
        assertTrue(CouponValidator.minimumOrderValueIsPositive(BigDecimal.ZERO));
        assertTrue(CouponValidator.minimumOrderValueIsPositive(BigDecimal.valueOf(0.01)));
        assertTrue(CouponValidator.minimumOrderValueIsPositive(BigDecimal.valueOf(50)));
        assertTrue(CouponValidator.minimumOrderValueIsPositive(BigDecimal.valueOf(999.99)));
    }

    @Test
    void minimumOrderValueIsPositive_shouldReturnFalse_forNegativeAndNull() {
        // BOUNDARY VALUE ANALYSIS: Testing invalid cases
        assertFalse(CouponValidator.minimumOrderValueIsPositive(null));
        assertFalse(CouponValidator.minimumOrderValueIsPositive(BigDecimal.valueOf(-1)));
        assertFalse(CouponValidator.minimumOrderValueIsPositive(BigDecimal.valueOf(-0.01)));
    }

    // ===== EXPIRY DATE VALIDATION TESTS (using today as reference) =====
    // ⚠️ NOTE: Uses LocalDate.now() - POTENTIAL CANDIDATE FOR TIME STUBBING
    // However, we test with fixed relative dates (today + X days) so it's deterministic

    @Test
    void isExpiryDateValid_shouldReturnTrue_forTodayAndFutureDates() {
        // REAL DATE COMPUTATION: Using actual LocalDate.now() and date arithmetic
        LocalDate today = LocalDate.now(); // December 1, 2025 (context provided)
        LocalDate tomorrow = today.plusDays(1);
        LocalDate nextWeek = today.plusDays(7);
        LocalDate nextYear = today.plusYears(1);

        // Testing with REAL computed dates, not mocked dates
        assertTrue(CouponValidator.isExpiryDateValid(today));
        assertTrue(CouponValidator.isExpiryDateValid(tomorrow));
        assertTrue(CouponValidator.isExpiryDateValid(nextWeek));
        assertTrue(CouponValidator.isExpiryDateValid(nextYear));
    }

    @Test
    void isExpiryDateValid_shouldReturnFalse_forPastDatesAndNull() {
        // REAL DATE COMPUTATION: Testing past dates relative to today
        LocalDate today = LocalDate.now(); // December 1, 2025
        LocalDate yesterday = today.minusDays(1);
        LocalDate lastWeek = today.minusDays(7);
        LocalDate lastYear = today.minusYears(1);

        assertFalse(CouponValidator.isExpiryDateValid(null));
        assertFalse(CouponValidator.isExpiryDateValid(yesterday));
        assertFalse(CouponValidator.isExpiryDateValid(lastWeek));
        assertFalse(CouponValidator.isExpiryDateValid(lastYear));
    }

    // ===== DATE RANGE VALIDATION TESTS (Business Scenario) =====
    // Uses FIXED DATE RANGES - no time dependencies, fully deterministic

    @Test
    void isValidOnDate_shouldFollowBusinessRules_forDateRanges() {
        // BUSINESS SCENARIO WITH FIXED DATES: 2025-11-26 to 2025-12-01
        // Uses real LocalDate.of() - no mocking needed for fixed dates
        LocalDate startDate = LocalDate.of(2025, 11, 26);
        LocalDate expiryDate = LocalDate.of(2025, 12, 1);

        // BOUNDARY VALUE ANALYSIS with real dates
        // BVA: Invalid (before start)
        assertFalse(CouponValidator.isValidOnDate(startDate, expiryDate, LocalDate.of(2025, 11, 25)));

        // BVA: Valid (on start date)
        assertTrue(CouponValidator.isValidOnDate(startDate, expiryDate, LocalDate.of(2025, 11, 26)));

        // EP: Valid (within range)
        assertTrue(CouponValidator.isValidOnDate(startDate, expiryDate, LocalDate.of(2025, 11, 30)));

        // BVA: Valid (on expiry date - inclusive)
        assertTrue(CouponValidator.isValidOnDate(startDate, expiryDate, LocalDate.of(2025, 12, 1)));

        // BVA: Invalid (after expiry)
        assertFalse(CouponValidator.isValidOnDate(startDate, expiryDate, LocalDate.of(2025, 12, 2)));
    }

    @Test
    void isValidOnDate_shouldReturnFalse_forNullInputs() {
        // NULL PARAMETER TESTING: Testing all null combinations
        LocalDate validDate = LocalDate.of(2025, 11, 26);

        assertFalse(CouponValidator.isValidOnDate(null, validDate, validDate));
        assertFalse(CouponValidator.isValidOnDate(validDate, null, validDate));
        assertFalse(CouponValidator.isValidOnDate(validDate, validDate, null));
        assertFalse(CouponValidator.isValidOnDate(null, null, null));
    }

    // ===== DATETIME RANGE VALIDATION TESTS =====
    // Uses FIXED DATETIME RANGES - testing time-precision boundaries

    @Test
    void isValidAtTimeAndDate_shouldFollowBusinessRules_forDateTimeRanges() {
        // BUSINESS SCENARIO WITH PRECISE TIMES: 2025-11-26T03:00 to 2025-12-01T03:00
        // Uses real LocalDateTime.of() - testing minute-level precision
        LocalDateTime startDateTime = LocalDateTime.of(2025, 11, 26, 3, 0);
        LocalDateTime expiryDateTime = LocalDateTime.of(2025, 12, 1, 3, 0);

        // TIME-BOUNDARY VALUE ANALYSIS: Testing minute-level precision
        // BVA: Invalid (1 minute before start)
        assertFalse(CouponValidator.isValidAtTimeAndDate(startDateTime, expiryDateTime,
            LocalDateTime.of(2025, 11, 26, 2, 59)));

        // BVA: Valid (exactly at start)
        assertTrue(CouponValidator.isValidAtTimeAndDate(startDateTime, expiryDateTime,
            LocalDateTime.of(2025, 11, 26, 3, 0)));

        // EP: Valid (within range)
        assertTrue(CouponValidator.isValidAtTimeAndDate(startDateTime, expiryDateTime,
            LocalDateTime.of(2025, 11, 30, 12, 0)));

        // BVA: Valid (exactly at expiry - inclusive)
        assertTrue(CouponValidator.isValidAtTimeAndDate(startDateTime, expiryDateTime,
            LocalDateTime.of(2025, 12, 1, 3, 0)));

        // BVA: Invalid (1 minute after expiry)
        assertFalse(CouponValidator.isValidAtTimeAndDate(startDateTime, expiryDateTime,
            LocalDateTime.of(2025, 12, 1, 3, 1)));
    }

    @Test
    void isValidAtTimeAndDate_shouldReturnFalse_forNullInputs() {
        // NULL PARAMETER TESTING for datetime methods
        LocalDateTime validDateTime = LocalDateTime.of(2025, 11, 26, 3, 0);

        assertFalse(CouponValidator.isValidAtTimeAndDate(null, validDateTime, validDateTime));
        assertFalse(CouponValidator.isValidAtTimeAndDate(validDateTime, null, validDateTime));
        assertFalse(CouponValidator.isValidAtTimeAndDate(validDateTime, validDateTime, null));
        assertFalse(CouponValidator.isValidAtTimeAndDate(null, null, null));
    }

    // ===== PERCENTAGE VALIDATION TESTS =====
    // Uses REAL PERCENTAGE VALUES - testing mathematical boundary conditions

    @Test
    void isValidPercentage_shouldReturnTrue_forValidRange() {
        // MATHEMATICAL BOUNDARY ANALYSIS: Testing percentage range 0-100
        // Note: Current implementation excludes 0 (> 0), but business rules suggest 0-100 inclusive

        // BVA: Valid percentages with real BigDecimal values
        assertTrue(CouponValidator.isValidPercentage(BigDecimal.valueOf(1))); // minimum valid (> 0)
        assertTrue(CouponValidator.isValidPercentage(BigDecimal.valueOf(50))); // middle range
        assertTrue(CouponValidator.isValidPercentage(BigDecimal.valueOf(99))); // near maximum
        assertTrue(CouponValidator.isValidPercentage(BigDecimal.valueOf(100))); // maximum

        // DECIMAL PRECISION TESTING: Real decimal percentages
        assertTrue(CouponValidator.isValidPercentage(BigDecimal.valueOf(25.5)));
        assertTrue(CouponValidator.isValidPercentage(BigDecimal.valueOf(99.99)));
    }

    @Test
    void isValidPercentage_shouldReturnFalse_forInvalidRange() {
        // BVA: Invalid percentage boundaries
        assertFalse(CouponValidator.isValidPercentage(null));
        assertFalse(CouponValidator.isValidPercentage(BigDecimal.valueOf(-1))); // negative
        assertFalse(CouponValidator.isValidPercentage(BigDecimal.valueOf(0))); // zero (current implementation)
        assertFalse(CouponValidator.isValidPercentage(BigDecimal.valueOf(101))); // over 100
        assertFalse(CouponValidator.isValidPercentage(BigDecimal.valueOf(150))); // way over 100
    }

    // ===== USAGE LIMIT VALIDATION TESTS =====
    // Uses REAL INTEGER VALUES - testing counting/limit logic

    @Test
    void canBeUsed_shouldReturnTrue_forValidUsage() {
        // BUSINESS LOGIC TESTING: Usage limit of 50 as specified in requirements
        int usageLimit = 50;

        // BVA: Valid usage scenarios with real integers
        assertTrue(CouponValidator.canBeUsed(usageLimit, 0)); // unused
        assertTrue(CouponValidator.canBeUsed(usageLimit, 1)); // barely used
        assertTrue(CouponValidator.canBeUsed(usageLimit, 25)); // half used
        assertTrue(CouponValidator.canBeUsed(usageLimit, 49)); // almost at limit

        // EDGE CASE TESTING: Different limit values
        assertTrue(CouponValidator.canBeUsed(1, 0)); // single use available
        assertTrue(CouponValidator.canBeUsed(100, 99)); // large limit, almost reached
    }

    @Test
    void canBeUsed_shouldReturnFalse_forInvalidUsage() {
        int usageLimit = 50;

        // BVA: Invalid usage scenarios per requirements
        assertFalse(CouponValidator.canBeUsed(usageLimit, -1)); // negative usage
        assertFalse(CouponValidator.canBeUsed(usageLimit, 50)); // exactly at limit
        assertFalse(CouponValidator.canBeUsed(usageLimit, 51)); // over limit
        assertFalse(CouponValidator.canBeUsed(usageLimit, 100)); // way over limit

        // INVALID INPUT TESTING: Invalid limits
        assertFalse(CouponValidator.canBeUsed(0, 0)); // zero limit
        assertFalse(CouponValidator.canBeUsed(-1, 0)); // negative limit
    }

    @Test
    void canBeUsedCountingNextUse_shouldReturnTrue_forValidNextUse() {
        // FORWARD-LOOKING VALIDATION: Testing if next use would be valid
        int usageLimit = 50;

        // BVA: Valid scenarios (after next use would still be within limit)
        assertTrue(CouponValidator.canBeUsedCountingNextUse(usageLimit, 0)); // 0 -> 1
        assertTrue(CouponValidator.canBeUsedCountingNextUse(usageLimit, 24)); // 24 -> 25
        assertTrue(CouponValidator.canBeUsedCountingNextUse(usageLimit, 49)); // 49 -> 50 (exactly at limit)

        // Edge cases
        assertTrue(CouponValidator.canBeUsedCountingNextUse(1, 0)); // 0 -> 1 with limit 1
    }

    @Test
    void canBeUsedCountingNextUse_shouldReturnFalse_forInvalidNextUse() {
        int usageLimit = 50;

        // BVA: Invalid scenarios - what would happen after next use
        assertFalse(CouponValidator.canBeUsedCountingNextUse(usageLimit, -1)); // negative current usage
        assertFalse(CouponValidator.canBeUsedCountingNextUse(usageLimit, 50)); // 50 -> 51 (would exceed)
        assertFalse(CouponValidator.canBeUsedCountingNextUse(usageLimit, 51)); // already over limit

        // Invalid limits
        assertFalse(CouponValidator.canBeUsedCountingNextUse(0, 0)); // zero limit
        assertFalse(CouponValidator.canBeUsedCountingNextUse(-1, 0)); // negative limit
    }

    @Test
    void remainingUses_shouldReturnCorrectCount() {
        // CALCULATION TESTING: Testing arithmetic logic with real integers
        int usageLimit = 50;

        // Valid scenarios - testing subtraction logic
        assertEquals(50, CouponValidator.remainingUses(usageLimit, 0)); // unused
        assertEquals(25, CouponValidator.remainingUses(usageLimit, 25)); // half used
        assertEquals(1, CouponValidator.remainingUses(usageLimit, 49)); // almost used up
        assertEquals(0, CouponValidator.remainingUses(usageLimit, 50)); // fully used
        assertEquals(0, CouponValidator.remainingUses(usageLimit, 51)); // over-used (capped at 0)

        // Invalid inputs should return 0
        assertEquals(0, CouponValidator.remainingUses(0, 0)); // zero limit
        assertEquals(0, CouponValidator.remainingUses(-1, 0)); // negative limit
        assertEquals(0, CouponValidator.remainingUses(usageLimit, -1)); // negative usage
    }

    // ===== INTEGRATION TESTS WITH COUPON ENTITY =====
    // Uses REAL COUPON OBJECTS - testing entity-validator integration

    @Test
    void couponEntity_shouldWorkWithValidator_forCompleteValidation() {
        // INTEGRATION TESTING: Creating real Coupon entity with valid data
        // Using constructor instead of builder to avoid potential Lombok issues
        Coupon validCoupon = new Coupon();
        validCoupon.setCouponId(UUID.randomUUID());
        validCoupon.setCode("WINTER25");
        validCoupon.setDiscountType(DiscountType.percentage);
        validCoupon.setDiscountValue(BigDecimal.valueOf(25));
        validCoupon.setMinimumOrderValue(BigDecimal.valueOf(100));
        validCoupon.setExpiryDate(LocalDate.of(2025, 12, 31));
        validCoupon.setUsageLimit(100);
        validCoupon.setTimesUsed(10);
        validCoupon.setActive(true);

        // COMPREHENSIVE VALIDATION: Testing all validator methods against real entity
        assertTrue(CouponValidator.hasValidIdLength(validCoupon.getCouponId().toString()));
        assertTrue(CouponValidator.hasValidCouponCodeLength(validCoupon.getCode()));
        assertTrue(CouponValidator.isValidDiscountType(validCoupon.getDiscountType()));
        assertTrue(CouponValidator.discountValueIsPositive(validCoupon.getDiscountValue()));
        assertTrue(CouponValidator.minimumOrderValueIsPositive(validCoupon.getMinimumOrderValue()));
        assertTrue(CouponValidator.isExpiryDateValid(validCoupon.getExpiryDate()));
        assertTrue(CouponValidator.canBeUsed(validCoupon.getUsageLimit(), validCoupon.getTimesUsed()));
        assertEquals(90, CouponValidator.remainingUses(validCoupon.getUsageLimit(), validCoupon.getTimesUsed()));
    }

    @Test
    void couponEntity_shouldFailValidation_forInvalidFields() {
        // NEGATIVE INTEGRATION TESTING: Creating invalid Coupon to test failure cases
        Coupon invalidCoupon = new Coupon();
        invalidCoupon.setCouponId(UUID.randomUUID());
        invalidCoupon.setCode("XY");  // INVALID: too short
        invalidCoupon.setDiscountType(DiscountType.percentage);
        invalidCoupon.setDiscountValue(BigDecimal.valueOf(-10)); // INVALID: negative
        invalidCoupon.setMinimumOrderValue(BigDecimal.valueOf(-5)); // INVALID: negative
        invalidCoupon.setExpiryDate(LocalDate.of(2024, 1, 1)); // INVALID: past date
        invalidCoupon.setUsageLimit(50);
        invalidCoupon.setTimesUsed(60); // INVALID: over limit
        invalidCoupon.setActive(false);

        // NEGATIVE VALIDATION: All these should fail
        assertFalse(CouponValidator.hasValidCouponCodeLength(invalidCoupon.getCode()));
        assertFalse(CouponValidator.discountValueIsPositive(invalidCoupon.getDiscountValue()));
        assertFalse(CouponValidator.minimumOrderValueIsPositive(invalidCoupon.getMinimumOrderValue()));
        assertFalse(CouponValidator.isExpiryDateValid(invalidCoupon.getExpiryDate()));
        assertFalse(CouponValidator.canBeUsed(invalidCoupon.getUsageLimit(), invalidCoupon.getTimesUsed()));
        assertEquals(0, CouponValidator.remainingUses(invalidCoupon.getUsageLimit(), invalidCoupon.getTimesUsed()));
    }
}
