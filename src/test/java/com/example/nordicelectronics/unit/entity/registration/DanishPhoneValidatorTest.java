package com.example.nordicelectronics.unit.entity.registration;

import com.example.nordicelectronics.entity.validator.UserValidator.DanishPhoneValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for DanishPhoneValidator - Equivalence Partitioning (EP) and Boundary Value Analysis (BVA)
 * Converted from AuthControllerIT to enable faster test execution without database dependency.
 */
public class DanishPhoneValidatorTest {

    // ============================================
    // EP TESTS - Valid Danish Phone Numbers
    // (Migrated from AuthControllerIT)
    // ============================================

    @Nested
    @DisplayName("EP: Valid Phone - 8 digits with valid Danish prefix")
    class ValidPhoneTests {

        @ParameterizedTest
        @DisplayName("EP: Valid phone - various valid Danish mobile prefixes")
        @ValueSource(strings = {
                "20123456", // prefix "2" - valid
                "30123456", // prefix "30" - valid
                "34212345", // prefix "342" - valid
                "40123456", // prefix "40" - valid
                "78912345", // prefix "789" - valid
                "82912345"  // prefix "829" - valid
        })
        void isValidDanishMobile_validNumbers_returnsTrue(String phone) {
            assertTrue(DanishPhoneValidator.isValidDanishMobile(phone));
        }

        @Test
        @DisplayName("EP: Valid phone - prefix 30 (valid Danish mobile)")
        void epValidPhonePrefix30() {
            assertTrue(DanishPhoneValidator.isValidDanishMobile("30123456"));
        }

        @Test
        @DisplayName("EP: Valid phone - prefix 40 (valid Danish mobile)")
        void epValidPhonePrefix40() {
            assertTrue(DanishPhoneValidator.isValidDanishMobile("40123456"));
        }
    }

    // ============================================
    // EP TESTS - Invalid Phone - Wrong Length
    // (Migrated from AuthControllerIT)
    // ============================================

    @Nested
    @DisplayName("EP: Invalid Phone - Wrong Length")
    class InvalidLengthTests {

        @Test
        @DisplayName("EP: Invalid phone - only 7 digits")
        void epInvalidPhone7Digits() {
            assertFalse(DanishPhoneValidator.isValidDanishMobile("2012345"));
        }

        @Test
        @DisplayName("EP: Invalid phone - 9 digits")
        void epInvalidPhone9Digits() {
            assertFalse(DanishPhoneValidator.isValidDanishMobile("201234567"));
        }

        @ParameterizedTest
        @DisplayName("EP: Invalid phone - various wrong lengths")
        @ValueSource(strings = {
                "1234567",   // 7 digits
                "123456789", // 9 digits
                "1234"       // 4 digits
        })
        void isValidDanishMobile_invalidLength_returnsFalse(String phone) {
            assertFalse(DanishPhoneValidator.isValidDanishMobile(phone));
        }
    }

    // ============================================
    // EP TESTS - Invalid Phone - Invalid Prefix
    // (Migrated from AuthControllerIT)
    // ============================================

    @Nested
    @DisplayName("EP: Invalid Phone - Invalid Danish Prefix")
    class InvalidPrefixTests {

        @Test
        @DisplayName("EP: Invalid phone - invalid Danish prefix (starts with 1)")
        void epInvalidPhoneInvalidPrefix1() {
            assertFalse(DanishPhoneValidator.isValidDanishMobile("10123456"));
        }

        @ParameterizedTest
        @DisplayName("EP: Invalid phone - various invalid prefixes")
        @ValueSource(strings = {
                "90123456",  // starts with 9
                "11111111",  // starts with 1
                "99999999",  // starts with 9
                "80123456"   // starts with 80 (invalid)
        })
        void isValidDanishMobile_invalidPrefix_returnsFalse(String phone) {
            assertFalse(DanishPhoneValidator.isValidDanishMobile(phone));
        }
    }

    // ============================================
    // EP TESTS - Invalid Phone - Non-Numeric Characters
    // (Migrated from AuthControllerIT)
    // ============================================

    @Nested
    @DisplayName("EP: Invalid Phone - Non-Numeric Characters")
    class NonNumericTests {

        @Test
        @DisplayName("EP: Invalid phone - contains letters")
        void epInvalidPhoneWithLetters() {
            assertFalse(DanishPhoneValidator.isValidDanishMobile("2012abc6"));
        }

        @Test
        @DisplayName("EP: Invalid phone - special characters (dashes)")
        void epInvalidPhoneSpecialChars() {
            assertFalse(DanishPhoneValidator.isValidDanishMobile("20-12-34-56"));
        }

        @ParameterizedTest
        @DisplayName("EP: Invalid phone - various non-numeric inputs")
        @ValueSource(strings = {
                "30abcd56",
                "34!23456",
                "12-34567",
                "abcdefgh"
        })
        void isValidDanishMobile_nonNumeric_returnsFalse(String phone) {
            assertFalse(DanishPhoneValidator.isValidDanishMobile(phone));
        }
    }

    // ============================================
    // EP TESTS - Invalid Phone - Empty or Null
    // (Migrated from AuthControllerIT)
    // ============================================

    @Nested
    @DisplayName("EP: Invalid Phone - Empty or Null")
    class EmptyOrNullTests {

        @Test
        @DisplayName("EP: Invalid phone - empty string")
        void epInvalidPhoneEmpty() {
            assertFalse(DanishPhoneValidator.isValidDanishMobile(""));
        }

        @Test
        @DisplayName("EP: Invalid phone - null value")
        void isValidDanishMobile_null_returnsFalse() {
            assertFalse(DanishPhoneValidator.isValidDanishMobile(null));
        }

        @ParameterizedTest
        @DisplayName("EP: Invalid phone - whitespace only")
        @ValueSource(strings = {"", " ", "   ", "          "})
        void isValidDanishMobile_blankOrEmpty_returnsFalse(String phone) {
            assertFalse(DanishPhoneValidator.isValidDanishMobile(phone));
        }
    }
}
