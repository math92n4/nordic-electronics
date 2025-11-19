package com.example.nordicelectronics.service.mongo;

import com.example.nordicelectronics.document.WarehouseDocument;
import com.example.nordicelectronics.repositories.mongodb.WarehouseMongoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WarehouseMongoService {

    private final WarehouseMongoRepository warehouseMongoRepository;

    public List<WarehouseDocument> getAll() {
        return warehouseMongoRepository.findAll();
    }

    public WarehouseDocument getById(String id) {
        return warehouseMongoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Warehouse not found with id: " + id));
    }

    public WarehouseDocument save(WarehouseDocument warehouse) {
        return warehouseMongoRepository.save(warehouse);
    }

    public WarehouseDocument update(String id, WarehouseDocument warehouse) {
        WarehouseDocument existing = getById(id);
        existing.setName(warehouse.getName());
        existing.setAddress(warehouse.getAddress());
        existing.setCity(warehouse.getCity());
        existing.setPostalCode(warehouse.getPostalCode());
        existing.setCountry(warehouse.getCountry());
        existing.setPhone(warehouse.getPhone());
        if (warehouse.getProducts() != null) {
            existing.setProducts(warehouse.getProducts());
        }
        return warehouseMongoRepository.save(existing);
    }

    public void deleteById(String id) {
        warehouseMongoRepository.deleteById(id);
    }
}

