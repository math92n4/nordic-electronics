package com.example.nordicelectronics.controller.mongodb;

import com.example.nordicelectronics.entity.mongodb.BrandDocument;
import com.example.nordicelectronics.service.mongodb.BrandMongoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "MongoDB Brand Controller", description = "Handles operations related to brands in MongoDB")
@RestController
@ConditionalOnProperty(name = "spring.data.mongodb.enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
@RequestMapping("/api/mongodb/brands")
public class BrandMongoController {

    private final BrandMongoService brandMongoService;

    @Operation(summary = "Get all MongoDB brands", description = "Fetches a list of all brands.")
    @GetMapping("")
    public ResponseEntity<List<BrandDocument>> getAll() {
        return new ResponseEntity<>(brandMongoService.getAll(), HttpStatus.OK);
    }

    @Operation(summary = "Get MongoDB brand by ID", description = "Fetches a brand by its unique ID.")
    @GetMapping("/{brandId}")
    public ResponseEntity<BrandDocument> getByBrandId(@PathVariable UUID brandId) {
        return new ResponseEntity<>(brandMongoService.getByBrandId(brandId), HttpStatus.OK);
    }

    @Operation(summary = "Create a new MongoDB brand", description = "Creates a new brand and returns the created brand.")
    @PostMapping("")
    public ResponseEntity<BrandDocument> save(@RequestBody BrandDocument brandDocument) {
        return new ResponseEntity<>(brandMongoService.save(brandDocument), HttpStatus.CREATED);
    }

    @Operation(summary = "Update an existing MongoDB brand", description = "Updates an existing brand by its ID and returns the updated brand.")
    @PutMapping("/{brandId}")
    public ResponseEntity<BrandDocument> update(@PathVariable UUID brandId, @RequestBody BrandDocument brandDocument) {
        return new ResponseEntity<>(brandMongoService.update(brandId, brandDocument), HttpStatus.OK);
    }

    @Operation(summary = "Delete a MongoDB brand", description = "Deletes a brand by its unique ID.")
    @DeleteMapping("/{brandId}")
    public ResponseEntity<Void> delete(@PathVariable UUID brandId) {
        brandMongoService.deleteByBrandId(brandId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}

