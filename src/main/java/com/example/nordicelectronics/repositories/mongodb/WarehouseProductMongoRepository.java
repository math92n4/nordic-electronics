package com.example.nordicelectronics.repositories.mongodb;

import com.example.nordicelectronics.document.WarehouseProductDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WarehouseProductMongoRepository extends MongoRepository<WarehouseProductDocument, String> {
    List<WarehouseProductDocument> findByWarehouseId(String warehouseId);
    List<WarehouseProductDocument> findByProductId(String productId);
    Optional<WarehouseProductDocument> findByWarehouseIdAndProductId(String warehouseId, String productId);
    void deleteByWarehouseIdAndProductId(String warehouseId, String productId);
}

