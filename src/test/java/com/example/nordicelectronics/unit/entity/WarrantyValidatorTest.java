package com.example.nordicelectronics.unit.entity;

import com.example.nordicelectronics.entity.validator.WarrantyValidator.WarrantyValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

public class WarrantyValidatorTest {

    // EP Tests
    @Test
    @DisplayName("EP: Valid warranty dates - should not throw")
    void validateWarrantyDates_validDates_doesNotThrow() {
        LocalDate start = LocalDate.of(2025, 1, 1);
        LocalDate end = LocalDate.of(2027, 1, 1);
        assertDoesNotThrow(() -> WarrantyValidator.validateWarrantyDates(start, end));
    }

    @Test
    @DisplayName("EP: Null startDate - throws exception")
    void validateWarrantyDates_nullStartDate_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> WarrantyValidator.validateWarrantyDates(null, LocalDate.now()));
    }

    @Test
    @DisplayName("EP: Null endDate - throws exception")
    void validateWarrantyDates_nullEndDate_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> WarrantyValidator.validateWarrantyDates(LocalDate.now(), null));
    }

    @Test
    @DisplayName("EP: EndDate before startDate - throws exception")
    void validateWarrantyDates_endBeforeStart_throws() {
        LocalDate start = LocalDate.of(2025, 1, 1);
        LocalDate end = LocalDate.of(2024, 12, 31);
        assertThrows(IllegalArgumentException.class,
                () -> WarrantyValidator.validateWarrantyDates(start, end));
    }

    // BVA Tests
    @Test
    @DisplayName("BVA: EndDate equals startDate - throws exception")
    void validateWarrantyDates_endEqualsStart_throws() {
        LocalDate date = LocalDate.of(2025, 1, 1);
        assertThrows(IllegalArgumentException.class,
                () -> WarrantyValidator.validateWarrantyDates(date, date));
    }

    @Test
    @DisplayName("BVA: EndDate is startDate + 1 day - valid (minimum warranty)")
    void validateWarrantyDates_endIsStartPlusOneDay_doesNotThrow() {
        LocalDate start = LocalDate.of(2025, 1, 1);
        LocalDate end = LocalDate.of(2025, 1, 2);
        assertDoesNotThrow(() -> WarrantyValidator.validateWarrantyDates(start, end));
    }

    @Test
    @DisplayName("BVA: Description is 1 character - valid")
    void validateDescription_oneCharacter_doesNotThrow() {
        assertDoesNotThrow(() -> WarrantyValidator.validateDescription("A"));
    }

    @Test
    @DisplayName("BVA: Description is 500 characters - valid")
    void validateDescription_500Characters_doesNotThrow() {
        String desc = "A".repeat(500);
        assertDoesNotThrow(() -> WarrantyValidator.validateDescription(desc));
    }

    @Test
    @DisplayName("BVA: Description is 501 characters - throws exception")
    void validateDescription_501Characters_throws() {
        String desc = "A".repeat(501);
        assertThrows(IllegalArgumentException.class,
                () -> WarrantyValidator.validateDescription(desc));
    }
}