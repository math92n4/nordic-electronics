package com.example.nordicelectronics.controller.postgresql;

import com.example.nordicelectronics.entity.Brand;
import com.example.nordicelectronics.entity.Product;
import com.example.nordicelectronics.entity.Warehouse;
import com.example.nordicelectronics.service.BrandService;
import com.example.nordicelectronics.service.WarehouseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "PostgreSQL Warehouse Controller", description = "Handles operations related to warehouses in PostgreSQL")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/postgresql/warehouses")
public class WarehouseController {


    private final WarehouseService warehouseService;

    @Operation(summary = "Get all PostgreSQL warehouses", description = "Fetches a list of all warehouses.")
    @GetMapping("")
    public ResponseEntity<List<Warehouse>> getAll() {
        return new ResponseEntity<>(warehouseService.getAll(), HttpStatus.OK);
    }

    @Operation(summary = "Get PostgreSQL warehouse by ID", description = "Fetches a warehouse by its unique ID.")
    @GetMapping("/{id}")
    public ResponseEntity<Warehouse> getById(@PathVariable UUID id) {
        return new ResponseEntity<>(warehouseService.getById(id), HttpStatus.OK);
    }

    @Operation(summary = "Create a new PostgreSQL warehouse", description = "Creates a new warehouse and returns the created warehouse.")
    @PostMapping("")
    public ResponseEntity<Warehouse> save(@RequestBody Warehouse warehouse) {
        return new ResponseEntity<>(warehouseService.save(warehouse), HttpStatus.CREATED);
    }

    @Operation(summary = "Update an existing PostgreSQL warehouse", description = "Updates an existing warehouse by its ID and returns the updated warehouse.")
    @PutMapping("/{id}")
    public ResponseEntity<Warehouse> update(@PathVariable UUID id, @RequestBody Warehouse warehouse) {
        return new ResponseEntity<>(warehouseService.update(id, warehouse), HttpStatus.OK);
    }

    @Operation(summary = "Delete a PostgreSQL warehouse", description = "Deletes a warehouse by its unique ID.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Product> delete(@PathVariable UUID id) {
        warehouseService.deleteById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
