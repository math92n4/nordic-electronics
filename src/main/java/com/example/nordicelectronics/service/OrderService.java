package com.example.nordicelectronics.service;

import com.example.nordicelectronics.entity.Order;
import com.example.nordicelectronics.repositories.sql.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class OrderService {

    @Autowired
    public OrderRepository orderRepository;

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Order getOrderById(UUID orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + orderId));
    }

    public Order[] getOrdersByIds(List<UUID> orderIds) {
        return orderIds.stream()
                .map(this::getOrderById)
                .toArray(Order[]::new);
    }

    public List<Order> getOrdersByUserId(UUID userId) {
        return orderRepository.findAll().stream()
                .toList();
        // TODO: Return orders by user when UserService is implemented
    }

    public Order createOrder(Order order) {
        return orderRepository.save(order);
    }

    public void deleteOrder(UUID orderId) {
        orderRepository.deleteById(orderId);
    }
}
