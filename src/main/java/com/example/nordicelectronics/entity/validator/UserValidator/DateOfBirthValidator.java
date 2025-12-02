package com.example.nordicelectronics.entity.validator.UserValidator;

import java.time.LocalDate;

public class DateOfBirthValidator {

    private static final int MIN_AGE = 18;

    public static void validateDateOfBirth(LocalDate dob) {
        if (dob == null) {
            throw new IllegalArgumentException("Date of birth cannot be null");
        }

        LocalDate today = LocalDate.now();
        int age = today.getYear() - dob.getYear();

        // Adjust if birthday hasn't occurred yet this year
        if (dob.plusYears(age).isAfter(today)) {
            age--;
        }

        if (age < MIN_AGE) {
            throw new IllegalArgumentException("User must be at least 18 years old");
        }
    }
}
