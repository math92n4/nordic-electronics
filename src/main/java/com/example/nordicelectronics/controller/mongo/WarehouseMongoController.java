package com.example.nordicelectronics.controller.mongo;

import com.example.nordicelectronics.document.WarehouseDocument;
import com.example.nordicelectronics.service.mongo.WarehouseMongoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "MongoDB Warehouse Controller", description = "Handles operations related to warehouses in MongoDB")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mongodb/warehouses")
public class WarehouseMongoController {

    private final WarehouseMongoService warehouseMongoService;

    @Operation(summary = "Get all warehouses from MongoDB", description = "Fetches a list of all warehouses from MongoDB.")
    @GetMapping("")
    public ResponseEntity<List<WarehouseDocument>> getAll() {
        return new ResponseEntity<>(warehouseMongoService.getAll(), HttpStatus.OK);
    }

    @Operation(summary = "Get warehouse by ID from MongoDB", description = "Fetches a warehouse by its unique ID from MongoDB.")
    @GetMapping("/{id}")
    public ResponseEntity<WarehouseDocument> getById(@PathVariable String id) {
        return new ResponseEntity<>(warehouseMongoService.getById(id), HttpStatus.OK);
    }

    @Operation(summary = "Create a new warehouse in MongoDB", description = "Creates a new warehouse and returns the created warehouse.")
    @PostMapping("")
    public ResponseEntity<WarehouseDocument> save(@RequestBody WarehouseDocument warehouse) {
        return new ResponseEntity<>(warehouseMongoService.save(warehouse), HttpStatus.CREATED);
    }

    @Operation(summary = "Update an existing warehouse in MongoDB", description = "Updates an existing warehouse by its ID and returns the updated warehouse.")
    @PutMapping("/{id}")
    public ResponseEntity<WarehouseDocument> update(@PathVariable String id, @RequestBody WarehouseDocument warehouse) {
        return new ResponseEntity<>(warehouseMongoService.update(id, warehouse), HttpStatus.OK);
    }

    @Operation(summary = "Delete a warehouse from MongoDB", description = "Deletes a warehouse by its unique ID from MongoDB.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        warehouseMongoService.deleteById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}

