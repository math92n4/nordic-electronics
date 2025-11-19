package com.example.nordicelectronics.controller.mongo;

import com.example.nordicelectronics.document.WarehouseProductDocument;
import com.example.nordicelectronics.service.mongo.WarehouseProductMongoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "MongoDB Warehouse Product Controller", description = "Handles operations related to warehouse products in MongoDB")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mongodb/warehouse-products")
public class WarehouseProductMongoController {

    private final WarehouseProductMongoService warehouseProductMongoService;

    @Operation(summary = "Get all warehouse products from MongoDB", description = "Fetches all warehouse products from MongoDB.")
    @GetMapping("")
    public ResponseEntity<List<WarehouseProductDocument>> getAll() {
        return ResponseEntity.ok(warehouseProductMongoService.getAll());
    }

    @Operation(summary = "Get warehouse product by ID from MongoDB", description = "Fetches a warehouse product by its unique ID from MongoDB.")
    @GetMapping("/{id}")
    public ResponseEntity<WarehouseProductDocument> getById(@PathVariable String id) {
        return ResponseEntity.ok(warehouseProductMongoService.getById(id));
    }

    @Operation(summary = "Get warehouse product by warehouse and product IDs from MongoDB", description = "Fetches a warehouse product based on warehouse ID and product ID from MongoDB.")
    @GetMapping("/warehouse/{warehouseId}/product/{productId}")
    public ResponseEntity<WarehouseProductDocument> getByWarehouseAndProduct(
            @PathVariable String warehouseId,
            @PathVariable String productId) {
        return ResponseEntity.ok(warehouseProductMongoService.getByWarehouseAndProduct(warehouseId, productId));
    }

    @Operation(summary = "Get warehouse products by warehouse ID from MongoDB", description = "Fetches all products in a specific warehouse from MongoDB.")
    @GetMapping("/warehouse/{warehouseId}")
    public ResponseEntity<List<WarehouseProductDocument>> getByWarehouseId(@PathVariable String warehouseId) {
        return ResponseEntity.ok(warehouseProductMongoService.getByWarehouseId(warehouseId));
    }

    @Operation(summary = "Get warehouse products by product ID from MongoDB", description = "Fetches all warehouses containing a specific product from MongoDB.")
    @GetMapping("/product/{productId}")
    public ResponseEntity<List<WarehouseProductDocument>> getByProductId(@PathVariable String productId) {
        return ResponseEntity.ok(warehouseProductMongoService.getByProductId(productId));
    }

    @Operation(summary = "Create a new warehouse product in MongoDB", description = "Creates a new warehouse product entry in MongoDB.")
    @PostMapping("")
    public ResponseEntity<WarehouseProductDocument> save(@RequestBody WarehouseProductDocument warehouseProduct) {
        return new ResponseEntity<>(warehouseProductMongoService.save(warehouseProduct), HttpStatus.CREATED);
    }

    @Operation(summary = "Update stock quantity in MongoDB", description = "Updates the stock quantity for a specific warehouse product in MongoDB.")
    @PutMapping("/warehouse/{warehouseId}/product/{productId}/stock/{stockQuantity}")
    public ResponseEntity<WarehouseProductDocument> updateStock(
            @PathVariable String warehouseId,
            @PathVariable String productId,
            @PathVariable Integer stockQuantity) {
        WarehouseProductDocument updated = warehouseProductMongoService.updateStock(warehouseId, productId, stockQuantity);
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Delete warehouse product from MongoDB", description = "Deletes a warehouse product by its unique ID from MongoDB.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        warehouseProductMongoService.deleteById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Operation(summary = "Delete warehouse product by IDs from MongoDB", description = "Deletes a warehouse product based on warehouse ID and product ID from MongoDB.")
    @DeleteMapping("/warehouse/{warehouseId}/product/{productId}")
    public ResponseEntity<Void> deleteByWarehouseAndProduct(
            @PathVariable String warehouseId,
            @PathVariable String productId) {
        warehouseProductMongoService.deleteByWarehouseAndProduct(warehouseId, productId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}

