package com.example.nordicelectronics.service;

import com.example.nordicelectronics.entity.Brand;
import com.example.nordicelectronics.entity.Warehouse;
import com.example.nordicelectronics.repositories.sql.BrandRepository;
import com.example.nordicelectronics.repositories.sql.WarehouseRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WarehouseService {

    private final WarehouseRepository warehouseRepository;

    public List<Warehouse> getAll() {
        return warehouseRepository.findAll();
    }

    public Warehouse getById(UUID id) {
        return warehouseRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Warehouse not found"));
    }

    public Warehouse save(Warehouse brand) {
        return warehouseRepository.save(brand);
    }

    public Warehouse update(UUID id, Warehouse warehouse) {
        Warehouse existing = getById(id);

        existing.setName(warehouse.getName());
        existing.setAddress(warehouse.getAddress());
        existing.setPhone(warehouse.getPhone());

        return warehouseRepository.save(existing);
    }

    public void deleteById(UUID id) {
        warehouseRepository.deleteById(id);
    }

}
