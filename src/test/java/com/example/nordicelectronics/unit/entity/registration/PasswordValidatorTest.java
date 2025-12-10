package com.example.nordicelectronics.unit.entity.registration;

import com.example.nordicelectronics.entity.validator.UserValidator.PasswordValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for PasswordValidator - Equivalence Partitioning (EP) and Boundary Value Analysis (BVA)
 * Converted from AuthControllerIT to enable faster test execution without database dependency.
 */
public class PasswordValidatorTest {

    // ============================================
    // BVA TESTS - Password Length Boundaries
    // (Migrated from AuthControllerIT)
    // ============================================

    @Nested
    @DisplayName("BVA: Password Length - Lower Boundary (8 chars)")
    class LowerBoundaryTests {

        @Test
        @DisplayName("BVA: Password with 7 characters - invalid (below minimum)")
        void bvaPassword7Chars_throws() {
            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> PasswordValidator.validatePassword("1234567")
            );
            assertTrue(ex.getMessage().contains("at least 8"));
        }

        @Test
        @DisplayName("BVA: Password with 8 characters - valid (at minimum)")
        void bvaPassword8Chars_doesNotThrow() {
            assertDoesNotThrow(() -> PasswordValidator.validatePassword("12345678"));
        }

        @Test
        @DisplayName("BVA: Password with 9 characters - valid (just above minimum)")
        void bvaPassword9Chars_doesNotThrow() {
            assertDoesNotThrow(() -> PasswordValidator.validatePassword("123456789"));
        }
    }

    @Nested
    @DisplayName("BVA: Password Length - Upper Boundary (64 chars)")
    class UpperBoundaryTests {

        @Test
        @DisplayName("BVA: Password with 63 characters - valid (just below maximum)")
        void bvaPassword63Chars_doesNotThrow() {
            assertDoesNotThrow(() -> PasswordValidator.validatePassword("a".repeat(63)));
        }

        @Test
        @DisplayName("BVA: Password with 64 characters - valid (at maximum)")
        void bvaPassword64Chars_doesNotThrow() {
            assertDoesNotThrow(() -> PasswordValidator.validatePassword("a".repeat(64)));
        }

        @Test
        @DisplayName("BVA: Password with 65 characters - invalid (above maximum)")
        void bvaPassword65Chars_throws() {
            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> PasswordValidator.validatePassword("a".repeat(65))
            );
            assertTrue(ex.getMessage().contains("64"));
        }
    }

    // ============================================
    // EP TESTS - Password Length Partitions
    // (Migrated from AuthControllerIT)
    // ============================================

    @Nested
    @DisplayName("EP: Password Length - Valid Range (8-64)")
    class ValidRangeTests {

        @ParameterizedTest
        @DisplayName("EP: Password in valid range (8-64)")
        @MethodSource("com.example.nordicelectronics.unit.entity.registration.PasswordValidatorTest#validPasswords")
        void validatePassword_validLengths_doesNotThrow(String password) {
            assertDoesNotThrow(() -> PasswordValidator.validatePassword(password));
        }

        @Test
        @DisplayName("EP: Password with 36 characters - valid (middle of range)")
        void epPassword36Chars_doesNotThrow() {
            assertDoesNotThrow(() -> PasswordValidator.validatePassword("a".repeat(36)));
        }

        @Test
        @DisplayName("EP: Password with typical length - valid")
        void epPasswordTypicalLength_doesNotThrow() {
            assertDoesNotThrow(() -> PasswordValidator.validatePassword("validPassword123"));
        }
    }

    @Nested
    @DisplayName("EP: Password Length - Below Valid Range (<8)")
    class BelowRangeTests {

        @ParameterizedTest
        @DisplayName("EP: Password below valid range (<8)")
        @ValueSource(strings = {
                "1234567",    // 7 chars
                "short",      // 5 chars
                "a",          // 1 char
                "abc"         // 3 chars
        })
        void validatePassword_tooShort_throws(String password) {
            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> PasswordValidator.validatePassword(password)
            );
            assertTrue(ex.getMessage().contains("at least"));
        }
    }

    @Nested
    @DisplayName("EP: Password Length - Above Valid Range (>64)")
    class AboveRangeTests {

        @ParameterizedTest
        @DisplayName("EP: Password above valid range (>64)")
        @MethodSource("com.example.nordicelectronics.unit.entity.registration.PasswordValidatorTest#invalidLongPasswords")
        void validatePassword_tooLong_throws(String password) {
            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> PasswordValidator.validatePassword(password)
            );
            assertTrue(ex.getMessage().contains("at most") || ex.getMessage().contains("64"));
        }

        @Test
        @DisplayName("EP: Password with 100 characters - invalid (well above maximum)")
        void epPassword100Chars_throws() {
            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> PasswordValidator.validatePassword("a".repeat(100))
            );
            assertTrue(ex.getMessage().contains("64") || ex.getMessage().contains("at most"));
        }
    }

    // ============================================
    // EP TESTS - Null and Blank Passwords
    // ============================================

    @Nested
    @DisplayName("EP: Password - Null or Blank")
    class NullOrBlankTests {

        @Test
        @DisplayName("EP: Null password - throws exception")
        void validatePassword_null_throws() {
            assertThrows(IllegalArgumentException.class,
                    () -> PasswordValidator.validatePassword(null));
        }

        @ParameterizedTest
        @DisplayName("EP: Blank/empty password - throws exception")
        @ValueSource(strings = {"", " ", "   "})
        void validatePassword_blank_throws(String password) {
            assertThrows(IllegalArgumentException.class,
                    () -> PasswordValidator.validatePassword(password));
        }
    }

    // ============================================
    // Test Data Providers
    // ============================================

    static Stream<String> validPasswords() {
        return Stream.of(
                "12345678",          // 8 chars - minimum
                "123456789",         // 9 chars
                "passwordpasswordpasswordpasswordpasswordpasswordpw", // ~50 chars
                "p".repeat(64)       // 64 chars - maximum
        );
    }

    static Stream<String> invalidLongPasswords() {
        return Stream.of(
                "p".repeat(65),
                "verylongpasswordverylongpasswordverylongpasswordverylongpassword123"
        );
    }
}
