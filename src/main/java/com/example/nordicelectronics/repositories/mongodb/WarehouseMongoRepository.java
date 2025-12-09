package com.example.nordicelectronics.repositories.mongodb;

import com.example.nordicelectronics.entity.mongodb.WarehouseDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WarehouseMongoRepository extends MongoRepository<WarehouseDocument, String> {
    Optional<WarehouseDocument> findByWarehouseId(UUID warehouseId);
    Optional<WarehouseDocument> findByName(String name);
    
    @Query("{ 'address.city': ?0 }")
    List<WarehouseDocument> findByAddressCity(String city);
    
    @Query("{ 'products.productId': ?0 }")
    List<WarehouseDocument> findByProductId(UUID productId);
    
    @Query("{ 'products': { $elemMatch: { 'productId': ?0, 'stockQuantity': { $gt: 0 } } } }")
    List<WarehouseDocument> findWarehousesWithStockForProduct(UUID productId);
    
    void deleteByWarehouseId(UUID warehouseId);
}
