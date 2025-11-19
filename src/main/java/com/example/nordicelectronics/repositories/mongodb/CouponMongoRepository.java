package com.example.nordicelectronics.repositories.mongodb;

import com.example.nordicelectronics.document.CouponDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CouponMongoRepository extends MongoRepository<CouponDocument, String> {
    Optional<CouponDocument> findByCode(String code);
}

