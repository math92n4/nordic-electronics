package com.example.nordicelectronics.service.mongo;

import com.example.nordicelectronics.document.ReviewDocument;
import com.example.nordicelectronics.repositories.mongodb.ReviewMongoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewMongoService {

    private final ReviewMongoRepository reviewMongoRepository;

    public List<ReviewDocument> getAll() {
        return reviewMongoRepository.findAll();
    }

    public ReviewDocument getById(String id) {
        return reviewMongoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Review not found with id: " + id));
    }

    public ReviewDocument save(ReviewDocument review) {
        return reviewMongoRepository.save(review);
    }

    public ReviewDocument update(String id, ReviewDocument review) {
        ReviewDocument existing = getById(id);
        existing.setReviewValue(review.getReviewValue());
        existing.setTitle(review.getTitle());
        existing.setComment(review.getComment());
        existing.setVerifiedPurchase(review.isVerifiedPurchase());
        return reviewMongoRepository.save(existing);
    }

    public void deleteById(String id) {
        reviewMongoRepository.deleteById(id);
    }

    public List<ReviewDocument> getByProductId(String productId) {
        return reviewMongoRepository.findByProductId(productId);
    }

    public List<ReviewDocument> getByUserId(String userId) {
        return reviewMongoRepository.findByUserId(userId);
    }
}

