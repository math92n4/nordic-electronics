package com.example.nordicelectronics.repositories.mongodb;

import com.example.nordicelectronics.document.ProductDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductMongoRepository extends MongoRepository<ProductDocument, String> {
    Optional<ProductDocument> findBySku(String sku);
    List<ProductDocument> findByBrandId(String brandId);
    List<ProductDocument> findByCategoryIdsContaining(String categoryId);
}

