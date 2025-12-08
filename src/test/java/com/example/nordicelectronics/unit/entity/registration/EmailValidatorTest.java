package com.example.nordicelectronics.unit.entity.registration;

import com.example.nordicelectronics.entity.validator.UserValidator.EmailValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for EmailValidator - Equivalence Partitioning (EP) and Boundary Value Analysis (BVA)
 * Converted from AuthControllerIT to enable faster test execution without database dependency.
 */
public class EmailValidatorTest {

    // ============================================
    // EP/BVA TESTS - Email Format Validation
    // (Migrated from AuthControllerIT)
    // ============================================

    @Nested
    @DisplayName("EP: Valid Email Cases")
    class ValidEmailTests {

        @ParameterizedTest
        @DisplayName("EP: Valid email - contains @ and valid domain")
        @ValueSource(strings = {
                "john@example.com",
                "anna@company.dk",
                "user123@domain.org",
                "test_mail@electronics.net",
                "someone@store.eu",
                "teacher@school.edu",
                "validuser@nordic.com"
        })
        void validateEmail_validEmails_doesNotThrow(String email) {
            assertDoesNotThrow(() -> EmailValidator.validateEmail(email));
        }

        @Test
        @DisplayName("EP: Valid email - with plus sign (email alias)")
        void validateEmail_withPlusSign_doesNotThrow() {
            assertDoesNotThrow(() -> EmailValidator.validateEmail("user+tag@nordic.com"));
        }

        @Test
        @DisplayName("EP: Valid email - with numbers in local part")
        void validateEmail_withNumbers_doesNotThrow() {
            assertDoesNotThrow(() -> EmailValidator.validateEmail("user123@nordic.com"));
        }

        @Test
        @DisplayName("EP: Valid email - with underscore in local part")
        void validateEmail_withUnderscore_doesNotThrow() {
            assertDoesNotThrow(() -> EmailValidator.validateEmail("test_mail@nordic.com"));
        }
    }

    @Nested
    @DisplayName("EP: Invalid Email - Missing @ Symbol")
    class MissingAtSymbolTests {

        @ParameterizedTest
        @DisplayName("EP: Invalid email - missing @ symbol")
        @ValueSource(strings = {
                "invalid.com",
                "testdomain.dk",
                "noatsign.eu",
                "abc123.net",
                "invalidemail.com"
        })
        void validateEmail_missingAtSymbol_throws(String email) {
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> EmailValidator.validateEmail(email)
            );
            assertTrue(exception.getMessage().toLowerCase().contains("invalid email"));
        }
    }

    @Nested
    @DisplayName("EP: Invalid Email - Missing Domain")
    class MissingDomainTests {

        @Test
        @DisplayName("EP: Invalid email - missing domain after @")
        void validateEmail_missingDomainAfterAt_throws() {
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> EmailValidator.validateEmail("user@")
            );
            assertTrue(exception.getMessage().toLowerCase().contains("invalid email"));
        }

        @Test
        @DisplayName("EP: Invalid email - missing TLD (no dot in domain)")
        void validateEmail_missingTLD_throws() {
            assertThrows(IllegalArgumentException.class,
                    () -> EmailValidator.validateEmail("user@nodomain"));
        }
    }

    @Nested
    @DisplayName("EP: Invalid Email - Invalid TLD")
    class InvalidTLDTests {

        @ParameterizedTest
        @DisplayName("EP: Invalid email - invalid TLD")
        @ValueSource(strings = {
                "hello@domain.xyz",
                "info@website.io",
                "support@mail.co",
                "name@invalid.zzz",
                "user@example.xyz"
        })
        void validateEmail_invalidTLD_throws(String email) {
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> EmailValidator.validateEmail(email)
            );
            assertTrue(exception.getMessage().contains("valid TLD"));
        }
    }

    @Nested
    @DisplayName("EP: Invalid Email - Empty or Null")
    class EmptyOrNullTests {

        @ParameterizedTest
        @DisplayName("EP: Invalid email - blank/empty strings")
        @ValueSource(strings = {"", " ", "   "})
        void validateEmail_blankEmail_throws(String email) {
            assertThrows(IllegalArgumentException.class,
                    () -> EmailValidator.validateEmail(email));
        }

        @Test
        @DisplayName("EP: Invalid email - null value")
        void validateEmail_nullEmail_throws() {
            assertThrows(IllegalArgumentException.class,
                    () -> EmailValidator.validateEmail(null));
        }
    }

    @Nested
    @DisplayName("EP: Invalid Email - Whitespace")
    class WhitespaceTests {

        @Test
        @DisplayName("EP: Invalid email - leading/trailing whitespace")
        void validateEmail_withWhitespace_throws() {
            assertThrows(IllegalArgumentException.class,
                    () -> EmailValidator.validateEmail("  spaces@nordic.com  "));
        }
    }
}
