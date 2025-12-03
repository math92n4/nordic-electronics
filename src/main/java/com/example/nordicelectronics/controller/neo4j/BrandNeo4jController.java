package com.example.nordicelectronics.controller.neo4j;

import com.example.nordicelectronics.entity.neo4j.BrandNode;
import com.example.nordicelectronics.service.neo4j.BrandNeo4jService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Neo4j Brand Controller", description = "Handles operations related to brands in Neo4j")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/neo4j/brands")
public class BrandNeo4jController {

    private final BrandNeo4jService brandNeo4jService;

    @Operation(summary = "Get all Neo4j brands")
    @GetMapping("")
    public ResponseEntity<List<BrandNode>> getAll() {
        return new ResponseEntity<>(brandNeo4jService.getAll(), HttpStatus.OK);
    }

    @Operation(summary = "Get Neo4j brand by ID")
    @GetMapping("/{brandId}")
    public ResponseEntity<BrandNode> getByBrandId(@PathVariable UUID brandId) {
        return new ResponseEntity<>(brandNeo4jService.getByBrandId(brandId), HttpStatus.OK);
    }

    @Operation(summary = "Create a new Neo4j brand")
    @PostMapping("")
    public ResponseEntity<BrandNode> save(@RequestBody BrandNode brandNode) {
        return new ResponseEntity<>(brandNeo4jService.save(brandNode), HttpStatus.CREATED);
    }

    @Operation(summary = "Update an existing Neo4j brand")
    @PutMapping("/{brandId}")
    public ResponseEntity<BrandNode> update(@PathVariable UUID brandId, @RequestBody BrandNode brandNode) {
        return new ResponseEntity<>(brandNeo4jService.update(brandId, brandNode), HttpStatus.OK);
    }

    @Operation(summary = "Delete a Neo4j brand")
    @DeleteMapping("/{brandId}")
    public ResponseEntity<Void> delete(@PathVariable UUID brandId) {
        brandNeo4jService.deleteByBrandId(brandId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}

