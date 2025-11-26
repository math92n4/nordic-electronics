package com.example.nordicelectronics.unit.service;

import com.example.nordicelectronics.entity.Order;
import com.example.nordicelectronics.entity.User;
import com.example.nordicelectronics.repositories.sql.OrderRepository;
import com.example.nordicelectronics.repositories.sql.UserRepository;
import com.example.nordicelectronics.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @InjectMocks
    private OrderService orderService;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserRepository userRepository;

    private UUID orderId;
    private UUID userId;
    private User user;
    private Order order;

    @BeforeEach
    void setUp() {
        // Initialize UUIDs
        orderId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        userId = UUID.fromString("11111111-1111-1111-1111-111111111111");

        // Initialize sample entities
        user = new User();
        user.setUserId(userId);

        order = new Order();
        order.setOrderId(orderId);
        order.setUser(user);
    }

    @Test
    @DisplayName("Should return all orders from repo as list")
    void getAllOrders() {
        // Arrange
        Order order1 = new Order();
        List<Order> orders = Arrays.asList(order, order1);

        // Behavior of the mocked repository
        when(orderRepository.findAll()).thenReturn(orders);

        // Act
        List<Order> actualOrders = orderService.getAllOrders();

        // Assert
        assertEquals(2, actualOrders.size());
        assertTrue(actualOrders.containsAll(orders));
        // Verify that findAll was called exactly once
        verify(orderRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should return an order when a valid ID is provided")
    void getOrderById_validId_success() {
        // Arrange
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        // Act
        Order actualOrder = orderService.getOrderById(orderId);

        // Assert
        assertNotNull(actualOrder);
        assertEquals(orderId, actualOrder.getOrderId());
        verify(orderRepository).findById(orderId);
    }

    @Test
    @DisplayName("Should throw RuntimeException when an invalid ID is provided")
    void getOrderById_invalidId_throwsException() {
        // Arrange
        UUID invalidId = UUID.randomUUID();
        when(orderRepository.findById(invalidId)).thenReturn(Optional.empty());

        // Expect RuntimeException with the specific message
        Exception exception = assertThrows(RuntimeException.class, () -> {
            orderService.getOrderById(invalidId);
        });

        assertTrue(exception.getMessage().contains("Order not found with ID: " + invalidId));
    }

    @Test
    @DisplayName("Should return a list of orders for a batch of IDs")
    void getOrdersByIds_success() {
        // Arrange
        UUID orderId1 = UUID.randomUUID();
        Order order1 = new Order();
        order1.setOrderId(orderId1);

        List<UUID> idList = Arrays.asList(orderId, orderId1);
        List<Order> expectedOrders = Arrays.asList(order, order1);

        when(orderRepository.findAllById(idList)).thenReturn(expectedOrders);

        // Act
        List<Order> actualOrders = orderService.getOrdersByIds(idList);

        // Assert
        assertEquals(2, actualOrders.size());
        verify(orderRepository).findAllById(idList);
    }

    @Test
    @DisplayName("Should return orders filtered by a specific user ID")
    void getOrdersByUserId_success() {

        // Arrange
        Order order1 = new Order();
        order1.setUser(user);
        List<Order> usersOrders = Arrays.asList(order, order1);

        when(orderRepository.findAll()).thenReturn(usersOrders);

        // Act
        List<Order> actualOrders = orderService.getOrdersByUserId(userId);

        // Assert
        assertEquals(usersOrders.size(), actualOrders.size());
        verify(orderRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should successfully delete an order by ID")
    void deleteOrder_success() {
        // Act
        orderService.deleteOrder(orderId);

        // Assert
        // Verify that the deleteById method was called with the correct ID
        verify(orderRepository, times(1)).deleteById(orderId);
    }


    @Test
    @DisplayName("Should throw RuntimeException if the referenced User ID does not exist")
    void createOrder_userNotFound_throwsException() {
        // Arrange
        UUID unknownUserId = UUID.randomUUID();
        Order newOrder = new Order();
        User userReference = new User();
        userReference.setUserId(unknownUserId);
        newOrder.setUser(userReference);

        // Mock the user lookup failure
        when(userRepository.findById(unknownUserId)).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            orderService.createOrder(newOrder);
        });

        assertTrue(exception.getMessage().contains("User not found with ID: " + unknownUserId));
        // Verify that the save operation was NOT called
        verify(orderRepository, never()).save(any(Order.class));
    }


}
