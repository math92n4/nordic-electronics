package com.example.nordicelectronics.controller.neo4j;

import com.example.nordicelectronics.entity.neo4j.CategoryNode;
import com.example.nordicelectronics.service.neo4j.CategoryNeo4jService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Neo4j Category Controller", description = "Handles operations related to categories in Neo4j")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/neo4j/categories")
public class CategoryNeo4jController {

    private final CategoryNeo4jService categoryNeo4jService;

    @Operation(summary = "Get all Neo4j categories")
    @GetMapping("")
    public ResponseEntity<List<CategoryNode>> getAll() {
        return new ResponseEntity<>(categoryNeo4jService.getAll(), HttpStatus.OK);
    }

    @Operation(summary = "Get Neo4j category by ID")
    @GetMapping("/{categoryId}")
    public ResponseEntity<CategoryNode> getByCategoryId(@PathVariable UUID categoryId) {
        return new ResponseEntity<>(categoryNeo4jService.getByCategoryId(categoryId), HttpStatus.OK);
    }

    @Operation(summary = "Create a new Neo4j category")
    @PostMapping("")
    public ResponseEntity<CategoryNode> save(@RequestBody CategoryNode categoryNode) {
        return new ResponseEntity<>(categoryNeo4jService.save(categoryNode), HttpStatus.CREATED);
    }

    @Operation(summary = "Update an existing Neo4j category")
    @PutMapping("/{categoryId}")
    public ResponseEntity<CategoryNode> update(@PathVariable UUID categoryId, @RequestBody CategoryNode categoryNode) {
        return new ResponseEntity<>(categoryNeo4jService.update(categoryId, categoryNode), HttpStatus.OK);
    }

    @Operation(summary = "Delete a Neo4j category")
    @DeleteMapping("/{categoryId}")
    public ResponseEntity<Void> delete(@PathVariable UUID categoryId) {
        categoryNeo4jService.deleteByCategoryId(categoryId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}

