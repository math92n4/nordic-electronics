package com.example.nordicelectronics.service;

import com.example.nordicelectronics.entity.Order;
import com.example.nordicelectronics.entity.User;
import com.example.nordicelectronics.repositories.sql.OrderRepository;
import com.example.nordicelectronics.repositories.sql.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class OrderService {

    @Autowired
    public OrderRepository orderRepository;

    @Autowired
    public UserRepository userRepository;

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

    // Inside com.example.nordicelectronics.service.OrderService.java

    public Order createOrder(Order order) {
        // 1. Get the User object from the incoming 'order' entity.
        //    This User object is likely only populated with the 'userId' from JSON.
        User incomingUser = order.getUser();

        // Safely check if the incoming user object is null OR if its ID is null
        if (incomingUser == null || incomingUser.getUserId() == null) {
            // Throw an explicit exception if the required user data is missing in the payload
            throw new IllegalArgumentException("Order request must contain a valid 'user' object with a 'userId'.");
        }

        // 2. Extract the ID and fetch the *managed* entity from the database
        UUID userId = incomingUser.getUserId();

        User managedUser = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        // 3. Attach the fully managed and non-null User entity back to the Order
        order.setUser(managedUser);

        // --- END: Fix for NullPointerException on User ---

        // 4. Any other logic (calculate totals, set dates, etc.)
        // ...

        // 5. Save the final order
        return orderRepository.save(order); // Line 42 might be here or just before here
    }

    public void deleteOrder(UUID orderId) {
        orderRepository.deleteById(orderId);
    }
}
