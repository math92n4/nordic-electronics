package com.example.nordicelectronics.controller;

import com.example.nordicelectronics.entity.Brand;
import com.example.nordicelectronics.entity.Product;
import com.example.nordicelectronics.service.BrandService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/brands")
public class BrandController {

    private final BrandService brandService;

    @GetMapping("")
    public ResponseEntity<List<Brand>> getAll() {
        return new ResponseEntity<>(brandService.getAll(), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Brand> getById(@PathVariable UUID id) {
        return new ResponseEntity<>(brandService.getById(id), HttpStatus.OK);
    }

    @PostMapping("")
    public ResponseEntity<Brand> save(@RequestBody Brand brand) {
        return new ResponseEntity<>(brandService.save(brand), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Brand> update(@PathVariable UUID id, @RequestBody Brand brand) {
        return new ResponseEntity<>(brandService.update(id, brand), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Product> delete(@PathVariable UUID id) {
        brandService.deleteById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
