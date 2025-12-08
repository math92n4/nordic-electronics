package com.example.nordicelectronics.unit.entity;

import com.example.nordicelectronics.entity.Review;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class ReviewValueValidatorTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();


    // -------------------------
    // DATA PROVIDER: VALID CASES
    // -------------------------
    static Stream<Integer> validValues() {
        return Stream.of(1, 2, 3, 4, 5);  // EP + BVA
    }

    // -------------------------
    // DATA PROVIDER: INVALID CASES
    // -------------------------
    static Stream<Integer> invalidValues() {
        return Stream.of(0, -1, 6, 10);  // EP + BVA
    }

    @ParameterizedTest
    @MethodSource("validValues")
    void reviewValue_shouldBeValid(int value) {
        Review review = new Review();
        review.setReviewValue(value);

        Set<ConstraintViolation<Review>> violations = validator.validate(review);
        assertThat(violations).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("invalidValues")
    void reviewValue_shouldBeInvalid(int value) {
        Review review = new Review();
        review.setReviewValue(value);

        Set<ConstraintViolation<Review>> violations = validator.validate(review);
        assertThat(violations).isNotEmpty();
    }
}
