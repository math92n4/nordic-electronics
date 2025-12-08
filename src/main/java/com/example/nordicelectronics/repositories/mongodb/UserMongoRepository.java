package com.example.nordicelectronics.repositories.mongodb;

import com.example.nordicelectronics.entity.mongodb.UserDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserMongoRepository extends MongoRepository<UserDocument, String> {
    Optional<UserDocument> findByUserId(UUID userId);
    Optional<UserDocument> findByEmail(String email);
    
    @Query("{ 'addresses.city': ?0 }")
    List<UserDocument> findByAddressCity(String city);
    
    @Query("{ 'addresses.zip': ?0 }")
    List<UserDocument> findByAddressZip(String zip);
    
    void deleteByUserId(UUID userId);
}
