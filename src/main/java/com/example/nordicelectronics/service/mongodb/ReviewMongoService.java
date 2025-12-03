package com.example.nordicelectronics.service.mongodb;

import com.example.nordicelectronics.entity.mongodb.ReviewDocument;
import com.example.nordicelectronics.repositories.mongodb.ReviewMongoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@ConditionalOnProperty(name = "spring.data.mongodb.enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
public class ReviewMongoService {

    private final ReviewMongoRepository reviewMongoRepository;

    public List<ReviewDocument> getAll() {
        return reviewMongoRepository.findAll();
    }

    public ReviewDocument getByReviewId(UUID reviewId) {
        return reviewMongoRepository.findByReviewId(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found with ID: " + reviewId));
    }

    public List<ReviewDocument> getByProductId(UUID productId) {
        return reviewMongoRepository.findByProductId(productId);
    }

    public List<ReviewDocument> getByUserId(UUID userId) {
        return reviewMongoRepository.findByUserId(userId);
    }

    public List<ReviewDocument> getVerifiedPurchaseReviews() {
        return reviewMongoRepository.findByIsVerifiedPurchase(true);
    }

    public ReviewDocument save(ReviewDocument reviewDocument) {
        if (reviewDocument.getReviewId() == null) {
            reviewDocument.setReviewId(UUID.randomUUID());
        }
        return reviewMongoRepository.save(reviewDocument);
    }

    public ReviewDocument update(UUID reviewId, ReviewDocument reviewDocument) {
        ReviewDocument existing = getByReviewId(reviewId);
        
        existing.setUserId(reviewDocument.getUserId());
        existing.setOrderId(reviewDocument.getOrderId());
        existing.setReviewValue(reviewDocument.getReviewValue());
        existing.setTitle(reviewDocument.getTitle());
        existing.setComment(reviewDocument.getComment());
        existing.setVerifiedPurchase(reviewDocument.isVerifiedPurchase());
        existing.setProductId(reviewDocument.getProductId());

        return reviewMongoRepository.save(existing);
    }

    public void deleteByReviewId(UUID reviewId) {
        reviewMongoRepository.deleteByReviewId(reviewId);
    }
}

