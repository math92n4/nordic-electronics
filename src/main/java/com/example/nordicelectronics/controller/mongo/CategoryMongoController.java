package com.example.nordicelectronics.controller.mongo;

import com.example.nordicelectronics.document.CategoryDocument;
import com.example.nordicelectronics.service.mongo.CategoryMongoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "MongoDB Category Controller", description = "Handles operations related to categories in MongoDB")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mongodb/categories")
public class CategoryMongoController {

    private final CategoryMongoService categoryMongoService;

    @Operation(summary = "Get all categories from MongoDB", description = "Fetches a list of all categories from MongoDB.")
    @GetMapping("")
    public ResponseEntity<List<CategoryDocument>> getAll() {
        return new ResponseEntity<>(categoryMongoService.getAll(), HttpStatus.OK);
    }

    @Operation(summary = "Get category by ID from MongoDB", description = "Fetches a category by its unique ID from MongoDB.")
    @GetMapping("/{id}")
    public ResponseEntity<CategoryDocument> getById(@PathVariable String id) {
        return new ResponseEntity<>(categoryMongoService.getById(id), HttpStatus.OK);
    }

    @Operation(summary = "Create a new category in MongoDB", description = "Creates a new category and returns the created category.")
    @PostMapping("")
    public ResponseEntity<CategoryDocument> save(@RequestBody CategoryDocument category) {
        return new ResponseEntity<>(categoryMongoService.save(category), HttpStatus.CREATED);
    }

    @Operation(summary = "Update an existing category in MongoDB", description = "Updates an existing category by its ID and returns the updated category.")
    @PutMapping("/{id}")
    public ResponseEntity<CategoryDocument> update(@PathVariable String id, @RequestBody CategoryDocument category) {
        return new ResponseEntity<>(categoryMongoService.update(id, category), HttpStatus.OK);
    }

    @Operation(summary = "Delete a category from MongoDB", description = "Deletes a category by its unique ID from MongoDB.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        categoryMongoService.deleteById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}

