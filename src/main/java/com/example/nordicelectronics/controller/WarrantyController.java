package com.example.nordicelectronics.controller;

import com.example.nordicelectronics.entity.Product;
import com.example.nordicelectronics.entity.Warranty;
import com.example.nordicelectronics.service.ProductService;
import com.example.nordicelectronics.service.WarrantyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/warranties")
public class WarrantyController {

    private final WarrantyService warrantyService;

    @GetMapping("")
    public ResponseEntity<List<Warranty>> getAll() {
        return new ResponseEntity<>(warrantyService.getAll(), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Warranty> getById(@PathVariable UUID id) {
        return new ResponseEntity<>(warrantyService.getById(id), HttpStatus.OK);
    }

    @PostMapping("")
    public ResponseEntity<Warranty> save(@RequestBody Warranty warranty) {
        return new ResponseEntity<>(warrantyService.save(warranty), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Warranty> update(@PathVariable UUID id, @RequestBody Warranty warranty) {
        return new ResponseEntity<>(warrantyService.update(id, warranty), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Product> delete(@PathVariable UUID id) {
        warrantyService.deleteById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}
