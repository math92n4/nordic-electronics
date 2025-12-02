package com.example.nordicelectronics.controller.mongodb;

import com.example.nordicelectronics.entity.mongodb.ProductDocument;
import com.example.nordicelectronics.service.mongodb.ProductMongoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "MongoDB Product Controller", description = "Handles operations related to products in MongoDB")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mongodb/products")
public class ProductMongoController {

    private final ProductMongoService productMongoService;

    @Operation(summary = "Get all MongoDB products", description = "Fetches a list of all products.")
    @GetMapping("")
    public ResponseEntity<List<ProductDocument>> getAll() {
        return new ResponseEntity<>(productMongoService.getAll(), HttpStatus.OK);
    }

    @Operation(summary = "Get MongoDB product by ID", description = "Fetches a product by its unique ID.")
    @GetMapping("/{productId}")
    public ResponseEntity<ProductDocument> getByProductId(@PathVariable UUID productId) {
        return new ResponseEntity<>(productMongoService.getByProductId(productId), HttpStatus.OK);
    }

    @Operation(summary = "Get MongoDB product by SKU", description = "Fetches a product by its SKU.")
    @GetMapping("/sku/{sku}")
    public ResponseEntity<ProductDocument> getBySku(@PathVariable String sku) {
        return new ResponseEntity<>(productMongoService.getBySku(sku), HttpStatus.OK);
    }

    @Operation(summary = "Get MongoDB products by brand ID", description = "Fetches all products for a specific brand.")
    @GetMapping("/brand/{brandId}")
    public ResponseEntity<List<ProductDocument>> getByBrandId(@PathVariable UUID brandId) {
        return new ResponseEntity<>(productMongoService.getByBrandId(brandId), HttpStatus.OK);
    }

    @Operation(summary = "Get MongoDB products by category ID", description = "Fetches all products in a specific category.")
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<ProductDocument>> getByCategoryId(@PathVariable UUID categoryId) {
        return new ResponseEntity<>(productMongoService.getByCategoryId(categoryId), HttpStatus.OK);
    }

    @Operation(summary = "Create a new MongoDB product", description = "Creates a new product and returns the created product.")
    @PostMapping("")
    public ResponseEntity<ProductDocument> save(@RequestBody ProductDocument productDocument) {
        return new ResponseEntity<>(productMongoService.save(productDocument), HttpStatus.CREATED);
    }

    @Operation(summary = "Update an existing MongoDB product", description = "Updates an existing product by its ID and returns the updated product.")
    @PutMapping("/{productId}")
    public ResponseEntity<ProductDocument> update(@PathVariable UUID productId, @RequestBody ProductDocument productDocument) {
        return new ResponseEntity<>(productMongoService.update(productId, productDocument), HttpStatus.OK);
    }

    @Operation(summary = "Delete a MongoDB product", description = "Deletes a product by its unique ID.")
    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> delete(@PathVariable UUID productId) {
        productMongoService.deleteByProductId(productId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}

