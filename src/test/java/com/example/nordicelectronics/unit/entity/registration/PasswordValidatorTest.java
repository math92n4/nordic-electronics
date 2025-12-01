package com.example.nordicelectronics.unit.entity.registration;

import com.example.nordicelectronics.entity.validator.UserValidator.PasswordValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class PasswordValidatorTest {

    private static Stream<String> invalidPasswords() {
        return Stream.of(
                "p".repeat(65),
                "verylongpasswordverylongpasswordverylongpasswordverylongpassword123"
        );
    }

    // -----------------------
    // Valid cases (EP + BVA: 8–64)
    // -----------------------
    @ParameterizedTest
    @MethodSource("validPasswords")
    void validatePassword_validLengths_doesNotThrow(String password) {
        assertDoesNotThrow(() -> PasswordValidator.validatePassword(password));
    }

    private static Stream<String> validPasswords() {
        return Stream.of(
                "12345678",          // 8 chars
                "123456789",         // 9 chars
                "passwordpasswordpasswordpasswordpasswordpasswordpw", // ~50 chars
                "p".repeat(64)       // now allowed
        );
    }

    // -----------------------
    // Invalid cases: below minimum (< 8)
    // BVA: 7 chars
    // -----------------------
    @ParameterizedTest
    @ValueSource(strings = {
            "1234567",    // 7 chars
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

    // -----------------------
    // Invalid cases: above maximum (> 64)
    // BVA: 65 chars
    // -----------------------
    @ParameterizedTest
    @MethodSource("invalidLongPasswords")
    void validatePassword_tooLong_throws(String password) {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> PasswordValidator.validatePassword(password)
        );
        assertTrue(ex.getMessage().contains("at most"));
    }

    private static Stream<String> invalidLongPasswords() {
        return Stream.of(
                "p".repeat(65),
                "verylongpasswordverylongpasswordverylongpasswordverylongpassword123"
        );
    }


    // -----------------------
    // Null or blank
    // -----------------------
    @Test
    void validatePassword_null_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> PasswordValidator.validatePassword(null));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "   "})
    void validatePassword_blank_throws(String password) {
        assertThrows(IllegalArgumentException.class,
                () -> PasswordValidator.validatePassword(password));
    }
}
