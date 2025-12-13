package com.example.nordicelectronics.service;

import com.example.nordicelectronics.entity.*;
import com.example.nordicelectronics.entity.dto.order.OrderProductRequestDTO;
import com.example.nordicelectronics.entity.dto.order.OrderRequestDTO;
import com.example.nordicelectronics.repositories.sql.*;
import com.example.nordicelectronics.service.validation.CouponValidationService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Types;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final ProductRepository productRepository;
    private final CouponValidationService couponValidationService;
    private final JdbcTemplate jdbcTemplate;

    @PersistenceContext
    EntityManager entityManager;

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Order getOrderById(UUID orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Order not found with ID: " + orderId));
    }

    public List<Order> getOrdersByIds(List<UUID> orderIds) {
        return orderRepository.findAllById(orderIds);
    }

    public List<Order> getOrdersByUserId(UUID userId) {
        return orderRepository.findAll().stream()
                .filter(order -> order.getUser() != null && order.getUser().getUserId().equals(userId))
                .filter(order -> order.getDeletedAt() == null)
                .sorted(Comparator.comparing(Order::getOrderDate, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    @Transactional
    public Order createOrder(OrderRequestDTO dto) {
        // =====================================================
        // 1. VALIDATE USER
        // =====================================================
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("User not found with ID: " + dto.getUserId()));

        if (user.getDeletedAt() != null) {
            throw new IllegalStateException("User has been deleted");
        }

        // =====================================================
        // 2. HANDLE ADDRESS
        // =====================================================
        Address address;

        if (dto.getAddress() == null) {
            throw new IllegalArgumentException("Order must contain an address in the request");
        }

        // Validate address fields
        if (dto.getAddress().getStreet() == null || dto.getAddress().getStreet().isBlank()) {
            throw new IllegalArgumentException("Address street is required");
        }
        if (dto.getAddress().getStreetNumber() == null || dto.getAddress().getStreetNumber().isBlank()) {
            throw new IllegalArgumentException("Address street number is required");
        }
        if (dto.getAddress().getZip() == null || dto.getAddress().getZip().isBlank()) {
            throw new IllegalArgumentException("Address zip code is required");
        }
        if (dto.getAddress().getCity() == null || dto.getAddress().getCity().isBlank()) {
            throw new IllegalArgumentException("Address city is required");
        }

        address = Address.builder()
                .street(dto.getAddress().getStreet())
                .streetNumber(dto.getAddress().getStreetNumber())
                .zip(dto.getAddress().getZip())
                .city(dto.getAddress().getCity())
                .user(user)
                .build();
        address = addressRepository.save(address);

        // CRITICAL: Flush to ensure address is visible to stored procedure
        entityManager.flush();

        // =====================================================
        // 3. CALCULATE SUBTOTAL
        // =====================================================
        BigDecimal subtotal = BigDecimal.ZERO;

        for (OrderProductRequestDTO productDto : dto.getOrderProducts()) {
            Product product = productRepository.findById(productDto.getProductId())
                    .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Product not found: " + productDto.getProductId()));

            BigDecimal itemTotal = product.getPrice()
                    .multiply(BigDecimal.valueOf(productDto.getQuantity()));
            subtotal = subtotal.add(itemTotal);
        }

        // =====================================================
        // 4. VALIDATE COUPON AND CALCULATE DISCOUNT
        // =====================================================
        Coupon validatedCoupon = null;
        BigDecimal discountAmount = BigDecimal.ZERO;

        if (dto.getCouponCode() != null && !dto.getCouponCode().isBlank()) {
            validatedCoupon = couponValidationService.validateCoupon(
                    dto.getCouponCode(),
                    subtotal
            );

            discountAmount = couponValidationService.calculateDiscount(
                    validatedCoupon,
                    subtotal
            );
        }

        // =====================================================
        // 5. PREPARE ORDER ITEMS JSON
        // =====================================================
        List<Map<String, Object>> orderItemsJson = dto.getOrderProducts().stream()
                .map(productDto -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("product_id", productDto.getProductId().toString());
                    item.put("quantity", productDto.getQuantity());
                    if (productDto.getWarehouseId() != null) {
                        item.put("warehouse_id", productDto.getWarehouseId().toString());
                    }
                    return item;
                })
                .toList();

        String orderItemsJsonString;
        try {
            orderItemsJsonString = new ObjectMapper().writeValueAsString(orderItemsJson);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize order items to JSON", e);
        }

        // =====================================================
        // 6. CALL STORED PROCEDURE - SET JSONB TYPE IN JAVA
        // =====================================================

        // Create final copies for lambda
        final UUID finalUserId = user.getUserId();
        final UUID finalAddressId = address.getAddressId();
        final UUID finalCouponId = validatedCoupon != null ? validatedCoupon.getCouponId() : null;
        final String finalOrderItemsJson = orderItemsJsonString;
        final BigDecimal finalDiscountAmount = discountAmount;

        jdbcTemplate.update(connection -> {
            CallableStatement stmt = connection.prepareCall("CALL sp_place_order(?, ?, ?, ?, ?)");
            stmt.setObject(1, finalUserId);
            stmt.setObject(2, finalAddressId);
            stmt.setObject(3, finalOrderItemsJson, Types.OTHER);
            stmt.setObject(4, finalCouponId);
            stmt.setBigDecimal(5, finalDiscountAmount);
            return stmt;
        });

        // =====================================================
        // 7. QUERY FOR THE MOST RECENT ORDER BY THIS USER
        // =====================================================
        return orderRepository.findTopByUserOrderByCreatedAtDesc(user)
                .orElseThrow(() -> new IllegalStateException("Order not found after creation"));
    }

    public void deleteOrder(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found"));
        order.softDelete();
        orderRepository.save(order);
    }

    public Order save(Order order) {
        return orderRepository.save(order);
    }
}