package com.example.nordicelectronics.controller.postgresql;

import com.example.nordicelectronics.entity.Review;
import com.example.nordicelectronics.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "PostgreSQL Review Controller", description = "Handles operations related to reviews in PostgreSQL")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/postgresql/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    @Operation(summary = "Get PostgreSQL reviews by user email", description = "Fetches all reviews associated with a specific user email.")
    @GetMapping("")
    public ResponseEntity<List<Review>> getMyReviews(@AuthenticationPrincipal UserDetails userDetails) {
        List<Review> reviews = reviewService.getByUserEmail(userDetails.getUsername());
        return ResponseEntity.ok(reviews);
    }

    @Operation(summary = "Get PostgreSQL reviews by product ID", description = "Fetches all reviews associated with a specific product ID.")
    @GetMapping("/product/{productId}")
    public ResponseEntity<List<Review>> getByProduct(@PathVariable UUID productId) {
        List<Review> reviews = reviewService.getByProductId(productId);
        return ResponseEntity.ok(reviews);
    }

    @Operation(summary = "Get PostgreSQL review by ID", description = "Fetches a review by its unique ID.")
    @GetMapping("/{id}")
    public ResponseEntity<Review> getById(@PathVariable UUID id) {
        Review review = reviewService.getById(id);
        return ResponseEntity.ok(review);
    }

    @Operation(summary = "Create a new PostgreSQL review", description = "Creates a new review and returns the created review.")
    @PostMapping("")
    public ResponseEntity<Review> save(@AuthenticationPrincipal UserDetails userDetails,
                                       @RequestBody Review review) {
        Review savedReview = reviewService.saveForUser(userDetails.getUsername(), review);
        return new ResponseEntity<>(savedReview, HttpStatus.CREATED);
    }

    @Operation(summary = "Update an existing PostgreSQL review", description = "Updates an existing review by its ID and returns the updated review.")
    @PutMapping("/{id}")
    public ResponseEntity<Review> update(@AuthenticationPrincipal UserDetails userDetails,
                                          @PathVariable UUID id,
                                          @RequestBody Review review) {
        Review updatedReview = reviewService.updateForUser(userDetails.getUsername(), id, review);
        return ResponseEntity.ok(updatedReview);
    }

    @Operation(summary = "Delete a PostgreSQL review", description = "Deletes a review by its unique ID.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal UserDetails userDetails,
                                       @PathVariable UUID id) {
        reviewService.deleteForUser(userDetails.getUsername(), id);
        return ResponseEntity.noContent().build();
    }
}

