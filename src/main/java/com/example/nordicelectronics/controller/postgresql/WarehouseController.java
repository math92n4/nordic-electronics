package com.example.nordicelectronics.controller.postgresql;

import com.example.nordicelectronics.entity.dto.warehouse.WarehouseRequestDTO;
import com.example.nordicelectronics.entity.dto.warehouse.WarehouseResponseDTO;
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
    public ResponseEntity<List<WarehouseResponseDTO>> getAll() {
        return new ResponseEntity<>(warehouseService.getAll(), HttpStatus.OK);
    }

    @Operation(summary = "Get PostgreSQL warehouse by ID", description = "Fetches a warehouse by its unique ID.")
    @GetMapping("/{id}")
    public ResponseEntity<WarehouseResponseDTO> getById(@PathVariable UUID id) {
        return new ResponseEntity<>(warehouseService.getById(id), HttpStatus.OK);
    }

    @Operation(summary = "Create a new PostgreSQL warehouse", description = "Creates a new warehouse and returns the created warehouse.")
    @PostMapping("")
    public ResponseEntity<WarehouseResponseDTO> save(@RequestBody WarehouseRequestDTO dto) {
        return new ResponseEntity<>(warehouseService.save(dto), HttpStatus.CREATED);
    }

    @Operation(summary = "Update an existing PostgreSQL warehouse", description = "Updates an existing warehouse by its ID and returns the updated warehouse.")
    @PutMapping("/{id}")
    public ResponseEntity<WarehouseResponseDTO> update(@PathVariable UUID id, @RequestBody WarehouseRequestDTO dto) {
        return new ResponseEntity<>(warehouseService.update(id, dto), HttpStatus.OK);
    }

    @Operation(summary = "Delete a PostgreSQL warehouse", description = "Deletes a warehouse by its unique ID.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        warehouseService.deleteById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
