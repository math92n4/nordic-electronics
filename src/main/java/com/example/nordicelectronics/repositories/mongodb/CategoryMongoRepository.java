package com.example.nordicelectronics.repositories.mongodb;

import com.example.nordicelectronics.document.CategoryDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoryMongoRepository extends MongoRepository<CategoryDocument, String> {
    Optional<CategoryDocument> findByName(String name);
}

