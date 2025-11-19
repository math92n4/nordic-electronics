package com.example.nordicelectronics.repositories.mongodb;

import com.example.nordicelectronics.document.WarrantyDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WarrantyMongoRepository extends MongoRepository<WarrantyDocument, String> {
}

