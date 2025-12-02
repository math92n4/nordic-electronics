package com.example.nordicelectronics.service.mongodb;

import com.example.nordicelectronics.entity.mongodb.WarehouseDocument;
import com.example.nordicelectronics.repositories.mongodb.WarehouseMongoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WarehouseMongoService {

    private final WarehouseMongoRepository warehouseMongoRepository;

    public List<WarehouseDocument> getAll() {
        return warehouseMongoRepository.findAll();
    }

    public WarehouseDocument getByWarehouseId(UUID warehouseId) {
        return warehouseMongoRepository.findByWarehouseId(warehouseId)
                .orElseThrow(() -> new RuntimeException("Warehouse not found with ID: " + warehouseId));
    }

    public WarehouseDocument save(WarehouseDocument warehouseDocument) {
        if (warehouseDocument.getWarehouseId() == null) {
            warehouseDocument.setWarehouseId(UUID.randomUUID());
        }
        return warehouseMongoRepository.save(warehouseDocument);
    }

    public WarehouseDocument update(UUID warehouseId, WarehouseDocument warehouseDocument) {
        WarehouseDocument existing = getByWarehouseId(warehouseId);
        
        existing.setName(warehouseDocument.getName());
        existing.setPhoneNumber(warehouseDocument.getPhoneNumber());
        existing.setWarehouseProducts(warehouseDocument.getWarehouseProducts());
        existing.setAddressId(warehouseDocument.getAddressId());

        return warehouseMongoRepository.save(existing);
    }

    public void deleteByWarehouseId(UUID warehouseId) {
        warehouseMongoRepository.deleteByWarehouseId(warehouseId);
    }
}

