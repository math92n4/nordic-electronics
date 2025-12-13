package com.example.nordicelectronics.unit.entity.registration;

import com.example.nordicelectronics.entity.validator.UserValidator.PasswordValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class PasswordValidatorTest {

    @ParameterizedTest
    @DisplayName("EP: Password in valid range (8-64)")
    @MethodSource("validPasswords")
    void validatePassword_validLengths_doesNotThrow(String password) {
        assertDoesNotThrow(() -> PasswordValidator.validatePassword(password));
    }

    @ParameterizedTest
    @DisplayName("EP: Password in invalid range (<8 || >64)")
    @MethodSource("invalidPasswords")
    void validatePassword_invalidRange_throws(String password) {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> PasswordValidator.validatePassword(password)
        );
        assertTrue(ex.getMessage().contains("Password must be at least 8 characters long") || ex.getMessage().contains("Password must be at most 64 characters long"));
    }

    @Test
    @DisplayName("EP: Null password - throws exception")
    void validatePassword_null_throws() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> PasswordValidator.validatePassword(null)
        );
        assertTrue(ex.getMessage().contains("Password cannot be null or empty"));
    }

    @Test
    @DisplayName("EP: Empty password - throws exception")
    void validatePassword_empty_throws() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> PasswordValidator.validatePassword(null)
        );
        assertTrue(ex.getMessage().contains("Password cannot be null or empty"));
    }

    // Test Data Providers

    static Stream<String> validPasswords() {
        return Stream.of(
                "12345678", // minimum
                "123456789",
                "p".repeat(36),
                "p".repeat(63),
                "p".repeat(64) // maximum
        );
    }

    static Stream<String> invalidPasswords() {
        return Stream.of(
                "p",
                "p".repeat(7),
                "p".repeat(65),
                "p".repeat(120)
        );

    }
}
