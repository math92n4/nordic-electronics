package com.example.nordicelectronics.controller.mongo;

import com.example.nordicelectronics.document.WarrantyDocument;
import com.example.nordicelectronics.service.mongo.WarrantyMongoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "MongoDB Warranty Controller", description = "Handles operations related to warranties in MongoDB")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mongodb/warranties")
public class WarrantyMongoController {

    private final WarrantyMongoService warrantyMongoService;

    @Operation(summary = "Get all warranties from MongoDB", description = "Fetches a list of all warranties from MongoDB.")
    @GetMapping("")
    public ResponseEntity<List<WarrantyDocument>> getAll() {
        return new ResponseEntity<>(warrantyMongoService.getAll(), HttpStatus.OK);
    }

    @Operation(summary = "Get warranty by ID from MongoDB", description = "Fetches a warranty by its unique ID from MongoDB.")
    @GetMapping("/{id}")
    public ResponseEntity<WarrantyDocument> getById(@PathVariable String id) {
        return new ResponseEntity<>(warrantyMongoService.getById(id), HttpStatus.OK);
    }

    @Operation(summary = "Create a new warranty in MongoDB", description = "Creates a new warranty and returns the created warranty.")
    @PostMapping("")
    public ResponseEntity<WarrantyDocument> save(@RequestBody WarrantyDocument warranty) {
        return new ResponseEntity<>(warrantyMongoService.save(warranty), HttpStatus.CREATED);
    }

    @Operation(summary = "Update an existing warranty in MongoDB", description = "Updates an existing warranty by its ID and returns the updated warranty.")
    @PutMapping("/{id}")
    public ResponseEntity<WarrantyDocument> update(@PathVariable String id, @RequestBody WarrantyDocument warranty) {
        return new ResponseEntity<>(warrantyMongoService.update(id, warranty), HttpStatus.OK);
    }

    @Operation(summary = "Delete a warranty from MongoDB", description = "Deletes a warranty by its unique ID from MongoDB.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        warrantyMongoService.deleteById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}

