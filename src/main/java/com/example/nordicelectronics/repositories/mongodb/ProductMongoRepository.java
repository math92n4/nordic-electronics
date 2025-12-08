package com.example.nordicelectronics.repositories.mongodb;

import com.example.nordicelectronics.entity.mongodb.ProductDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductMongoRepository extends MongoRepository<ProductDocument, String> {
    Optional<ProductDocument> findByProductId(UUID productId);
    Optional<ProductDocument> findBySku(String sku);
    
    @Query("{ 'brand.brandId': ?0 }")
    List<ProductDocument> findByBrandId(UUID brandId);
    
    @Query("{ 'brand.name': ?0 }")
    List<ProductDocument> findByBrandName(String brandName);
    
    // Query by embedded categories
    @Query("{ 'categories.categoryId': ?0 }")
    List<ProductDocument> findByCategoryId(UUID categoryId);
    
    @Query("{ 'categories.name': ?0 }")
    List<ProductDocument> findByCategoryName(String categoryName);
    
    void deleteByProductId(UUID productId);
}
