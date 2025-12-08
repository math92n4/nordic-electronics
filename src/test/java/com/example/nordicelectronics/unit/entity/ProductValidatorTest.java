package com.example.nordicelectronics.unit.entity;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ProductValidatorTest {
//    @Test
//    void shouldReturnZeroStockWhenNoWarehouses() {
//        // Arrange
//        Product product = Product.builder()
//                .warehouseProducts(new HashSet<>())
//                .build();
//
//        // Act
//        int stockQuantity = product.getStockQuantity();
//
//        // Assert
//        assertThat(stockQuantity).isZero();
//    }
//
//    @Test
//    void shouldCalculateTotalStockAcrossSingleWarehouse() {
//        // Arrange
//        Product product = Product.builder()
//                .warehouseProducts(new HashSet<>())
//                .build();
//
//        WarehouseProduct wp = WarehouseProduct.builder()
//                .stockQuantity(50)
//                .product(product)
//                .build();
//
//        product.getWarehouseProducts().add(wp);
//
//        // Act
//        int stockQuantity = product.getStockQuantity();
//
//        // Assert
//        assertThat(stockQuantity).isEqualTo(50);
//    }
//
//    /**
//     * Parameterized test for multiple scenarios
//     * Black-box testing: Boundary value analysis for stock calculation
//     */
//    @ParameterizedTest
//    @MethodSource("provideStockQuantities")
//    void shouldHandleDifferentStockQuantities(List<Integer> quantities, int expected) {
//        // Arrange
//        Product product = Product.builder()
//                .warehouseProducts(new HashSet<>())
//                .build();
//
//        quantities.forEach(qty -> {
//            WarehouseProduct wp = WarehouseProduct.builder()
//                    .stockQuantity(qty)
//                    .product(product)
//                    .build();
//            product.getWarehouseProducts().add(wp);
//        });
//
//        // Act
//        int stockQuantity = product.getStockQuantity();
//
//        // Assert
//        assertThat(stockQuantity).isEqualTo(expected);
//    }
//
//    static Stream<Arguments> provideStockQuantities() {
//        return Stream.of(
//                Arguments.of(List.of(), 0),                 // Empty list
//                Arguments.of(List.of(0), 0),                 // Single warehouse with zero stock (BVA)
//                Arguments.of(List.of(1), 1),                 // Single warehouse with minimal positive stock (BVA)
//                Arguments.of(List.of(100), 100),            // Single warehouse
//                Arguments.of(List.of(50, 75), 125),         // Two warehouses
//                Arguments.of(List.of(10, 20, 30), 60),      // Three warehouses - all unique values
//                Arguments.of(List.of(0, 10), 10),           // Mixed zero and positive
//                Arguments.of(List.of(10, 10), 20)           // Duplicate quantities (two warehouses with same stock)
//        );
//    }
//
//    // New test: verify @Min(0) validation on WarehouseProduct.stockQuantity
//    @Test
//    void warehouseProductShouldNotAllowNegativeStock() {
//        WarehouseProduct wp = WarehouseProduct.builder()
//                .stockQuantity(-1)
//                .build();
//
//        // Use Jakarta Validation to check the @Min constraint
//        try (jakarta.validation.ValidatorFactory factory = jakarta.validation.Validation.buildDefaultValidatorFactory()) {
//            jakarta.validation.Validator validator = factory.getValidator();
//            Set<jakarta.validation.ConstraintViolation<WarehouseProduct>> violations = validator.validate(wp);
//
//            assertThat(violations).isNotEmpty();
//            boolean hasStockQuantityViolation = violations.stream()
//                    .anyMatch(v -> "stockQuantity".equals(v.getPropertyPath().toString()));
//            assertThat(hasStockQuantityViolation).isTrue();
//        }
//    }
//

    // we assume that 50 is max stock for this test
    @ParameterizedTest
    @CsvSource({
            "-1, false",
            "0, true",
            "1, true",
            "25, true",
            "49, true",
            "50, true",
            "51, false",
    })
    void stockQuantityHasToBeValid(int stockQuantity, boolean expectedValid) {
        if (expectedValid) {
            assertThatCode(() -> getValidStockQuantity(stockQuantity)).doesNotThrowAnyException();
        } else {
            assertThatThrownBy(() -> getValidStockQuantity(stockQuantity))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Stock quantity must be between 0 and 50");
        }
    }

    // New shared helper enforcing a valid range [0..50] for stock quantity
    private void getValidStockQuantity(int stockQuantity) {
        if (stockQuantity < 0 || stockQuantity > 50) {
            throw new IllegalArgumentException("Stock quantity must be between 0 and 50");
        }
    }
}
