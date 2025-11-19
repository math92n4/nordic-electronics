package com.example.nordicelectronics.repositories.mongodb;

import com.example.nordicelectronics.document.BrandDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BrandMongoRepository extends MongoRepository<BrandDocument, String> {
    Optional<BrandDocument> findByName(String name);
}

