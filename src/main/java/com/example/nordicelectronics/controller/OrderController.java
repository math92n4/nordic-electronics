package com.example.nordicelectronics.controller;

import com.example.nordicelectronics.entity.Order;
import com.example.nordicelectronics.entity.mapper.OrderMapper;
import com.example.nordicelectronics.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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

    @GetMapping("/by-ids") // Assuming this is your method signature
    public ResponseEntity<List<com.example.nordicelectronics.dto.OrderResponseDTO>> getOrdersByIds(@RequestParam List<UUID> ids) {

        // 1. Fetch entities (they contain lazy proxies)
        List<Order> orders = orderService.getOrdersByIds(ids);

        // 2. Map EACH entity to the safe DTO
        List<com.example.nordicelectronics.dto.OrderResponseDTO> responseDTOs = orders.stream()
                .map(OrderMapper::toResponseDTO)
                .collect(Collectors.toList());

        // 3. Return the safe list of DTOs
        return ResponseEntity.ok(responseDTOs);
    }

    @PostMapping("/create")
    public ResponseEntity<com.example.nordicelectronics.dto.OrderResponseDTO> createOrder(@RequestBody Order order) {

        // 1. Service successfully saves the order (which includes fetching and attaching User)
        Order savedOrder = orderService.createOrder(order);

        // 2. Map the saved entity to the DTO for a safe response
        com.example.nordicelectronics.dto.OrderResponseDTO responseDTO = OrderMapper.toResponseDTO(savedOrder);

        // 3. Return the DTO
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
    }

}
