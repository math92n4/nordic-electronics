package com.example.nordicelectronics.controller.neo4j;

import com.example.nordicelectronics.entity.neo4j.ProductNode;
import com.example.nordicelectronics.service.neo4j.ProductNeo4jService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Neo4j Product Controller", description = "Handles operations related to products in Neo4j")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/neo4j/products")
public class ProductNeo4jController {

    private final ProductNeo4jService productNeo4jService;

    @Operation(summary = "Get all Neo4j products")
    @GetMapping("")
    public ResponseEntity<List<ProductNode>> getAll() {
        return new ResponseEntity<>(productNeo4jService.getAll(), HttpStatus.OK);
    }

    @Operation(summary = "Get Neo4j product by ID")
    @GetMapping("/{productId}")
    public ResponseEntity<ProductNode> getByProductId(@PathVariable UUID productId) {
        return new ResponseEntity<>(productNeo4jService.getByProductId(productId), HttpStatus.OK);
    }

    @Operation(summary = "Get Neo4j product by SKU")
    @GetMapping("/sku/{sku}")
    public ResponseEntity<ProductNode> getBySku(@PathVariable String sku) {
        return new ResponseEntity<>(productNeo4jService.getBySku(sku), HttpStatus.OK);
    }

    @Operation(summary = "Get Neo4j products by brand ID")
    @GetMapping("/brand/{brandId}")
    public ResponseEntity<List<ProductNode>> getByBrandId(@PathVariable UUID brandId) {
        return new ResponseEntity<>(productNeo4jService.getByBrandId(brandId), HttpStatus.OK);
    }

    @Operation(summary = "Get Neo4j products by category ID")
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<ProductNode>> getByCategoryId(@PathVariable UUID categoryId) {
        return new ResponseEntity<>(productNeo4jService.getByCategoryId(categoryId), HttpStatus.OK);
    }

    @Operation(summary = "Create a new Neo4j product")
    @PostMapping("")
    public ResponseEntity<ProductNode> save(@RequestBody ProductNode productNode) {
        return new ResponseEntity<>(productNeo4jService.save(productNode), HttpStatus.CREATED);
    }

    @Operation(summary = "Update an existing Neo4j product")
    @PutMapping("/{productId}")
    public ResponseEntity<ProductNode> update(@PathVariable UUID productId, @RequestBody ProductNode productNode) {
        return new ResponseEntity<>(productNeo4jService.update(productId, productNode), HttpStatus.OK);
    }

    @Operation(summary = "Delete a Neo4j product")
    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> delete(@PathVariable UUID productId) {
        productNeo4jService.deleteByProductId(productId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}

