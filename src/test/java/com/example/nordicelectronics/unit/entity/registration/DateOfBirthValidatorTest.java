package com.example.nordicelectronics.unit.entity.registration;

import com.example.nordicelectronics.entity.validator.UserValidator.DateOfBirthValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDate;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class DateOfBirthValidatorTest {

    // -----------------------
    // Valid dates (>= 18)
    // -----------------------
    @ParameterizedTest
    @MethodSource("validDates")
    void validateDateOfBirth_validDates_doesNotThrow(LocalDate dob) {
        assertDoesNotThrow(() -> DateOfBirthValidator.validateDateOfBirth(dob));
    }

    private static Stream<LocalDate> validDates() {
        LocalDate today = LocalDate.now();
        return Stream.of(
                today.minusYears(18),   // exactly 18
                today.minusYears(19),   // older than 18
                today.minusYears(50)    // much older
        );
    }

    // -----------------------
    // Invalid dates (< 18)
    // -----------------------
    @ParameterizedTest
    @MethodSource("invalidDates")
    void validateDateOfBirth_invalidDates_throws(LocalDate dob) {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> DateOfBirthValidator.validateDateOfBirth(dob)
        );
        assertTrue(ex.getMessage().contains("at least 18"));
    }

    private static Stream<LocalDate> invalidDates() {
        LocalDate today = LocalDate.now();
        return Stream.of(
                today.minusYears(17),  // 17 years old
                today.minusYears(18).plusDays(1), // 17y 364d
                today.minusYears(10)   // too young
        );
    }

    // -----------------------
    // Null input
    // -----------------------
    @Test
    void validateDateOfBirth_null_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> DateOfBirthValidator.validateDateOfBirth(null));
    }
}
