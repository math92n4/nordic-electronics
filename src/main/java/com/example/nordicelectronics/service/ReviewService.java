package com.example.nordicelectronics.service;

import com.example.nordicelectronics.entity.Review;
import com.example.nordicelectronics.entity.User;
import com.example.nordicelectronics.repositories.sql.ReviewRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepository;
    @Lazy
    private final UserService userService;
    @Lazy
    private final ProductService productService;

    public Review getById(UUID id) {
        return reviewRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Review not found"));
    }

    public List<Review> getByUserId(UUID userId) {
        return reviewRepository.findByUser_UserId(userId);
    }

    public List<Review> getByUserEmail(String email) {
        User user = userService.findByEmail(email);
        return getByUserId(user.getUserId());
    }

    public List<Review> getByProductId(UUID productId) {
        return reviewRepository.findByProductId(productId);
    }

    public Review save(Review review) {
        return reviewRepository.save(review);
    }

    public Review saveForUser(String email, Review review) {
        User user = userService.findByEmail(email);
        
        productService.getById(review.getProductId());
        
        review.setUser(user);
        return reviewRepository.save(review);
    }

    public Review update(UUID id, Review review) {
        Review existing = getById(id);

        productService.getById(review.getProductId());

        existing.setProductId(review.getProductId());
        existing.setOrderId(review.getOrderId());
        existing.setReviewValue(review.getReviewValue());
        existing.setTitle(review.getTitle());
        existing.setComment(review.getComment());
        existing.setVerifiedPurchase(review.isVerifiedPurchase());

        return reviewRepository.save(existing);
    }

    public Review updateForUser(String email, UUID reviewId, Review review) {
        User user = userService.findByEmail(email);
        Review existing = reviewRepository.findByReviewIdAndUser_UserId(reviewId, user.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("Review not found or you don't have permission to update it"));
        return update(existing.getReviewId(), review);
    }

    public void deleteById(UUID id) {
        reviewRepository.deleteById(id);
    }

    public void deleteForUser(String email, UUID reviewId) {
        User user = userService.findByEmail(email);
        Review existing = reviewRepository.findByReviewIdAndUser_UserId(reviewId, user.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("Review not found or you don't have permission to delete it"));
        deleteById(existing.getReviewId());
    }
}

