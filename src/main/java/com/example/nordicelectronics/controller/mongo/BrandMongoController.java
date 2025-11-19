package com.example.nordicelectronics.controller.mongo;

import com.example.nordicelectronics.document.BrandDocument;
import com.example.nordicelectronics.service.mongo.BrandMongoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "MongoDB Brand Controller", description = "Handles operations related to brands in MongoDB")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mongodb/brands")
public class BrandMongoController {

    private final BrandMongoService brandMongoService;

    @Operation(summary = "Get all brands from MongoDB", description = "Fetches a list of all brands from MongoDB.")
    @GetMapping("")
    public ResponseEntity<List<BrandDocument>> getAll() {
        return new ResponseEntity<>(brandMongoService.getAll(), HttpStatus.OK);
    }

    @Operation(summary = "Get brand by ID from MongoDB", description = "Fetches a brand by its unique ID from MongoDB.")
    @GetMapping("/{id}")
    public ResponseEntity<BrandDocument> getById(@PathVariable String id) {
        return new ResponseEntity<>(brandMongoService.getById(id), HttpStatus.OK);
    }

    @Operation(summary = "Create a new brand in MongoDB", description = "Creates a new brand and returns the created brand.")
    @PostMapping("")
    public ResponseEntity<BrandDocument> save(@RequestBody BrandDocument brand) {
        return new ResponseEntity<>(brandMongoService.save(brand), HttpStatus.CREATED);
    }

    @Operation(summary = "Update an existing brand in MongoDB", description = "Updates an existing brand by its ID and returns the updated brand.")
    @PutMapping("/{id}")
    public ResponseEntity<BrandDocument> update(@PathVariable String id, @RequestBody BrandDocument brand) {
        return new ResponseEntity<>(brandMongoService.update(id, brand), HttpStatus.OK);
    }

    @Operation(summary = "Delete a brand from MongoDB", description = "Deletes a brand by its unique ID from MongoDB.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        brandMongoService.deleteById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}

