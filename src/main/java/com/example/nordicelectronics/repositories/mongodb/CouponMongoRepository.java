package com.example.nordicelectronics.repositories.mongodb;

import com.example.nordicelectronics.entity.mongodb.CouponDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CouponMongoRepository extends MongoRepository<CouponDocument, String> {
    Optional<CouponDocument> findByCouponId(UUID couponId);
    Optional<CouponDocument> findByCode(String code);
    List<CouponDocument> findByIsActive(boolean isActive);
    void deleteByCouponId(UUID couponId);
}

