package com.example.nordicelectronics.integration.repository;

import com.example.nordicelectronics.entity.Order;
import com.example.nordicelectronics.entity.User;
import com.example.nordicelectronics.entity.enums.OrderStatus;
import com.example.nordicelectronics.integration.BaseIntegrationTest;
import com.example.nordicelectronics.repositories.sql.OrderRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
class OrderRepositoryTest extends BaseIntegrationTest {

    @Autowired
    private OrderRepository orderRepository;

    @Test
    void shouldSaveAndRetrieveOrder() {
        // Arrange
        User user = createTestUser("John", "Doe", "john.doe@example.com");
        Order order = createTestOrder(user, OrderStatus.pending);

        // Act
        entityManager.persist(order);
        entityManager.flush();
        UUID orderId = order.getOrderId();
        entityManager.clear();

        Order retrieved = orderRepository.findById(orderId).orElseThrow();

        // Assert
        assertNotNull(retrieved);
        assertEquals(OrderStatus.pending, retrieved.getOrderStatus());
        assertEquals(new BigDecimal("100.00"), retrieved.getSubtotal());
        assertEquals(new BigDecimal("25.00"), retrieved.getTaxAmount());
        assertEquals(new BigDecimal("135.00"), retrieved.getTotalAmount());
    }

    @Test
    void shouldFindTopByUserOrderByCreatedAtDesc() {
        // Arrange
        User user = createTestUser("Jane", "Smith", "jane.smith@example.com");

        Order olderOrder = createTestOrder(user, OrderStatus.delivered);
        olderOrder.setCreatedAt(LocalDateTime.now().minusDays(5));
        entityManager.persist(olderOrder);

        Order newerOrder = createTestOrder(user, OrderStatus.pending);
        newerOrder.setCreatedAt(LocalDateTime.now());
        entityManager.persist(newerOrder);

        entityManager.flush();
        entityManager.clear();

        // Act
        Optional<Order> result = orderRepository.findTopByUserOrderByCreatedAtDesc(user);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(newerOrder.getOrderId(), result.get().getOrderId());
        assertEquals(OrderStatus.pending, result.get().getOrderStatus());
    }

    @Test
    void shouldReturnEmptyWhenNoOrdersForUser() {
        // Arrange
        User user = createTestUser("NoOrders", "User", "no.orders@example.com");
        entityManager.flush();
        entityManager.clear();

        // Act
        Optional<Order> result = orderRepository.findTopByUserOrderByCreatedAtDesc(user);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldDeleteOrder() {
        // Arrange
        User user = createTestUser("Delete", "Test", "delete.test@example.com");
        Order order = createTestOrder(user, OrderStatus.cancelled);

        entityManager.persist(order);
        entityManager.flush();
        UUID orderId = order.getOrderId();

        // Act
        orderRepository.deleteById(orderId);
        entityManager.flush();
        entityManager.clear();

        Optional<Order> result = orderRepository.findById(orderId);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldUpdateOrderStatus() {
        // Arrange
        User user = createTestUser("Update", "Test", "update.test@example.com");
        Order order = createTestOrder(user, OrderStatus.pending);

        entityManager.persist(order);
        entityManager.flush();
        UUID orderId = order.getOrderId();

        // Act
        order.setOrderStatus(OrderStatus.shipped);
        entityManager.flush();
        entityManager.clear();

        Order retrieved = orderRepository.findById(orderId).orElseThrow();

        // Assert
        assertEquals(OrderStatus.shipped, retrieved.getOrderStatus());
    }

    private User createTestUser(String firstName, String lastName, String email) {
        User user = User.builder()
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .phoneNumber("+4512345678")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .password("testPassword123")
                .isAdmin(false)
                .build();
        entityManager.persist(user);
        entityManager.flush();
        return user;
    }

    private Order createTestOrder(User user, OrderStatus status) {
        return Order.builder()
                .user(user)
                .orderStatus(status)
                .orderDate(LocalDateTime.now())
                .subtotal(new BigDecimal("100.00"))
                .taxAmount(new BigDecimal("25.00"))
                .shippingCost(new BigDecimal("10.00"))
                .discountAmount(BigDecimal.ZERO)
                .totalAmount(new BigDecimal("135.00"))
                .build();
    }
}

