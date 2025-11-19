package com.example.nordicelectronics.controller;

import com.example.nordicelectronics.entity.Review;
import com.example.nordicelectronics.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping("")
    public ResponseEntity<List<Review>> getMyReviews(@AuthenticationPrincipal UserDetails userDetails) {
        List<Review> reviews = reviewService.getByUserEmail(userDetails.getUsername());
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<Review>> getByProduct(@PathVariable UUID productId) {
        List<Review> reviews = reviewService.getByProductId(productId);
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Review> getById(@PathVariable UUID id) {
        Review review = reviewService.getById(id);
        return ResponseEntity.ok(review);
    }

    @PostMapping("")
    public ResponseEntity<Review> save(@AuthenticationPrincipal UserDetails userDetails,
                                       @RequestBody Review review) {
        Review savedReview = reviewService.saveForUser(userDetails.getUsername(), review, review.getProduct().getProductId());
        return new ResponseEntity<>(savedReview, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Review> update(@AuthenticationPrincipal UserDetails userDetails,
                                          @PathVariable UUID id,
                                          @RequestBody Review review) {
        Review updatedReview = reviewService.updateForUser(userDetails.getUsername(), id, review, review.getProduct().getProductId());
        return ResponseEntity.ok(updatedReview);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal UserDetails userDetails,
                                       @PathVariable UUID id) {
        reviewService.deleteForUser(userDetails.getUsername(), id);
        return ResponseEntity.noContent().build();
    }
}

