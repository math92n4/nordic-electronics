package com.example.nordicelectronics.controller.mongodb;

import com.example.nordicelectronics.entity.mongodb.CategoryDocument;
import com.example.nordicelectronics.service.mongodb.CategoryMongoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "MongoDB Category Controller", description = "Handles operations related to categories in MongoDB")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mongodb/categories")
public class CategoryMongoController {

    private final CategoryMongoService categoryMongoService;

    @Operation(summary = "Get all MongoDB categories", description = "Fetches a list of all categories.")
    @GetMapping("")
    public ResponseEntity<List<CategoryDocument>> getAll() {
        return new ResponseEntity<>(categoryMongoService.getAll(), HttpStatus.OK);
    }

    @Operation(summary = "Get MongoDB category by ID", description = "Fetches a category by its unique ID.")
    @GetMapping("/{categoryId}")
    public ResponseEntity<CategoryDocument> getByCategoryId(@PathVariable UUID categoryId) {
        return new ResponseEntity<>(categoryMongoService.getByCategoryId(categoryId), HttpStatus.OK);
    }

    @Operation(summary = "Create a new MongoDB category", description = "Creates a new category and returns the created category.")
    @PostMapping("")
    public ResponseEntity<CategoryDocument> save(@RequestBody CategoryDocument categoryDocument) {
        return new ResponseEntity<>(categoryMongoService.save(categoryDocument), HttpStatus.CREATED);
    }

    @Operation(summary = "Update an existing MongoDB category", description = "Updates an existing category by its ID and returns the updated category.")
    @PutMapping("/{categoryId}")
    public ResponseEntity<CategoryDocument> update(@PathVariable UUID categoryId, @RequestBody CategoryDocument categoryDocument) {
        return new ResponseEntity<>(categoryMongoService.update(categoryId, categoryDocument), HttpStatus.OK);
    }

    @Operation(summary = "Delete a MongoDB category", description = "Deletes a category by its unique ID.")
    @DeleteMapping("/{categoryId}")
    public ResponseEntity<Void> delete(@PathVariable UUID categoryId) {
        categoryMongoService.deleteByCategoryId(categoryId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}

