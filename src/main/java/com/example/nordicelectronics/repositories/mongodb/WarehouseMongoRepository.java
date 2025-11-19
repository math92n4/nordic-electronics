package com.example.nordicelectronics.repositories.mongodb;

import com.example.nordicelectronics.document.WarehouseDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WarehouseMongoRepository extends MongoRepository<WarehouseDocument, String> {
    Optional<WarehouseDocument> findByName(String name);
}

