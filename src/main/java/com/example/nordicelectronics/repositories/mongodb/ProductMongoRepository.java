package com.example.nordicelectronics.repositories.mongodb;

import com.example.nordicelectronics.entity.mongodb.ProductDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductMongoRepository extends MongoRepository<ProductDocument, String> {
    Optional<ProductDocument> findByProductId(UUID productId);
    Optional<ProductDocument> findBySku(String sku);
    List<ProductDocument> findByBrandId(UUID brandId);
    List<ProductDocument> findByCategoryIdsContaining(UUID categoryId);
    void deleteByProductId(UUID productId);
}

