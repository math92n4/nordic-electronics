package com.example.nordicelectronics.controller.mongo;

import com.example.nordicelectronics.document.OrderDocument;
import com.example.nordicelectronics.service.mongo.OrderMongoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "MongoDB Order Controller", description = "Handles operations related to orders in MongoDB")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mongodb/orders")
public class OrderMongoController {

    private final OrderMongoService orderMongoService;

    @Operation(summary = "Get all orders from MongoDB", description = "Fetches a list of all orders from MongoDB.")
    @GetMapping("")
    public ResponseEntity<List<OrderDocument>> getAll() {
        return new ResponseEntity<>(orderMongoService.getAll(), HttpStatus.OK);
    }

    @Operation(summary = "Get order by ID from MongoDB", description = "Fetches an order by its unique ID from MongoDB.")
    @GetMapping("/{id}")
    public ResponseEntity<OrderDocument> getById(@PathVariable String id) {
        return new ResponseEntity<>(orderMongoService.getById(id), HttpStatus.OK);
    }

    @Operation(summary = "Create a new order in MongoDB", description = "Creates a new order and returns the created order.")
    @PostMapping("")
    public ResponseEntity<OrderDocument> save(@RequestBody OrderDocument order) {
        return new ResponseEntity<>(orderMongoService.save(order), HttpStatus.CREATED);
    }

    @Operation(summary = "Update an existing order in MongoDB", description = "Updates an existing order by its ID and returns the updated order.")
    @PutMapping("/{id}")
    public ResponseEntity<OrderDocument> update(@PathVariable String id, @RequestBody OrderDocument order) {
        return new ResponseEntity<>(orderMongoService.update(id, order), HttpStatus.OK);
    }

    @Operation(summary = "Delete an order from MongoDB", description = "Deletes an order by its unique ID from MongoDB.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        orderMongoService.deleteById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}

