package com.example.nordicelectronics.controller.mongodb;

import com.example.nordicelectronics.entity.mongodb.WarehouseDocument;
import com.example.nordicelectronics.service.mongodb.WarehouseMongoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "MongoDB Warehouse Controller", description = "Handles operations related to warehouses in MongoDB")
@RestController
@ConditionalOnProperty(name = "spring.data.mongodb.enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
@RequestMapping("/api/mongodb/warehouses")
public class WarehouseMongoController {

    private final WarehouseMongoService warehouseMongoService;

    @Operation(summary = "Get all MongoDB warehouses", description = "Fetches a list of all warehouses.")
    @GetMapping("")
    public ResponseEntity<List<WarehouseDocument>> getAll() {
        return new ResponseEntity<>(warehouseMongoService.getAll(), HttpStatus.OK);
    }

    @Operation(summary = "Get MongoDB warehouse by ID", description = "Fetches a warehouse by its unique ID.")
    @GetMapping("/{warehouseId}")
    public ResponseEntity<WarehouseDocument> getByWarehouseId(@PathVariable UUID warehouseId) {
        return new ResponseEntity<>(warehouseMongoService.getByWarehouseId(warehouseId), HttpStatus.OK);
    }

    @Operation(summary = "Create a new MongoDB warehouse", description = "Creates a new warehouse and returns the created warehouse.")
    @PostMapping("")
    public ResponseEntity<WarehouseDocument> save(@RequestBody WarehouseDocument warehouseDocument) {
        return new ResponseEntity<>(warehouseMongoService.save(warehouseDocument), HttpStatus.CREATED);
    }

    @Operation(summary = "Update an existing MongoDB warehouse", description = "Updates an existing warehouse by its ID and returns the updated warehouse.")
    @PutMapping("/{warehouseId}")
    public ResponseEntity<WarehouseDocument> update(@PathVariable UUID warehouseId, @RequestBody WarehouseDocument warehouseDocument) {
        return new ResponseEntity<>(warehouseMongoService.update(warehouseId, warehouseDocument), HttpStatus.OK);
    }

    @Operation(summary = "Delete a MongoDB warehouse", description = "Deletes a warehouse by its unique ID.")
    @DeleteMapping("/{warehouseId}")
    public ResponseEntity<Void> delete(@PathVariable UUID warehouseId) {
        warehouseMongoService.deleteByWarehouseId(warehouseId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}

