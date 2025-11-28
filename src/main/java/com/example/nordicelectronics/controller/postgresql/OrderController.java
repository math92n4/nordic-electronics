package com.example.nordicelectronics.controller.postgresql;

import com.example.nordicelectronics.entity.Order;
import com.example.nordicelectronics.entity.dto.order.OrderRequestDTO;
import com.example.nordicelectronics.entity.dto.order.OrderResponseDTO;
import com.example.nordicelectronics.entity.mapper.OrderMapper;
import com.example.nordicelectronics.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Tag(name ="PostgreSQL Order Controller", description = "Handles operations related to orders in PostgreSQL")
@RestController
@RequestMapping("api/postgresql/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Operation(summary = "Get PostgreSQL orders by user ID", description = "Fetches all orders associated with a specific user ID.")
    @GetMapping("/by-user")
    public ResponseEntity<List<OrderResponseDTO>> getOrdersByUser(
            @RequestParam("userId") UUID userId) {
        // Fetch entities
        List<Order> orders = orderService.getOrdersByUserId(userId);
        
        // Convert to DTOs to avoid lazy loading issues
        List<OrderResponseDTO> responseDTOs = orders.stream()
                .map(OrderMapper::toResponseDTO)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(responseDTOs);
    }

    @Operation(summary = "Get PostgreSQL orders by IDs", description = "Fetches orders based on a list of order IDs and returns them as DTOs.")
    @GetMapping("/by-ids") // Assuming this is your method signature
    public ResponseEntity<List<OrderResponseDTO>> getOrdersByIds(@RequestParam List<UUID> ids) {

        // 1. Fetch entities (they contain lazy proxies)
        List<Order> orders = orderService.getOrdersByIds(ids);

        // 2. Map EACH entity to the safe DTO
        List<OrderResponseDTO> responseDTOs = orders.stream()
                .map(OrderMapper::toResponseDTO)
                .collect(Collectors.toList());

        // 3. Return the safe list of DTOs
        return ResponseEntity.ok(responseDTOs);
    }

    @Operation(summary = "Create a new PostgreSQL order", description = "Creates a new PostgreSQL order and returns the created order as a DTO.")
    @PostMapping("/create")
    public ResponseEntity<OrderResponseDTO> createOrder(@RequestBody OrderRequestDTO dto) {

        // 1. Service successfully saves the order (which includes fetching and attaching User)
        Order savedOrder = orderService.createOrder(dto);

        // 2. Map the saved entity to the DTO for a safe response
        OrderResponseDTO responseDTO = OrderMapper.toResponseDTO(savedOrder);

        // 3. Return the DTO
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
    }
}
