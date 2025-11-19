package com.example.nordicelectronics.repositories.mongodb;

import com.example.nordicelectronics.document.AddressDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AddressMongoRepository extends MongoRepository<AddressDocument, String> {
    Optional<AddressDocument> findByUserId(String userId);
    void deleteByUserId(String userId);
}

