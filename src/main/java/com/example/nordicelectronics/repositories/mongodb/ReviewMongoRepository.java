package com.example.nordicelectronics.repositories.mongodb;

import com.example.nordicelectronics.entity.mongodb.ReviewDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReviewMongoRepository extends MongoRepository<ReviewDocument, String> {
    Optional<ReviewDocument> findByReviewId(UUID reviewId);
    List<ReviewDocument> findByProductId(UUID productId);
    List<ReviewDocument> findByUserId(UUID userId);
    List<ReviewDocument> findByIsVerifiedPurchase(boolean isVerifiedPurchase);
    void deleteByReviewId(UUID reviewId);
}

