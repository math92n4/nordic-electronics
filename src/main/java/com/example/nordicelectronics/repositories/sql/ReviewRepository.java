package com.example.nordicelectronics.repositories.sql;

import com.example.nordicelectronics.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReviewRepository extends JpaRepository<Review, UUID> {
    List<Review> findByUser_UserId(UUID userId);
    Optional<Review> findByReviewIdAndUser_UserId(UUID reviewId, UUID userId);

    List<Review> findByProduct_ProductId(UUID productId);
}
