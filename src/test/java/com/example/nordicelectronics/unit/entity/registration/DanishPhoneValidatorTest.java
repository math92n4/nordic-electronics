package com.example.nordicelectronics.unit.entity.registration;

import com.example.nordicelectronics.entity.validator.UserValidator.DanishPhoneValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DanishPhoneValidatorTest {

    @ParameterizedTest
    @DisplayName("EP: Valid phone - various valid Danish mobile prefixes")
    @ValueSource(strings = {
            "20123456", // prefix "2" - valid
            "30123456", // prefix "30" - valid
            "34212345", // prefix "342" - valid
            "40123456", // prefix "40" - valid
            "78912345", // prefix "789" - valid
            "82912345"  // prefix "829" - valid
    })
    void isValidDanishMobile_validNumbers_returnsTrue(String phone) {
        assertTrue(DanishPhoneValidator.isValidDanishMobile(phone));
    }

    @Test
    @DisplayName("EP: Invalid phone - only 7 digits")
    void epInvalidPhone7Digits() {
        assertFalse(DanishPhoneValidator.isValidDanishMobile("2012345"));
    }

    @Test
    @DisplayName("EP: Invalid phone - 9 digits")
    void epInvalidPhone9Digits() {
        assertFalse(DanishPhoneValidator.isValidDanishMobile("201234567"));
    }

    @ParameterizedTest
    @DisplayName("EP: Invalid phone - various invalid prefixes with valid lengths")
    @ValueSource(strings = {
            "90123456",
            "11111111",
            "99999999",
            "80123456"
    })
    void isValidDanishMobile_invalidPrefix_returnsFalse(String phone) {
        assertFalse(DanishPhoneValidator.isValidDanishMobile(phone));
    }

    @ParameterizedTest
    @DisplayName("EP: Invalid phone - various non-numeric inputs")
    @ValueSource(strings = {
            "30abcd56",
            "34!23456",
            "12-34567",
            "abcdefgh",
            "+4520208517",
    })
    void isValidDanishMobile_nonNumeric_returnsFalse(String phone) {
        assertFalse(DanishPhoneValidator.isValidDanishMobile(phone));
    }

    @Test
    @DisplayName("EP: Invalid phone - empty string")
    void epInvalidPhoneEmpty() {
        assertFalse(DanishPhoneValidator.isValidDanishMobile(""));
    }

    @Test
    @DisplayName("EP: Invalid phone - null value")
    void isValidDanishMobile_null_returnsFalse() {
        assertFalse(DanishPhoneValidator.isValidDanishMobile(null));
    }
}
