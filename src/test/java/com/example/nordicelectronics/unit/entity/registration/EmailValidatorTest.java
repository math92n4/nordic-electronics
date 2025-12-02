package com.example.nordicelectronics.unit.entity.registration;

import com.example.nordicelectronics.entity.validator.UserValidator.EmailValidator;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

public class EmailValidatorTest {

    // -----------------------
    // Valid email cases
    // -----------------------
    @ParameterizedTest
    @ValueSource(strings = {
            "john@example.com",
            "anna@company.dk",
            "user123@domain.org",
            "test_mail@electronics.net",
            "someone@store.eu",
            "teacher@school.edu"
    })
    void validateEmail_validEmails_doesNotThrow(String email) {
        assertDoesNotThrow(() -> EmailValidator.validateEmail(email));
    }

    // -----------------------
    // Invalid email cases: missing @
    // -----------------------
    @ParameterizedTest
    @ValueSource(strings = {
            "invalid.com",
            "testdomain.dk",
            "noatsign.eu",
            "abc123.net"
    })
    void validateEmail_missingAtSymbol_throws(String email) {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> EmailValidator.validateEmail(email)
        );

        assertTrue(exception.getMessage().toLowerCase().contains("invalid email"));
    }

    // -----------------------
    // Invalid email cases: invalid TLD
    // -----------------------
    @ParameterizedTest
    @ValueSource(strings = {
            "hello@domain.xyz",
            "info@website.io",
            "support@mail.co",
            "name@invalid.zzz"
    })
    void validateEmail_invalidTLD_throws(String email) {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> EmailValidator.validateEmail(email)
        );

        assertTrue(exception.getMessage().contains("valid TLD"));
    }

    // -----------------------
    // Invalid email cases: empty or null
    // -----------------------
    @ParameterizedTest
    @ValueSource(strings = {"", " ", "   "})
    void validateEmail_blankEmail_throws(String email) {
        assertThrows(IllegalArgumentException.class,
                () -> EmailValidator.validateEmail(email));
    }

    // Note: null must be tested separately (ValueSource doesn't allow null)
    @org.junit.jupiter.api.Test
    void validateEmail_nullEmail_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> EmailValidator.validateEmail(null));
    }
}
