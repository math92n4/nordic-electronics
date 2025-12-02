package com.example.nordicelectronics.repositories.mongodb;

import com.example.nordicelectronics.entity.mongodb.AddressDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AddressMongoRepository extends MongoRepository<AddressDocument, String> {
    Optional<AddressDocument> findByAddressId(UUID addressId);
    List<AddressDocument> findByUserId(UUID userId);
    void deleteByAddressId(UUID addressId);
}

