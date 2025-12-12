package com.example.nordicelectronics.unit.entity.registration;

import com.example.nordicelectronics.entity.validator.UserValidator.EmailValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

public class EmailValidatorTest {

    @ParameterizedTest
    @DisplayName("EP: Valid email - contains @ and valid domain")
    @ValueSource(strings = {
            "daniel@example.com",
            "navid@company.dk",
            "arturo@domain.org",
            "anders@gmail.net",
            "someone@store.eu",
            "teacher@school.edu"
    })
    void validateEmail_validEmails_doesNotThrow(String email) {
        assertDoesNotThrow(() -> EmailValidator.validateEmail(email));
    }

    @ParameterizedTest
    @DisplayName("EP: Valid regex - contains . _ % + -")
    @ValueSource(strings = {
            "daniel_jappe@example.com",
            "navid+salatpizza@example.com",
            "arturo.teacher@example.com",
            "mathias%wulff@example.com",
            "someone-someone@example.com"
    })
    void validateEmailRegex_validEmails_doesNotThrow(String email) {
        assertDoesNotThrow(() -> EmailValidator.validateEmail(email));
    }

    @Test
    @DisplayName("EP: Invalid email - missing @ symbol")
    void validateEmail_missingAtSymbol_throws() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> EmailValidator.validateEmail("bigdawg.com")
        );
        assertTrue(exception.getMessage().toLowerCase().contains("invalid email"));
    }


    @Test
    @DisplayName("EP: Invalid email - missing domain after @")
    void validateEmail_missingDomainAfterAt_throws() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> EmailValidator.validateEmail("user@salat")
        );
        assertTrue(exception.getMessage().contains("Invalid email address"));
    }

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
        assertTrue(exception.getMessage().contains("Email must end with a valid TLD"));
    }

    @Test
    @DisplayName("EP: Invalid email - blank/empty string")
    void validateEmail_blankEmail_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> EmailValidator.validateEmail(""));
    }

    @Test
    @DisplayName("EP: Invalid email - null value")
    void validateEmail_nullEmail_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> EmailValidator.validateEmail(null));
    }
}
