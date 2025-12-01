package com.example.nordicelectronics.repositories.mongodb;

import com.example.nordicelectronics.entity.mongodb.WarehouseDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;
import java.util.UUID;

public interface WarehouseMongoRepository extends MongoRepository<WarehouseDocument, String> {
    Optional<WarehouseDocument> findByWarehouseId(UUID warehouseId);
    Optional<WarehouseDocument> findByName(String name);
    void deleteByWarehouseId(UUID warehouseId);
}

