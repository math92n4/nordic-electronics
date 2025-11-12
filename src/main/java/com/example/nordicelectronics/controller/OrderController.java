package com.example.nordicelectronics.controller;

import com.example.nordicelectronics.entity.Order;
import com.example.nordicelectronics.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @GetMapping("/by-user")
    public List<Order> getOrdersByUser(
            @RequestParam("userId") UUID userId) {
        return orderService.getOrdersByUserId(userId);
    }

    @GetMapping("/by-ids")
    public Order[] getOrders(List<UUID> orders) {
        return orderService.getOrdersByIds(orders);
    }

    @PostMapping("/create")
    public Order createOrder(@RequestBody Order order) {
        return orderService.createOrder(order);
    }

}
