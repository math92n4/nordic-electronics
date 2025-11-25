package com.example.nordicelectronics.service;

import com.example.nordicelectronics.entity.*;
import com.example.nordicelectronics.repositories.sql.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;
    @PersistenceContext
    private EntityManager entityManager;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private OrderProductRepository orderProductRepository;
    @Autowired
    private ProductRepository productRepository;

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
    public Order createOrder(Order order) {

        if (order.getUser() == null || order.getUser().getUserId() == null) {
            throw new IllegalArgumentException("Order must contain a valid userId.");
        }

        UUID userId = order.getUser().getUserId();

        User managedUser = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        order.setUser(managedUser);


        if (managedUser.getAddress() == null || managedUser.getAddress().getAddressId() == null) {
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
