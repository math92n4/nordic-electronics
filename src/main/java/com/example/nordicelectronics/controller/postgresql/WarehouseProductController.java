package com.example.nordicelectronics.controller.postgresql;

import com.example.nordicelectronics.entity.WarehouseProduct;
import com.example.nordicelectronics.service.WarehouseProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "PostgreSQL Warehouse Product Controller", description = "Handles operations related to warehouse products in PostgreSQL")
@RestController
@RequestMapping("/api/postgresql/warehouse-products")
@RequiredArgsConstructor
public class WarehouseProductController {

    private final WarehouseProductService warehouseProductService;

    @Operation(summary = "Get all PostgreSQL warehouse products", description = "Fetches all warehouse products from the system.")
    @GetMapping("")
    public ResponseEntity<List<WarehouseProduct>> getAll() {
        return ResponseEntity.ok(warehouseProductService.getAll());
    }

    @Operation(summary = "Get PostgreSQL warehouse product by IDs", description = "Fetches a warehouse product based on warehouse ID and product ID.")
    @GetMapping("/{warehouseId}/{productId}")
    public ResponseEntity<WarehouseProduct> getById(
            @PathVariable UUID warehouseId,
            @PathVariable UUID productId) {
        return ResponseEntity.ok(warehouseProductService.getById(warehouseId, productId));
    }

    @Operation(summary = "Create a new PostgreSQL warehouse product", description = "Creates a new warehouse product entry in the system.")
    @PostMapping("")
    public ResponseEntity<WarehouseProduct> save(@RequestBody WarehouseProduct warehouseProduct) {
        return new ResponseEntity<>(warehouseProductService.save(
                warehouseProduct.getWarehouse().getWarehouseId(),
                warehouseProduct.getProduct().getProductId(),
                warehouseProduct.getStockQuantity()), HttpStatus.CREATED);
    }

    @Operation(summary = "Update PostgreSQL stock quantity", description = "Updates the stock quantity for a specific warehouse product.")
    @PutMapping("/{warehouseId}/{productId}/{stockQuantity}")
    public ResponseEntity<WarehouseProduct> updateStock(
            @PathVariable UUID warehouseId,
            @PathVariable UUID productId,
            @PathVariable int stockQuantity) {
        WarehouseProduct updated = warehouseProductService.updateStock(warehouseId, productId, stockQuantity);
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Delete PostgreSQL warehouse product", description = "Deletes a warehouse product based on warehouse ID and product ID.")
    @DeleteMapping("/{warehouseId}/{productId}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID warehouseId,
            @PathVariable UUID productId) {
        warehouseProductService.deleteById(warehouseId, productId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}
