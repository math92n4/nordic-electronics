package com.example.nordicelectronics.controller;

import com.example.nordicelectronics.entity.Product;
import com.example.nordicelectronics.entity.Warranty;
import com.example.nordicelectronics.service.ProductService;
import com.example.nordicelectronics.service.WarrantyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Warranty Controller", description = "Handles operations related to warranties")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/warranties")
public class WarrantyController {

    private final WarrantyService warrantyService;

    @Operation(summary = "Get all warranties", description = "Fetches a list of all warranties.")
    @GetMapping("")
    public ResponseEntity<List<Warranty>> getAll() {
        return new ResponseEntity<>(warrantyService.getAll(), HttpStatus.OK);
    }

    @Operation(summary = "Get warranty by ID", description = "Fetches a warranty by its unique ID.")
    @GetMapping("/{id}")
    public ResponseEntity<Warranty> getById(@PathVariable UUID id) {
        return new ResponseEntity<>(warrantyService.getById(id), HttpStatus.OK);
    }

    @Operation(summary = "Create a new warranty", description = "Creates a new warranty and returns the created warranty.")
    @PostMapping("")
    public ResponseEntity<Warranty> save(@RequestBody Warranty warranty) {
        return new ResponseEntity<>(warrantyService.save(warranty), HttpStatus.CREATED);
    }

    @Operation(summary = "Update an existing warranty", description = "Updates an existing warranty by its ID and returns the updated warranty.")
    @PutMapping("/{id}")
    public ResponseEntity<Warranty> update(@PathVariable UUID id, @RequestBody Warranty warranty) {
        return new ResponseEntity<>(warrantyService.update(id, warranty), HttpStatus.OK);
    }

    @Operation(summary = "Delete a warranty", description = "Deletes a warranty by its unique ID.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Product> delete(@PathVariable UUID id) {
        warrantyService.deleteById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}
