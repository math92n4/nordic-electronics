package com.example.nordicelectronics.controller;

import com.example.nordicelectronics.entity.Product;
import com.example.nordicelectronics.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Product Controller", description = "Handles operations related to products")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    @Operation(summary = "Get all products", description = "Fetches a list of all products.")
    @GetMapping("")
    public ResponseEntity<List<Product>> getAll() {
        return new ResponseEntity<>(productService.getAll(), HttpStatus.OK);
    }

    @Operation(summary = "Get product by ID", description = "Fetches a product by its unique ID.")
    @GetMapping("/{id}")
    public ResponseEntity<Product> getById(@PathVariable UUID id) {
        return new ResponseEntity<>(productService.getById(id), HttpStatus.OK);
    }

    @Operation(summary = "Create a new product", description = "Creates a new product and returns the created product.")
    @PostMapping("")
    public ResponseEntity<Product> save(@RequestBody Product product) {
        return new ResponseEntity<>(productService.save(product), HttpStatus.CREATED);
    }

    @Operation(summary = "Update an existing product", description = "Updates an existing product by its ID and returns the updated product.")
    @PutMapping("/{id}")
    public ResponseEntity<Product> update(@PathVariable UUID id, @RequestBody Product product) {
        return new ResponseEntity<>(productService.update(id, product), HttpStatus.OK);
    }

    @Operation(summary = "Delete a product", description = "Deletes a product by its unique ID.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Product> delete(@PathVariable UUID id) {
        productService.deleteById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
