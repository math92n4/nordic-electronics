package com.example.nordicelectronics.entity.validator.WarrantyValidator;

import java.time.LocalDate;

public class WarrantyValidator {

    private WarrantyValidator() {}

    public static void validateWarrantyDates(LocalDate startDate, LocalDate endDate) {
        if (startDate == null) {
            throw new IllegalArgumentException("Warranty start date cannot be null");
        }
        
        if (endDate == null) {
            throw new IllegalArgumentException("Warranty end date cannot be null");
        }
        
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("Warranty end date must be after or equal to start date");
        }
        
        if (endDate.isEqual(startDate)) {
            throw new IllegalArgumentException("Warranty end date must be after start date (minimum 1 day warranty)");
        }
    }

    public static void validateDescription(String description) {
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Warranty description cannot be null or empty");
        }
        
        if (description.length() > 500) {
            throw new IllegalArgumentException("Warranty description cannot exceed 500 characters");
        }
    }
}