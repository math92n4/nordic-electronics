package com.example.nordicelectronics.repositories.mongodb;

import com.example.nordicelectronics.entity.mongodb.UserDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserMongoRepository extends MongoRepository<UserDocument, String> {
    Optional<UserDocument> findByUserId(UUID userId);
    Optional<UserDocument> findByEmail(String email);
    void deleteByUserId(UUID userId);
}

