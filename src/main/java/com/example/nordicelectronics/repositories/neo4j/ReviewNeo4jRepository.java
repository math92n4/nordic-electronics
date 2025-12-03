package com.example.nordicelectronics.repositories.neo4j;

import com.example.nordicelectronics.entity.neo4j.ReviewNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReviewNeo4jRepository extends Neo4jRepository<ReviewNode, String> {
    Optional<ReviewNode> findByReviewId(UUID reviewId);
    List<ReviewNode> findByProductId(UUID productId);
    List<ReviewNode> findByUserId(UUID userId);
    List<ReviewNode> findByIsVerifiedPurchase(boolean isVerifiedPurchase);
    void deleteByReviewId(UUID reviewId);
}

