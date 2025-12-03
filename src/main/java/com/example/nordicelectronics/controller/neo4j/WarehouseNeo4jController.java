package com.example.nordicelectronics.controller.neo4j;

import com.example.nordicelectronics.entity.neo4j.WarehouseNode;
import com.example.nordicelectronics.service.neo4j.WarehouseNeo4jService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Neo4j Warehouse Controller", description = "Handles operations related to warehouses in Neo4j")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/neo4j/warehouses")
public class WarehouseNeo4jController {

    private final WarehouseNeo4jService warehouseNeo4jService;

    @Operation(summary = "Get all Neo4j warehouses")
    @GetMapping("")
    public ResponseEntity<List<WarehouseNode>> getAll() {
        return new ResponseEntity<>(warehouseNeo4jService.getAll(), HttpStatus.OK);
    }

    @Operation(summary = "Get Neo4j warehouse by ID")
    @GetMapping("/{warehouseId}")
    public ResponseEntity<WarehouseNode> getByWarehouseId(@PathVariable UUID warehouseId) {
        return new ResponseEntity<>(warehouseNeo4jService.getByWarehouseId(warehouseId), HttpStatus.OK);
    }

    @Operation(summary = "Create a new Neo4j warehouse")
    @PostMapping("")
    public ResponseEntity<WarehouseNode> save(@RequestBody WarehouseNode warehouseNode) {
        return new ResponseEntity<>(warehouseNeo4jService.save(warehouseNode), HttpStatus.CREATED);
    }

    @Operation(summary = "Update an existing Neo4j warehouse")
    @PutMapping("/{warehouseId}")
    public ResponseEntity<WarehouseNode> update(@PathVariable UUID warehouseId, @RequestBody WarehouseNode warehouseNode) {
        return new ResponseEntity<>(warehouseNeo4jService.update(warehouseId, warehouseNode), HttpStatus.OK);
    }

    @Operation(summary = "Delete a Neo4j warehouse")
    @DeleteMapping("/{warehouseId}")
    public ResponseEntity<Void> delete(@PathVariable UUID warehouseId) {
        warehouseNeo4jService.deleteByWarehouseId(warehouseId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}

