package com.example.nordicelectronics.unit.entity.registration;

import com.example.nordicelectronics.entity.validator.UserValidator.DateOfBirthValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDate;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for DateOfBirthValidator - Equivalence Partitioning (EP) and Boundary Value Analysis (BVA)
 * Converted from AuthControllerIT to enable faster test execution without database dependency.
 */
public class DateOfBirthValidatorTest {

    // ============================================
    // BVA TESTS - Age Boundary (18 years)
    // (Migrated from AuthControllerIT)
    // ============================================

    @Nested
    @DisplayName("BVA: Age Boundary - 18 Years")
    class AgeBoundaryTests {

        @Test
        @DisplayName("BVA: User exactly 17 years old - invalid (below boundary)")
        void bvaAge17Years_throws() {
            LocalDate dob = LocalDate.now().minusYears(17);
            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> DateOfBirthValidator.validateDateOfBirth(dob)
            );
            assertTrue(ex.getMessage().contains("18"));
        }

        @Test
        @DisplayName("BVA: User exactly 18 years old - valid (at boundary)")
        void bvaAge18Years_doesNotThrow() {
            LocalDate dob = LocalDate.now().minusYears(18);
            assertDoesNotThrow(() -> DateOfBirthValidator.validateDateOfBirth(dob));
        }

        @Test
        @DisplayName("BVA: User exactly 19 years old - valid (just above boundary)")
        void bvaAge19Years_doesNotThrow() {
            LocalDate dob = LocalDate.now().minusYears(19);
            assertDoesNotThrow(() -> DateOfBirthValidator.validateDateOfBirth(dob));
        }

        @Test
        @DisplayName("BVA: User 17 years and 364 days old - invalid (1 day before boundary)")
        void bvaAge17Years364Days_throws() {
            LocalDate dob = LocalDate.now().minusYears(18).plusDays(1);
            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> DateOfBirthValidator.validateDateOfBirth(dob)
            );
            assertTrue(ex.getMessage().contains("18"));
        }
    }

    // ============================================
    // EP TESTS - Age Partitions
    // (Migrated from AuthControllerIT)
    // ============================================

    @Nested
    @DisplayName("EP: User Under 18 - Invalid")
    class Under18Tests {

        @Test
        @DisplayName("EP: User under 18 (10 years old) - invalid")
        void epAgeUnder18_throws() {
            LocalDate dob = LocalDate.now().minusYears(10);
            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> DateOfBirthValidator.validateDateOfBirth(dob)
            );
            assertTrue(ex.getMessage().contains("18"));
        }

        @ParameterizedTest
        @DisplayName("EP: Various ages under 18 - invalid")
        @MethodSource("com.example.nordicelectronics.unit.entity.registration.DateOfBirthValidatorTest#invalidDates")
        void validateDateOfBirth_invalidDates_throws(LocalDate dob) {
            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> DateOfBirthValidator.validateDateOfBirth(dob)
            );
            assertTrue(ex.getMessage().contains("at least 18"));
        }
    }

    @Nested
    @DisplayName("EP: User Over 18 - Valid")
    class Over18Tests {

        @Test
        @DisplayName("EP: User over 18 (30 years old) - valid")
        void epAgeOver18_doesNotThrow() {
            LocalDate dob = LocalDate.now().minusYears(30);
            assertDoesNotThrow(() -> DateOfBirthValidator.validateDateOfBirth(dob));
        }

        @ParameterizedTest
        @DisplayName("EP: Various valid ages (18+)")
        @MethodSource("com.example.nordicelectronics.unit.entity.registration.DateOfBirthValidatorTest#validDates")
        void validateDateOfBirth_validDates_doesNotThrow(LocalDate dob) {
            assertDoesNotThrow(() -> DateOfBirthValidator.validateDateOfBirth(dob));
        }

        @Test
        @DisplayName("EP: User much older (50 years old) - valid")
        void epAge50Years_doesNotThrow() {
            LocalDate dob = LocalDate.now().minusYears(50);
            assertDoesNotThrow(() -> DateOfBirthValidator.validateDateOfBirth(dob));
        }
    }

    // ============================================
    // EP TESTS - Null Input
    // ============================================

    @Nested
    @DisplayName("EP: Null Date of Birth")
    class NullTests {

        @Test
        @DisplayName("EP: Null date of birth - throws exception")
        void validateDateOfBirth_null_throws() {
            assertThrows(IllegalArgumentException.class,
                    () -> DateOfBirthValidator.validateDateOfBirth(null));
        }
    }

    // ============================================
    // Test Data Providers
    // ============================================

    static Stream<LocalDate> validDates() {
        LocalDate today = LocalDate.now();
        return Stream.of(
                today.minusYears(18),   // exactly 18
                today.minusYears(19),   // older than 18
                today.minusYears(50)    // much older
        );
    }

    static Stream<LocalDate> invalidDates() {
        LocalDate today = LocalDate.now();
        return Stream.of(
                today.minusYears(17),                // 17 years old
                today.minusYears(18).plusDays(1),    // 17y 364d
                today.minusYears(10)                 // too young
        );
    }
}
