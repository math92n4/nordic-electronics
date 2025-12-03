package com.example.nordicelectronics.controller.mongodb;

import com.example.nordicelectronics.entity.mongodb.WarrantyDocument;
import com.example.nordicelectronics.service.mongodb.WarrantyMongoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "MongoDB Warranty Controller", description = "Handles operations related to warranties in MongoDB")
@RestController
@ConditionalOnProperty(name = "spring.data.mongodb.enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
@RequestMapping("/api/mongodb/warranties")
public class WarrantyMongoController {

    private final WarrantyMongoService warrantyMongoService;

    @Operation(summary = "Get all MongoDB warranties", description = "Fetches a list of all warranties.")
    @GetMapping("")
    public ResponseEntity<List<WarrantyDocument>> getAll() {
        return new ResponseEntity<>(warrantyMongoService.getAll(), HttpStatus.OK);
    }

    @Operation(summary = "Get MongoDB warranty by ID", description = "Fetches a warranty by its unique ID.")
    @GetMapping("/{warrantyId}")
    public ResponseEntity<WarrantyDocument> getByWarrantyId(@PathVariable UUID warrantyId) {
        return new ResponseEntity<>(warrantyMongoService.getByWarrantyId(warrantyId), HttpStatus.OK);
    }

    @Operation(summary = "Get MongoDB warranty by product ID", description = "Fetches a warranty by product ID.")
    @GetMapping("/product/{productId}")
    public ResponseEntity<WarrantyDocument> getByProductId(@PathVariable UUID productId) {
        return new ResponseEntity<>(warrantyMongoService.getByProductId(productId), HttpStatus.OK);
    }

    @Operation(summary = "Create a new MongoDB warranty", description = "Creates a new warranty and returns the created warranty.")
    @PostMapping("")
    public ResponseEntity<WarrantyDocument> save(@RequestBody WarrantyDocument warrantyDocument) {
        return new ResponseEntity<>(warrantyMongoService.save(warrantyDocument), HttpStatus.CREATED);
    }

    @Operation(summary = "Update an existing MongoDB warranty", description = "Updates an existing warranty by its ID and returns the updated warranty.")
    @PutMapping("/{warrantyId}")
    public ResponseEntity<WarrantyDocument> update(@PathVariable UUID warrantyId, @RequestBody WarrantyDocument warrantyDocument) {
        return new ResponseEntity<>(warrantyMongoService.update(warrantyId, warrantyDocument), HttpStatus.OK);
    }

    @Operation(summary = "Delete a MongoDB warranty", description = "Deletes a warranty by its unique ID.")
    @DeleteMapping("/{warrantyId}")
    public ResponseEntity<Void> delete(@PathVariable UUID warrantyId) {
        warrantyMongoService.deleteByWarrantyId(warrantyId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}

