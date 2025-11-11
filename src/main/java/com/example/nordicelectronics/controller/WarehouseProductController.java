package com.example.nordicelectronics.controller;

import com.example.nordicelectronics.entity.WarehouseProduct;
import com.example.nordicelectronics.service.WarehouseProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/warehouse-products")
@RequiredArgsConstructor
public class WarehouseProductController {

    private final WarehouseProductService warehouseProductService;

    @GetMapping("")
    public ResponseEntity<List<WarehouseProduct>> getAll() {
        return ResponseEntity.ok(warehouseProductService.getAll());
    }

    @GetMapping("/{warehouseId}/{productId}")
    public ResponseEntity<WarehouseProduct> getById(
            @PathVariable UUID warehouseId,
            @PathVariable UUID productId) {
        return ResponseEntity.ok(warehouseProductService.getById(warehouseId, productId));
    }

    @PostMapping("")
    public ResponseEntity<WarehouseProduct> save(@RequestBody WarehouseProduct warehouseProduct) {
        return new ResponseEntity<>(warehouseProductService.save(
                warehouseProduct.getWarehouse().getWarehouseId(),
                warehouseProduct.getProduct().getProductId(),
                warehouseProduct.getStockQuantity()), HttpStatus.CREATED);
    }

    @PutMapping("/{warehouseId}/{productId}/{stockQuantity}")
    public ResponseEntity<WarehouseProduct> updateStock(
            @PathVariable UUID warehouseId,
            @PathVariable UUID productId,
            @PathVariable int stockQuantity) {
        WarehouseProduct updated = warehouseProductService.updateStock(warehouseId, productId, stockQuantity);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{warehouseId}/{productId}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID warehouseId,
            @PathVariable UUID productId) {
        warehouseProductService.deleteById(warehouseId, productId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}
