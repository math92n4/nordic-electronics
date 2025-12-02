package com.example.nordicelectronics.repositories.mongodb;

import com.example.nordicelectronics.entity.mongodb.BrandDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;
import java.util.UUID;

public interface BrandMongoRepository extends MongoRepository<BrandDocument, String> {
    Optional<BrandDocument> findByBrandId(UUID brandId);
    Optional<BrandDocument> findByName(String name);
    void deleteByBrandId(UUID brandId);
}

