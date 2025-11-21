package com.example.nordicelectronics.controller.postgresql;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.example.nordicelectronics.entity.Category;
import com.example.nordicelectronics.entity.Product;
import com.example.nordicelectronics.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "PostgreSQL Category Controller", description = "Handles operations related to categories in PostgreSQL")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/postgresql/categories")
public class CategoryController {

    private final CategoryService categoryService;

    @Operation(summary = "Get all PostgreSQL categories", description = "Fetches all categories.")
    @GetMapping("")
    public ResponseEntity<List<Category>> getAll() {
        return new ResponseEntity<>(categoryService.getAll(), HttpStatus.OK);
    }

    @Operation(summary = "Get PostgreSQL category by ID", description = "Fetches a category based on its unique ID.")
    @GetMapping("/{id}")
    public ResponseEntity<Category> getById(@PathVariable UUID id) {
        return new ResponseEntity<>(categoryService.getById(id), HttpStatus.OK);
    }

    @Operation(summary = "Create a new PostgreSQL category", description = "Creates a new category and returns the created category.")
    @PostMapping("")
    public ResponseEntity<Category> save(@RequestBody Category category) {
        return new ResponseEntity<>(categoryService.save(category), HttpStatus.CREATED);
    }

    @Operation(summary = "Update an existing PostgreSQL category", description = "Updates an existing category and returns the updated category.")
    @PutMapping("/{id}")
    public ResponseEntity<Category> update(@PathVariable UUID id, @RequestBody Category category) {
        return new ResponseEntity<>(categoryService.update(id, category), HttpStatus.OK);
    }

    @Operation(summary = "Delete a PostgreSQL category", description = "Deletes a category based on its unique ID.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Product> delete(@PathVariable UUID id) {
        categoryService.deleteById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
