package com.example.nordicelectronics.controller.mongo;

import com.example.nordicelectronics.document.ProductDocument;
import com.example.nordicelectronics.service.mongo.ProductMongoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "MongoDB Product Controller", description = "Handles operations related to products in MongoDB")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mongodb/products")
public class ProductMongoController {

    private final ProductMongoService productMongoService;

    @Operation(summary = "Get all products from MongoDB", description = "Fetches a list of all products from MongoDB.")
    @GetMapping("")
    public ResponseEntity<List<ProductDocument>> getAll() {
        return new ResponseEntity<>(productMongoService.getAll(), HttpStatus.OK);
    }

    @Operation(summary = "Get product by ID from MongoDB", description = "Fetches a product by its unique ID from MongoDB.")
    @GetMapping("/{id}")
    public ResponseEntity<ProductDocument> getById(@PathVariable String id) {
        return new ResponseEntity<>(productMongoService.getById(id), HttpStatus.OK);
    }

    @Operation(summary = "Create a new product in MongoDB", description = "Creates a new product and returns the created product.")
    @PostMapping("")
    public ResponseEntity<ProductDocument> save(@RequestBody ProductDocument product) {
        return new ResponseEntity<>(productMongoService.save(product), HttpStatus.CREATED);
    }

    @Operation(summary = "Update an existing product in MongoDB", description = "Updates an existing product by its ID and returns the updated product.")
    @PutMapping("/{id}")
    public ResponseEntity<ProductDocument> update(@PathVariable String id, @RequestBody ProductDocument product) {
        return new ResponseEntity<>(productMongoService.update(id, product), HttpStatus.OK);
    }

    @Operation(summary = "Delete a product from MongoDB", description = "Deletes a product by its unique ID from MongoDB.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        productMongoService.deleteById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}

