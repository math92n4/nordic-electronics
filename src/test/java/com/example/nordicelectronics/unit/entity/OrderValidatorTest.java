package com.example.nordicelectronics.unit.entity;

import com.example.nordicelectronics.entity.dto.order.OrderProductRequestDTO;
import com.example.nordicelectronics.entity.dto.order.OrderRequestDTO;
import com.example.nordicelectronics.entity.validator.OrderValidator.ValidateOrderQuantity;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class OrderValidatorTest {

    private OrderRequestDTO createOrder(int quantity) {
        OrderProductRequestDTO product = new OrderProductRequestDTO();
        product.setQuantity(quantity);

        return OrderRequestDTO.builder()
                .orderProducts(List.of(product))
                .build();
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 25, 49, 50})
    void validateOrderQuantity_validQuantities_doesNotThrow(int qty) {
        OrderRequestDTO dto = createOrder(qty);

        assertDoesNotThrow(() -> {
            ValidateOrderQuantity.validate(dto);
        });
    }

    @ParameterizedTest
    @ValueSource(ints = {-100, 0, 51, 100})
    void validateOrderQuantity_quantityBelowMinimum_throws(int qty) {
        OrderRequestDTO dto = createOrder(qty);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> ValidateOrderQuantity.validate(dto));

        assertTrue(exception.getMessage().contains("Product quantity must be greater than 0") || exception.getMessage().contains("Product quantity must be less than or equal to 50"));
    }
}

