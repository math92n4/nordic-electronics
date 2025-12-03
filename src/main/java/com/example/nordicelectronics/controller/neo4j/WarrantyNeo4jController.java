package com.example.nordicelectronics.controller.neo4j;

import com.example.nordicelectronics.entity.neo4j.WarrantyNode;
import com.example.nordicelectronics.service.neo4j.WarrantyNeo4jService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Neo4j Warranty Controller", description = "Handles operations related to warranties in Neo4j")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/neo4j/warranties")
public class WarrantyNeo4jController {

    private final WarrantyNeo4jService warrantyNeo4jService;

    @Operation(summary = "Get all Neo4j warranties")
    @GetMapping("")
    public ResponseEntity<List<WarrantyNode>> getAll() {
        return new ResponseEntity<>(warrantyNeo4jService.getAll(), HttpStatus.OK);
    }

    @Operation(summary = "Get Neo4j warranty by ID")
    @GetMapping("/{warrantyId}")
    public ResponseEntity<WarrantyNode> getByWarrantyId(@PathVariable UUID warrantyId) {
        return new ResponseEntity<>(warrantyNeo4jService.getByWarrantyId(warrantyId), HttpStatus.OK);
    }

    @Operation(summary = "Get Neo4j warranty by product ID")
    @GetMapping("/product/{productId}")
    public ResponseEntity<WarrantyNode> getByProductId(@PathVariable UUID productId) {
        return new ResponseEntity<>(warrantyNeo4jService.getByProductId(productId), HttpStatus.OK);
    }

    @Operation(summary = "Create a new Neo4j warranty")
    @PostMapping("")
    public ResponseEntity<WarrantyNode> save(@RequestBody WarrantyNode warrantyNode) {
        return new ResponseEntity<>(warrantyNeo4jService.save(warrantyNode), HttpStatus.CREATED);
    }

    @Operation(summary = "Update an existing Neo4j warranty")
    @PutMapping("/{warrantyId}")
    public ResponseEntity<WarrantyNode> update(@PathVariable UUID warrantyId, @RequestBody WarrantyNode warrantyNode) {
        return new ResponseEntity<>(warrantyNeo4jService.update(warrantyId, warrantyNode), HttpStatus.OK);
    }

    @Operation(summary = "Delete a Neo4j warranty")
    @DeleteMapping("/{warrantyId}")
    public ResponseEntity<Void> delete(@PathVariable UUID warrantyId) {
        warrantyNeo4jService.deleteByWarrantyId(warrantyId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}

