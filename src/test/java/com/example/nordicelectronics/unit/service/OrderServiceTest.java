package com.example.nordicelectronics.unit.service;

import com.example.nordicelectronics.entity.*;
import com.example.nordicelectronics.entity.dto.address.AddressRequestDTO;
import com.example.nordicelectronics.entity.dto.order.OrderProductRequestDTO;
import com.example.nordicelectronics.entity.dto.order.OrderRequestDTO;
import com.example.nordicelectronics.repositories.sql.AddressRepository;
import com.example.nordicelectronics.repositories.sql.OrderRepository;
import com.example.nordicelectronics.repositories.sql.ProductRepository;
import com.example.nordicelectronics.repositories.sql.UserRepository;
import com.example.nordicelectronics.service.OrderService;
import com.example.nordicelectronics.service.validation.CouponValidationService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @InjectMocks
    private OrderService orderService;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CouponValidationService couponValidationService;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private EntityManager entityManager;

    private UUID userId;
    private UUID productId;
    private User user;
    private Product product;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        productId = UUID.randomUUID();

        user = User.builder()
                .userId(userId)
                .address(new ArrayList<>())
                .build();

        product = Product.builder()
                .productId(productId)
                .price(BigDecimal.valueOf(100))
                .build();

        // Inject EntityManager mock to prevent NPE
        ReflectionTestUtils.setField(orderService, "entityManager", entityManager);
    }

    // ----------------------------------
    // SIMPLE REPO METHODS
    // ----------------------------------
    @Test
    void getAllOrders_shouldReturnList() {
        List<Order> orders = List.of(new Order(), new Order());
        when(orderRepository.findAll()).thenReturn(orders);

        List<Order> result = orderService.getAllOrders();

        assertEquals(2, result.size());
        verify(orderRepository).findAll();
    }

    @Test
    void getOrderById_found_shouldReturnOrder() {
        UUID orderId = UUID.randomUUID();
        Order order = new Order();
        order.setOrderId(orderId);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        Order result = orderService.getOrderById(orderId);
        assertEquals(orderId, result.getOrderId());
    }

    @Test
    void getOrderById_notFound_shouldThrow() {
        UUID orderId = UUID.randomUUID();
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> orderService.getOrderById(orderId));
    }

    @Test
    void getOrdersByIds_shouldReturnOrders() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        List<UUID> ids = List.of(id1, id2);

        Order order1 = Order.builder().orderId(id1).build();
        Order order2 = Order.builder().orderId(id2).build();

        when(orderRepository.findAllById(ids)).thenReturn(List.of(order1, order2));

        List<Order> result = orderService.getOrdersByIds(ids);

        assertEquals(2, result.size());
        assertTrue(result.contains(order1));
        assertTrue(result.contains(order2));

        verify(orderRepository).findAllById(ids);
    }

    @Test
    void getOrdersByUserId_shouldReturnOrdersForUser() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        User user = User.builder().userId(userId).build();
        User otherUser = User.builder().userId(UUID.randomUUID()).build();

        Order order1 = Order.builder().orderId(id1).user(user).build();
        Order order2 = Order.builder().orderId(id2).user(user).build();
        Order order3 = Order.builder().orderId(UUID.randomUUID()).user(otherUser).build();
        Order order4 = Order.builder().orderId(UUID.randomUUID()).user(user).build();
        order4.setDeletedAt(LocalDateTime.now());

        when(orderRepository.findAll()).thenReturn(List.of(order1, order2, order3, order4));

        List<Order> result = orderService.getOrdersByUserId(userId);

        // Only orders 1 and 2 should be returned (order4 is deleted, order3 belongs to another user)
        assertEquals(2, result.size());
        assertTrue(result.contains(order1));
        assertTrue(result.contains(order2));
        assertFalse(result.contains(order3));
        assertFalse(result.contains(order4));

        verify(orderRepository).findAll();
    }

    @Test
    void getOrdersByUserId_noOrders_shouldReturnEmptyList() {
        UUID userId = UUID.randomUUID();

        when(orderRepository.findAll()).thenReturn(List.of());

        List<Order> result = orderService.getOrdersByUserId(userId);

        assertTrue(result.isEmpty());

        verify(orderRepository).findAll();
    }

    // ----------------------------------
    // HAPPY PATH: CREATE ORDER
    // ----------------------------------
    @Test
    void createOrder_happyPath_withCoupon_shouldReturnOrder() throws Exception {
        UUID addressId = UUID.randomUUID();
        UUID couponId = UUID.randomUUID();
        UUID warehouseId = UUID.randomUUID();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(addressRepository.save(any())).thenAnswer(inv -> {
            Address a = new Address();
            a.setAddressId(addressId);
            return a;
        });
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        Coupon coupon = Coupon.builder().couponId(couponId).build();
        when(couponValidationService.validateCoupon(any(), any())).thenReturn(coupon);
        when(couponValidationService.calculateDiscount(coupon, BigDecimal.valueOf(100)))
                .thenReturn(BigDecimal.valueOf(10));

        when(jdbcTemplate.update(any(PreparedStatementCreator.class))).thenReturn(1);

        Order expectedOrder = Order.builder().orderId(UUID.randomUUID()).build();
        when(orderRepository.findTopByUserOrderByCreatedAtDesc(user)).thenReturn(Optional.of(expectedOrder));

        doNothing().when(entityManager).flush(); // prevent NPE

        OrderRequestDTO dto = OrderRequestDTO.builder()
                .userId(userId)
                .orderProducts(List.of(OrderProductRequestDTO.builder()
                        .productId(productId)
                        .quantity(1)
                        .warehouseId(warehouseId)
                        .build()))
                .couponCode("DISCOUNT10")
                .address(AddressRequestDTO.builder()
                        .street("Street")
                        .city("City")
                        .zip("1234")
                        .streetNumber("1")
                        .build())
                .build();

        Order result = orderService.createOrder(dto);

        assertNotNull(result);
        assertEquals(expectedOrder.getOrderId(), result.getOrderId());

        verify(userRepository).findById(userId);
        verify(addressRepository).save(any());
        verify(productRepository).findById(productId);
        verify(couponValidationService).validateCoupon(any(), any());
        verify(couponValidationService).calculateDiscount(coupon, BigDecimal.valueOf(100));
        verify(jdbcTemplate).update(any(PreparedStatementCreator.class));
        verify(orderRepository).findTopByUserOrderByCreatedAtDesc(user);
        verify(entityManager).flush();
    }

    // ----------------------------------
    // CREATE ORDER EDGE CASES
    // ----------------------------------
    @Test
    void createOrder_userNotFound_shouldThrow() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        OrderRequestDTO dto = OrderRequestDTO.builder()
                .userId(userId)
                .orderProducts(List.of())
                .address(AddressRequestDTO.builder().build())
                .build();

        assertThrows(RuntimeException.class, () -> orderService.createOrder(dto));
    }

    @Test
    void createOrder_userDeleted_shouldThrow() {
        user.setDeletedAt(LocalDateTime.now());
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        OrderRequestDTO dto = OrderRequestDTO.builder()
                .userId(userId)
                .orderProducts(List.of())
                .address(AddressRequestDTO.builder().build())
                .build();

        assertThrows(RuntimeException.class, () -> orderService.createOrder(dto));
    }

    @Test
    void createOrder_noAddress_shouldThrow() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        OrderRequestDTO dto = OrderRequestDTO.builder()
                .userId(userId)
                .orderProducts(List.of())
                .build(); // no address

        assertThrows(IllegalArgumentException.class, () -> orderService.createOrder(dto));
    }

    @Test
    void createOrder_productNotFound_shouldThrow() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(addressRepository.save(any())).thenReturn(Address.builder().addressId(UUID.randomUUID()).build());
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        OrderRequestDTO dto = OrderRequestDTO.builder()
                .userId(userId)
                .orderProducts(List.of(OrderProductRequestDTO.builder()
                        .productId(productId)
                        .quantity(1)
                        .build()))
                .address(AddressRequestDTO.builder()
                        .street("X")
                        .city("Y")
                        .zip("Z")
                        .streetNumber("1")
                        .build())
                .build();

        assertThrows(RuntimeException.class, () -> orderService.createOrder(dto));
    }

    @Test
    void createOrder_orderNotFoundAfterProcedure_shouldThrow() throws Exception {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(addressRepository.save(any())).thenReturn(Address.builder().addressId(UUID.randomUUID()).build());
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(jdbcTemplate.update(any(PreparedStatementCreator.class))).thenReturn(1);

        when(orderRepository.findTopByUserOrderByCreatedAtDesc(user)).thenReturn(Optional.empty());
        doNothing().when(entityManager).flush(); // prevent NPE

        OrderRequestDTO dto = OrderRequestDTO.builder()
                .userId(userId)
                .orderProducts(List.of(OrderProductRequestDTO.builder()
                        .productId(productId)
                        .quantity(1)
                        .build()))
                .address(AddressRequestDTO.builder()
                        .street("X")
                        .city("Y")
                        .zip("Z")
                        .streetNumber("1")
                        .build())
                .build();

        assertThrows(IllegalStateException.class, () -> orderService.createOrder(dto));
    }

    @Test
    void deleteOrderById_shouldSetDeletedAt() {
        UUID orderId = UUID.randomUUID();

        // Create an order object
        Order order = new Order();
        order.setOrderId(orderId);

        // Mock repository behavior
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order); // simulate save

        // Call the service method
        orderService.deleteOrderById(orderId);

        // Assert deletedAt is set
        assertNotNull(order.getDeletedAt(), "deletedAt should be set after deletion");

        // Verify save was called
        verify(orderRepository).save(order);
    }

    @Test
    void deleteOrderById_orderNotFound_shouldThrow() {
        UUID orderId = UUID.randomUUID();

        // Mock repository returning empty
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        // Assert that EntityNotFoundException is thrown
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> orderService.deleteOrderById(orderId));

        assertEquals("Order not found", exception.getMessage());
    }

}
