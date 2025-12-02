package com.example.nordicelectronics.entity.validator.UserValidator;

public class PasswordValidator {

    private static final int MIN_LENGTH = 8;
    private static final int MAX_LENGTH = 64;

    public static void validatePassword(String password) {
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }

        int length = password.length();

        if (length < MIN_LENGTH) {
            throw new IllegalArgumentException("Password must be at least " + MIN_LENGTH + " characters long");
        }

        if (length > MAX_LENGTH) {
            throw new IllegalArgumentException("Password must be at most " + MAX_LENGTH + " characters long");
        }
    }

}
