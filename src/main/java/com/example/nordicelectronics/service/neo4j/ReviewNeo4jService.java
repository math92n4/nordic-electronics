package com.example.nordicelectronics.service.neo4j;

import com.example.nordicelectronics.entity.neo4j.ReviewNode;
import com.example.nordicelectronics.repositories.neo4j.ReviewNeo4jRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReviewNeo4jService {

    private final ReviewNeo4jRepository reviewNeo4jRepository;

    public List<ReviewNode> getAll() {
        return reviewNeo4jRepository.findAll();
    }

    public ReviewNode getByReviewId(UUID reviewId) {
        return reviewNeo4jRepository.findByReviewId(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found with ID: " + reviewId));
    }

    public List<ReviewNode> getByProductId(UUID productId) {
        return reviewNeo4jRepository.findByProductId(productId);
    }

    public List<ReviewNode> getByUserId(UUID userId) {
        return reviewNeo4jRepository.findByUserId(userId);
    }

    public List<ReviewNode> getVerifiedPurchaseReviews() {
        return reviewNeo4jRepository.findByIsVerifiedPurchase(true);
    }

    public ReviewNode save(ReviewNode reviewNode) {
        if (reviewNode.getReviewId() == null) {
            reviewNode.setReviewId(UUID.randomUUID());
        }
        return reviewNeo4jRepository.save(reviewNode);
    }

    public ReviewNode update(UUID reviewId, ReviewNode reviewNode) {
        ReviewNode existing = getByReviewId(reviewId);

        existing.setUserId(reviewNode.getUserId());
        existing.setOrderId(reviewNode.getOrderId());
        existing.setReviewValue(reviewNode.getReviewValue());
        existing.setTitle(reviewNode.getTitle());
        existing.setComment(reviewNode.getComment());
        existing.setVerifiedPurchase(reviewNode.isVerifiedPurchase());
        existing.setProductId(reviewNode.getProductId());

        return reviewNeo4jRepository.save(existing);
    }

    public void deleteByReviewId(UUID reviewId) {
        reviewNeo4jRepository.deleteByReviewId(reviewId);
    }
}

