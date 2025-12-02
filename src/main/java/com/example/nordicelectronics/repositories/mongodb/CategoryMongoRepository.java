package com.example.nordicelectronics.repositories.mongodb;

import com.example.nordicelectronics.entity.mongodb.CategoryDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;
import java.util.UUID;

public interface CategoryMongoRepository extends MongoRepository<CategoryDocument, String> {
    Optional<CategoryDocument> findByCategoryId(UUID categoryId);
    Optional<CategoryDocument> findByName(String name);
    void deleteByCategoryId(UUID categoryId);
}

