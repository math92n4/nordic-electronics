package com.example.nordicelectronics.repositories.mongodb;

import com.example.nordicelectronics.entity.mongodb.WarrantyDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;
import java.util.UUID;

public interface WarrantyMongoRepository extends MongoRepository<WarrantyDocument, String> {
    Optional<WarrantyDocument> findByWarrantyId(UUID warrantyId);
    Optional<WarrantyDocument> findByProductId(UUID productId);
    void deleteByWarrantyId(UUID warrantyId);
}

