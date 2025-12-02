package com.example.nordicelectronics.entity.validator.UserValidator;

import java.util.Set;
import java.util.regex.Pattern;

public class EmailValidator {

    private static final Set<String> VALID_TLDS = Set.of(
            ".com", ".dk", ".org", ".net", ".eu", ".edu"
    );

    private static final String REGEX_PATTERN = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

    private static final Pattern EMAIL_PATTERN = Pattern.compile(REGEX_PATTERN);

    public static void validateEmail(String email) {

        if(email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email is null or empty");

        } else if(!EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("Invalid email address");
        }

        boolean hasValidTld = VALID_TLDS.stream()
                .anyMatch(email::endsWith);

        if(!hasValidTld) {
            throw new IllegalArgumentException("Email must end with a valid TLD " + VALID_TLDS);
        }
    }


}
