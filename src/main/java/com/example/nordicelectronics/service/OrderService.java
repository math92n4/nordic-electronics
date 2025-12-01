package com.example.nordicelectronics.service;

import com.example.nordicelectronics.entity.*;
import com.example.nordicelectronics.entity.dto.order.OrderProductRequestDTO;
import com.example.nordicelectronics.entity.dto.order.OrderRequestDTO;
import com.example.nordicelectronics.entity.enums.OrderStatus;
import com.example.nordicelectronics.repositories.sql.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    @PersistenceContext
    private final EntityManager entityManager;
    private final UserRepository userRepository;
    private final OrderProductRepository orderProductRepository;
    private final ProductRepository productRepository;
    private final AddressRepository addressRepository;
    private final CouponRepository couponRepository;

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Order getOrderById(UUID orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + orderId));
    }

    public List<Order> getOrdersByIds(List<UUID> orderIds) {
        return orderRepository.findAllById(orderIds);
    }

    public List<Order> getOrdersByUserId(UUID userId) {
        return orderRepository.findAll().stream()
                .filter(order -> order.getUser() != null && order.getUser().getUserId().equals(userId))
                .filter(order -> order.getDeletedAt() == null)
                .toList();
    }

    @Transactional
    public Order createOrder(OrderRequestDTO dto) {
        // Convert DTO to Entity
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + dto.getUserId()));

        // Get user's first address or use provided address
        Address address = null;
        if (dto.getAddress() != null) {
            // Create or find address from DTO
            address = Address.builder()
                    .street(dto.getAddress().getStreet())
                    .streetNumber(dto.getAddress().getStreetNumber())
                    .zip(dto.getAddress().getZip())
                    .city(dto.getAddress().getCity())
                    .user(user)
                    .build();
            address = addressRepository.save(address);
        } else if (user.getAddress() != null && !user.getAddress().isEmpty()) {
            address = user.getAddress().get(0);
        } else {
            throw new IllegalArgumentException("Order must contain a valid address.");
        }

        // Build OrderProducts from DTO
        List<OrderProduct> orderProducts = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;

        if (dto.getOrderProducts() == null || dto.getOrderProducts().isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one product.");
        }

        for (OrderProductRequestDTO opDto : dto.getOrderProducts()) {
            Product product = productRepository.findById(opDto.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found: " + opDto.getProductId()));

            BigDecimal unitPrice = product.getPrice();
            BigDecimal totalPrice = unitPrice.multiply(BigDecimal.valueOf(opDto.getQuantity()));
            subtotal = subtotal.add(totalPrice);

            OrderProduct orderProduct = OrderProduct.builder()
                    .product(product)
                    .quantity(opDto.getQuantity())
                    .unitPrice(unitPrice)
                    .totalPrice(totalPrice)
                    .build();

            orderProducts.add(orderProduct);
        }

        // Calculate totals
        BigDecimal taxAmount = subtotal.multiply(BigDecimal.valueOf(0.20)); // 20% tax
        BigDecimal shippingCost = BigDecimal.valueOf(10.00); // Fixed shipping
        BigDecimal discountAmount = BigDecimal.ZERO;

        // Apply coupon if provided
        if (dto.getCouponCode() != null && !dto.getCouponCode().isBlank()) {
            var coupon = couponRepository.findByCode(dto.getCouponCode())
                    .orElse(null);
            if (coupon != null && coupon.isActive()) {
                // Calculate discount based on coupon type
                // This is simplified - you may need to implement full coupon logic
                discountAmount = subtotal.multiply(BigDecimal.valueOf(0.10)); // 10% discount example
            }
        }

        BigDecimal totalAmount = subtotal.add(taxAmount).add(shippingCost).subtract(discountAmount);

        // Build Order entity (initialize with empty list to avoid null)
        Order order = Order.builder()
                .user(user)
                .address(address)
                .orderDate(LocalDateTime.now())
                .orderStatus(OrderStatus.pending)
                .subtotal(subtotal)
                .taxAmount(taxAmount)
                .shippingCost(shippingCost)
                .discountAmount(discountAmount)
                .totalAmount(totalAmount)
                .orderProducts(new ArrayList<>())
                .build();

        // Save order first to get the orderId (flush to ensure ID is available)
        Order savedOrder = orderRepository.save(order);
        orderRepository.flush(); // Ensure the order ID is persisted before using it

        // Now create and save order products with composite keys directly (like PostgresSeeder)
        // We save them directly via repository to avoid orphan removal issues
        for (OrderProduct op : orderProducts) {
            // Create composite key with both IDs (matching PostgresSeeder pattern)
            OrderProductKey key = new OrderProductKey(
                    savedOrder.getOrderId(),
                    op.getProduct().getProductId()
            );
            // Set the composite key
            op.setId(key);
            // Set the order reference (required for relationship)
            op.setOrder(savedOrder);
            // Save directly via repository (don't add to collection to avoid orphan removal)
            orderProductRepository.save(op);
        }
        
        // Don't update the Order's collection - Hibernate will load them via the relationship
        // The OrderProducts are already saved and linked via the order_id foreign key

        // Note: Stock deduction and payment processing should be handled separately
        // The order is created and saved at this point
        
        // Return the final order (refresh to ensure all relationships are loaded)
        return orderRepository.findById(savedOrder.getOrderId())
                .orElseThrow(() -> new IllegalStateException("Order vanished after creation!"));
    }

    @Transactional
    public Order createOrder(Order order) {

        if (order.getUser() == null || order.getUser().getUserId() == null) {
            throw new IllegalArgumentException("Order must contain a valid userId.");
        }

        UUID userId = order.getUser().getUserId();

        User managedUser = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        order.setUser(managedUser);


        if (managedUser.getAddress() == null || managedUser.getAddress().stream().findFirst() == null) {
            throw new IllegalArgumentException("Order must contain a valid shippingAddressId.");
        }


        for (OrderProduct op : order.getOrderProducts()) {
            UUID productId = op.getProduct().getProductId();

            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new RuntimeException("Product not found: " + productId));

            op.setOrder(order);
            op.setProduct(product);

        }

        Order savedOrder = orderRepository.save(order);

        for (OrderProduct op : order.getOrderProducts()) {

            // Build composite key
            OrderProductKey key = new OrderProductKey(
                    savedOrder.getOrderId(),
                    op.getProduct().getProductId()
            );

            op.setId(key);
            orderProductRepository.save(op);
        }


        // ===============================
        // 6. CALL stored procedure: sp_ProcessOrder
        //    → stock deduction
        //    → payment completed
        //    → order.status = confirmed
        // ===============================
        entityManager
                .createNativeQuery("CALL sp_ProcessOrder(:orderId)")
                .setParameter("orderId", savedOrder.getOrderId())
                .executeUpdate();


        // ===============================
        // 7. CALL shipping cost calculation
        // ===============================
        /*
        entityManager
                .createNativeQuery("CALL sp_CalculateShipping(:orderId)")
                .setParameter("orderId", savedOrder.getOrderId())
                .executeUpdate();
        */

        // ===============================
        // 8. Return the final order
        // ===============================
        return orderRepository.findById(savedOrder.getOrderId())
                .orElseThrow(() -> new IllegalStateException("Order vanished after creation!"));
    }


    public void deleteOrder(UUID orderId) {
        orderRepository.deleteById(orderId);
    }
}
