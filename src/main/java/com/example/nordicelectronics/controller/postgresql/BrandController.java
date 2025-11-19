package com.example.nordicelectronics.controller.postgresql;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.example.nordicelectronics.entity.Brand;
import com.example.nordicelectronics.entity.Product;
import com.example.nordicelectronics.service.BrandService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "PostgreSQL Brand Controller", description = "Handles operations related to brands in PostgreSQL")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/postgresql/brands")
public class BrandController {

    private final BrandService brandService;

    @Operation(summary = "Get all PostgreSQL brands", description = "Fetches a list of all brands.")
    @GetMapping("")
    public ResponseEntity<List<Brand>> getAll() {
        return new ResponseEntity<>(brandService.getAll(), HttpStatus.OK);
    }

    @Operation(summary = "Get PostgreSQL brand by ID", description = "Fetches a brand by its unique ID.")
    @GetMapping("/{id}")
    public ResponseEntity<Brand> getById(@PathVariable UUID id) {
        return new ResponseEntity<>(brandService.getById(id), HttpStatus.OK);
    }

    @Operation(summary = "Create a new PostgreSQL brand", description = "Creates a new brand and returns the created brand.")
    @PostMapping("")
    public ResponseEntity<Brand> save(@RequestBody Brand brand) {
        return new ResponseEntity<>(brandService.save(brand), HttpStatus.CREATED);
    }

    @Operation(summary = "Update an existing PostgreSQL brand", description = "Updates an existing brand by its ID and returns the updated brand.")
    @PutMapping("/{id}")
    public ResponseEntity<Brand> update(@PathVariable UUID id, @RequestBody Brand brand) {
        return new ResponseEntity<>(brandService.update(id, brand), HttpStatus.OK);
    }

    @Operation(summary = "Delete a PostgreSQL brand", description = "Deletes a brand by its unique ID.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Product> delete(@PathVariable UUID id) {
        brandService.deleteById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
