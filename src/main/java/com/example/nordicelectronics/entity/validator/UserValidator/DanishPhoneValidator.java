package com.example.nordicelectronics.entity.validator.UserValidator;

import java.util.Set;

public class DanishPhoneValidator {

    // All valid Danish mobile prefixes
    private static final Set<String> VALID_PREFIXES = Set.of(
            // Single digit
            "2",

            // Two digits
            "30", "31", "40", "41", "42", "50", "51", "52", "53",
            "60", "61", "71", "81", "91", "92", "93",

            // Three digits
            "342",
            "344", "345", "346", "347", "348", "349",
            "356", "357",
            "359",
            "362",
            "365", "366",
            "389",
            "398",
            "431",
            "441",
            "462",
            "466",
            "468",
            "472",
            "474",
            "476",
            "478",
            "485", "486",
            "488", "489",
            "493", "494", "495", "496",
            "498", "499",
            "542", "543",
            "545",
            "551", "552",
            "556",
            "571", "572", "573", "574",
            "577",
            "579",
            "584",
            "586", "587",
            "589",
            "597", "598",
            "627",
            "629",
            "641",
            "649",
            "658",
            "662", "663", "664", "665",
            "667",
            "692", "693", "694",
            "697",
            "771", "772",
            "782", "783",
            "785", "786",
            "788", "789",
            "826", "827",
            "829"
    );

    public static boolean isValidDanishMobile(String phoneNumber) {
        // Check if exactly 8 digits
        if (phoneNumber == null || !phoneNumber.matches("^\\d{8}$")) {
            return false;
        }

        // Check if starts with valid prefix
        // Try 3-digit prefix first, then 2-digit, then 1-digit
        if (phoneNumber.length() >= 3 && VALID_PREFIXES.contains(phoneNumber.substring(0, 3))) {
            return true;
        }
        if (phoneNumber.length() >= 2 && VALID_PREFIXES.contains(phoneNumber.substring(0, 2))) {
            return true;
        }
        return VALID_PREFIXES.contains(phoneNumber.substring(0, 1));
    }
}