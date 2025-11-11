package com.example.nordicelectronics.controller;

import com.example.nordicelectronics.entity.Brand;
import com.example.nordicelectronics.entity.Product;
import com.example.nordicelectronics.entity.Warehouse;
import com.example.nordicelectronics.service.BrandService;
import com.example.nordicelectronics.service.WarehouseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/warehouses")
public class WarehouseController {


    private final WarehouseService warehouseService;

    @GetMapping("")
    public ResponseEntity<List<Warehouse>> getAll() {
        return new ResponseEntity<>(warehouseService.getAll(), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Warehouse> getById(@PathVariable UUID id) {
        return new ResponseEntity<>(warehouseService.getById(id), HttpStatus.OK);
    }

    @PostMapping("")
    public ResponseEntity<Warehouse> save(@RequestBody Warehouse warehouse) {
        return new ResponseEntity<>(warehouseService.save(warehouse), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Warehouse> update(@PathVariable UUID id, @RequestBody Warehouse warehouse) {
        return new ResponseEntity<>(warehouseService.update(id, warehouse), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Product> delete(@PathVariable UUID id) {
        warehouseService.deleteById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
