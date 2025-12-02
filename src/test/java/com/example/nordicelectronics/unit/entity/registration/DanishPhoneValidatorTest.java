package com.example.nordicelectronics.unit.entity.registration;

import com.example.nordicelectronics.entity.validator.UserValidator.DanishPhoneValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class DanishPhoneValidatorTest {

    // -----------------------
    // Valid Danish mobile numbers
    // -----------------------

    @ParameterizedTest
    @ValueSource(strings = {
            "20123456", // single-digit prefix "2"
            "30123456", // two-digit prefix "30"
            "34212345", // three-digit prefix "342"
            "78912345", // three-digit prefix "789"
            "82912345"  // three-digit prefix "829"
    })
    void isValidDanishMobile_validNumbers_returnsTrue(String phone) {
        assertTrue(DanishPhoneValidator.isValidDanishMobile(phone));
    }

    // -----------------------
    // Invalid numbers: wrong length
    // -----------------------
    @ParameterizedTest
    @ValueSource(strings = {
            "1234567",   // 7 digits
            "123456789", // 9 digits
            "",          // empty
            "          ", // spaces
            "1234"       // 4 digits
    })
    void isValidDanishMobile_invalidLength_returnsFalse(String phone) {
        assertFalse(DanishPhoneValidator.isValidDanishMobile(phone));
    }

    // -----------------------
    // Invalid numbers: not starting with valid prefix
    // -----------------------
    @ParameterizedTest
    @ValueSource(strings = {
            "90123456",
            "11111111",
            "99999999",
            "80123456"
    })
    void isValidDanishMobile_invalidPrefix_returnsFalse(String phone) {
        assertFalse(DanishPhoneValidator.isValidDanishMobile(phone));
    }

    // -----------------------
    // Invalid numbers: non-numeric characters
    // -----------------------
    @ParameterizedTest
    @ValueSource(strings = {
            "30abcd56",
            "34!23456",
            "12-34567",
            "abcdefgh"
    })
    void isValidDanishMobile_nonNumeric_returnsFalse(String phone) {
        assertFalse(DanishPhoneValidator.isValidDanishMobile(phone));
    }

    // -----------------------
    // Null input
    // -----------------------
    @Test
    void isValidDanishMobile_null_returnsFalse() {
        assertFalse(DanishPhoneValidator.isValidDanishMobile(null));
    }
}
