package com.example.nordicelectronics.unit.entity.registration;

import com.example.nordicelectronics.entity.validator.UserValidator.DateOfBirthValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class DateOfBirthValidatorTest {

    @Test
    @DisplayName("EP: Null date of birth - throws exception")
    void validateDateOfBirth_null_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> DateOfBirthValidator.validateDateOfBirth(null));
    }

    @Test
    @DisplayName("BVA: User exactly 18 years old - valid (at boundary)")
    void bvaAge18Years_doesNotThrow() {
        LocalDate dob = LocalDate.now().minusYears(18);
        assertDoesNotThrow(() -> DateOfBirthValidator.validateDateOfBirth(dob));
    }

    @Test
    @DisplayName("BVA: User exactly 18 years old and one day - valid (just above boundary)")
    void bvaAge18YearsAnd1Day_doesNotThrow() {
        LocalDate dob = LocalDate.now().minusYears(18).minusDays(1);
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
        assertTrue(ex.getMessage().contains("User must be at least 18 years old"));
    }
}
