package com.example.nordicelectronics.unit.entity;

import com.example.nordicelectronics.entity.Product;
import com.example.nordicelectronics.entity.WarehouseProduct;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class ProductTest {
    @Test
    void shouldReturnZeroStockWhenNoWarehouses() {
        // Arrange
        Product product = Product.builder()
                .warehouseProducts(new HashSet<>())
                .build();

        // Act
        int stockQuantity = product.getStockQuantity();

        // Assert
        assertThat(stockQuantity).isZero();
    }

    @Test
    void shouldCalculateTotalStockAcrossSingleWarehouse() {
        // Arrange
        Product product = Product.builder()
                .warehouseProducts(new HashSet<>())
                .build();

        WarehouseProduct wp = WarehouseProduct.builder()
                .stockQuantity(50)
                .product(product)
                .build();

        product.getWarehouseProducts().add(wp);

        // Act
        int stockQuantity = product.getStockQuantity();

        // Assert
        assertThat(stockQuantity).isEqualTo(50);
    }

    // Parameterized test for multiple scenarios
    // Black-box testing: Boundary value analysis for stock calculation
    @ParameterizedTest
    @MethodSource("provideStockQuantities")
    void shouldHandleDifferentStockQuantities(Set<Integer> quantities, int expected) {
        // Arrange
        Product product = Product.builder()
                .warehouseProducts(new HashSet<>())
                .build();

        quantities.forEach(qty -> {
            WarehouseProduct wp = WarehouseProduct.builder()
                    .stockQuantity(qty)
                    .product(product)
                    .build();
            product.getWarehouseProducts().add(wp);
        });

        // Act
        int stockQuantity = product.getStockQuantity();

        // Assert
        assertThat(stockQuantity).isEqualTo(expected);
    }

    static Stream<Arguments> provideStockQuantities() {
        return Stream.of(
                Arguments.of(Set.of(), 0),           // Empty set
                Arguments.of(Set.of(100), 100),      // Single warehouse
                Arguments.of(Set.of(50, 75), 125),   // Two warehouses
                Arguments.of(Set.of(10, 20, 30), 60) // Three warehouses - all unique values
        );
    }
}
