package com.example.nordicelectronics.unit.mapper;

import com.example.nordicelectronics.entity.*;
import com.example.nordicelectronics.entity.dto.order.OrderResponseDTO;
import com.example.nordicelectronics.entity.enums.OrderStatus;
import com.example.nordicelectronics.entity.mapper.OrderMapper;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class OrderMapperTest {

    @Test
    void toResponseDTO_WithValidOrder_ShouldMapAllFields() {
        // Arrange
        UUID userId = UUID.randomUUID();
        UUID addressId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        User user = User.builder()
                .userId(userId)
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .build();

        Address address = Address.builder()
                .addressId(addressId)
                .street("Main St")
                .streetNumber("123")
                .zip("1000")
                .city("Copenhagen")
                .build();

        Order order = Order.builder()
                .orderId(orderId)
                .user(user)
                .address(address)
                .orderStatus(OrderStatus.pending)
                .totalAmount(new BigDecimal("1000.00"))
                .subtotal(new BigDecimal("900.00"))
                .taxAmount(new BigDecimal("50.00"))
                .shippingCost(new BigDecimal("50.00"))
                .discountAmount(BigDecimal.ZERO)
                .orderDate(LocalDateTime.now())
                .build();

        // Act
        OrderResponseDTO result = OrderMapper.toResponseDTO(order);

        // Assert
        assertNotNull(result);
        assertEquals(orderId, result.getOrderId());
        assertEquals(userId, result.getUser().getUserId());
        assertEquals("John", result.getUser().getFirstName());
        assertEquals(addressId, result.getAddress().getAddressId());
        assertEquals("Copenhagen", result.getAddress().getCity());
        assertEquals("pending", result.getStatus());
        assertEquals(new BigDecimal("1000.00"), result.getTotalAmount());
    }

    @Test
    void toResponseDTO_WithNullOrder_ShouldReturnNull() {
        // Act
        OrderResponseDTO result = OrderMapper.toResponseDTO(null);

        // Assert
        assertNull(result);
    }

    @Test
    void toResponseDTO_WithNullUser_ShouldHandleGracefully() {
        // Arrange
        UUID orderId = UUID.randomUUID();

        Order order = Order.builder()
                .orderId(orderId)
                .user(null) // null user
                .orderStatus(OrderStatus.pending)
                .totalAmount(new BigDecimal("1000.00"))
                .build();

        // Act
        OrderResponseDTO result = OrderMapper.toResponseDTO(order);

        // Assert
        assertNotNull(result);
        assertEquals(orderId, result.getOrderId());
        assertNull(result.getUser());
    }

    @Test
    void toResponseDTO_WithOrderProducts_ShouldMapProducts() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        Product product = Product.builder()
                .productId(productId)
                .name("Laptop")
                .price(new BigDecimal("999.99"))
                .build();

        OrderProduct orderProduct = OrderProduct.builder()
                .product(product)
                .quantity(2)
                .unitPrice(new BigDecimal("999.99"))
                .totalPrice(new BigDecimal("1999.98"))
                .build();

        Order order = Order.builder()
                .orderId(orderId)
                .orderProducts(List.of(orderProduct))
                .totalAmount(new BigDecimal("1999.98"))
                .build();

        // Act
        OrderResponseDTO result = OrderMapper.toResponseDTO(order);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getOrderProducts().size());
        assertEquals(productId, result.getOrderProducts().get(0).getProduct().getProductId());
        assertEquals(2, result.getOrderProducts().get(0).getQuantity());
    }
}