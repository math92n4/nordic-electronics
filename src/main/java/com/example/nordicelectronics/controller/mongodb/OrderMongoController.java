package com.example.nordicelectronics.controller.mongodb;

import com.example.nordicelectronics.entity.enums.OrderStatus;
import com.example.nordicelectronics.entity.mongodb.OrderDocument;
import com.example.nordicelectronics.service.mongodb.OrderMongoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "MongoDB Order Controller", description = "Handles operations related to orders in MongoDB")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mongodb/orders")
public class OrderMongoController {

    private final OrderMongoService orderMongoService;

    @Operation(summary = "Get all MongoDB orders", description = "Fetches a list of all orders.")
    @GetMapping("")
    public ResponseEntity<List<OrderDocument>> getAll() {
        return new ResponseEntity<>(orderMongoService.getAll(), HttpStatus.OK);
    }

    @Operation(summary = "Get MongoDB order by ID", description = "Fetches an order by its unique ID.")
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDocument> getByOrderId(@PathVariable UUID orderId) {
        return new ResponseEntity<>(orderMongoService.getByOrderId(orderId), HttpStatus.OK);
    }

    @Operation(summary = "Get MongoDB orders by user ID", description = "Fetches all orders for a specific user.")
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<OrderDocument>> getByUserId(@PathVariable UUID userId) {
        return new ResponseEntity<>(orderMongoService.getByUserId(userId), HttpStatus.OK);
    }

    @Operation(summary = "Get MongoDB orders by status", description = "Fetches all orders with a specific status.")
    @GetMapping("/status/{status}")
    public ResponseEntity<List<OrderDocument>> getByStatus(@PathVariable OrderStatus status) {
        return new ResponseEntity<>(orderMongoService.getByStatus(status), HttpStatus.OK);
    }

    @Operation(summary = "Create a new MongoDB order", description = "Creates a new order and returns the created order.")
    @PostMapping("")
    public ResponseEntity<OrderDocument> save(@RequestBody OrderDocument orderDocument) {
        return new ResponseEntity<>(orderMongoService.save(orderDocument), HttpStatus.CREATED);
    }

    @Operation(summary = "Update an existing MongoDB order", description = "Updates an existing order by its ID and returns the updated order.")
    @PutMapping("/{orderId}")
    public ResponseEntity<OrderDocument> update(@PathVariable UUID orderId, @RequestBody OrderDocument orderDocument) {
        return new ResponseEntity<>(orderMongoService.update(orderId, orderDocument), HttpStatus.OK);
    }

    @Operation(summary = "Delete a MongoDB order", description = "Deletes an order by its unique ID.")
    @DeleteMapping("/{orderId}")
    public ResponseEntity<Void> delete(@PathVariable UUID orderId) {
        orderMongoService.deleteByOrderId(orderId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}

