package com.example.nordicelectronics.controller.mongodb;

import com.example.nordicelectronics.entity.mongodb.ReviewDocument;
import com.example.nordicelectronics.service.mongodb.ReviewMongoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "MongoDB Review Controller", description = "Handles operations related to reviews in MongoDB")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mongodb/reviews")
public class ReviewMongoController {

    private final ReviewMongoService reviewMongoService;

    @Operation(summary = "Get all MongoDB reviews", description = "Fetches a list of all reviews.")
    @GetMapping("")
    public ResponseEntity<List<ReviewDocument>> getAll() {
        return new ResponseEntity<>(reviewMongoService.getAll(), HttpStatus.OK);
    }

    @Operation(summary = "Get MongoDB review by ID", description = "Fetches a review by its unique ID.")
    @GetMapping("/{reviewId}")
    public ResponseEntity<ReviewDocument> getByReviewId(@PathVariable UUID reviewId) {
        return new ResponseEntity<>(reviewMongoService.getByReviewId(reviewId), HttpStatus.OK);
    }

    @Operation(summary = "Get MongoDB reviews by product ID", description = "Fetches all reviews for a specific product.")
    @GetMapping("/product/{productId}")
    public ResponseEntity<List<ReviewDocument>> getByProductId(@PathVariable UUID productId) {
        return new ResponseEntity<>(reviewMongoService.getByProductId(productId), HttpStatus.OK);
    }

    @Operation(summary = "Get MongoDB reviews by user ID", description = "Fetches all reviews by a specific user.")
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ReviewDocument>> getByUserId(@PathVariable UUID userId) {
        return new ResponseEntity<>(reviewMongoService.getByUserId(userId), HttpStatus.OK);
    }

    @Operation(summary = "Get verified purchase reviews", description = "Fetches all verified purchase reviews.")
    @GetMapping("/verified")
    public ResponseEntity<List<ReviewDocument>> getVerifiedPurchaseReviews() {
        return new ResponseEntity<>(reviewMongoService.getVerifiedPurchaseReviews(), HttpStatus.OK);
    }

    @Operation(summary = "Create a new MongoDB review", description = "Creates a new review and returns the created review.")
    @PostMapping("")
    public ResponseEntity<ReviewDocument> save(@RequestBody ReviewDocument reviewDocument) {
        return new ResponseEntity<>(reviewMongoService.save(reviewDocument), HttpStatus.CREATED);
    }

    @Operation(summary = "Update an existing MongoDB review", description = "Updates an existing review by its ID and returns the updated review.")
    @PutMapping("/{reviewId}")
    public ResponseEntity<ReviewDocument> update(@PathVariable UUID reviewId, @RequestBody ReviewDocument reviewDocument) {
        return new ResponseEntity<>(reviewMongoService.update(reviewId, reviewDocument), HttpStatus.OK);
    }

    @Operation(summary = "Delete a MongoDB review", description = "Deletes a review by its unique ID.")
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> delete(@PathVariable UUID reviewId) {
        reviewMongoService.deleteByReviewId(reviewId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}

