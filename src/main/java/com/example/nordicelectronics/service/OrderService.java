package com.example.nordicelectronics.service;

import com.example.nordicelectronics.entity.*;
import com.example.nordicelectronics.entity.dto.address.AddressRequestDTO;
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
                .toList();
        // TODO: Return orders by user when UserService is implemented
    }


    @Transactional
    public Order createOrder(OrderRequestDTO dto) {
        if (dto.getUserId() == null) {
            throw new IllegalArgumentException("Order must contain a valid userId.");
        }

        // 1. Get User
        User managedUser = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + dto.getUserId()));

        // 2. Create and save Address from DTO
        Address address = null;
        if (dto.getAddress() != null) {
            AddressRequestDTO addressDTO = dto.getAddress();
            address = Address.builder()
                    .street(addressDTO.getStreet())
                    .streetNumber(addressDTO.getStreetNumber())
                    .zip(addressDTO.getZip())
                    .city(addressDTO.getCity())
                    .user(managedUser)
                    .build();
            address = addressRepository.save(address);
        } else {
            // Try to use user's first address if available
            if (managedUser.getAddress() != null && !managedUser.getAddress().isEmpty()) {
                address = managedUser.getAddress().stream().findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("User has no address and no address provided in order."));
            } else {
                throw new IllegalArgumentException("Order must contain a valid address.");
            }
        }

        // 3. Get Coupon if couponCode is provided
        Coupon coupon = null;
        if (dto.getCouponCode() != null && !dto.getCouponCode().isEmpty()) {
            coupon = couponRepository.findByCode(dto.getCouponCode())
                    .orElse(null); // Coupon is optional, so we don't throw if not found
        }

        // 4. Create Order
        Order order = Order.builder()
                .user(managedUser)
                .address(address)
                .coupon(coupon)
                .orderDate(LocalDateTime.now())
                .orderStatus(OrderStatus.pending)
                .subtotal(BigDecimal.ZERO)
                .taxAmount(BigDecimal.ZERO)
                .shippingCost(BigDecimal.ZERO)
                .totalAmount(BigDecimal.ZERO)
                .orderProducts(new ArrayList<>())
                .build();

        // 5. Create OrderProducts from DTO
        BigDecimal subtotal = BigDecimal.ZERO;
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
                    .order(order)
                    .build();

            order.getOrderProducts().add(orderProduct);
        }

        // Set subtotal
        order.setSubtotal(subtotal);
        // TODO: Calculate tax, shipping, discount, and total
        order.setTotalAmount(subtotal); // Placeholder

        // 6. Save Order
        Order savedOrder = orderRepository.save(order);

        // 7. Save OrderProducts with composite keys
        for (OrderProduct op : savedOrder.getOrderProducts()) {
            OrderProductKey key = new OrderProductKey(
                    savedOrder.getOrderId(),
                    op.getProduct().getProductId()
            );
            op.setId(key);
            orderProductRepository.save(op);
        }

        // 8. CALL stored procedure: sp_ProcessOrder
        entityManager
                .createNativeQuery("CALL sp_ProcessOrder(:orderId)")
                .setParameter("orderId", savedOrder.getOrderId())
                .executeUpdate();

        // 9. Return the final order
        return orderRepository.findById(savedOrder.getOrderId())
                .orElseThrow(() -> new IllegalStateException("Order vanished after creation!"));
    }


    public void deleteOrder(UUID orderId) {
        orderRepository.deleteById(orderId);
    }
}
