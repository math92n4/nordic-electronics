package com.example.nordicelectronics.controller.neo4j;

import com.example.nordicelectronics.entity.neo4j.ReviewNode;
import com.example.nordicelectronics.service.neo4j.ReviewNeo4jService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Neo4j Review Controller", description = "Handles operations related to reviews in Neo4j")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/neo4j/reviews")
public class ReviewNeo4jController {

    private final ReviewNeo4jService reviewNeo4jService;

    @Operation(summary = "Get all Neo4j reviews")
    @GetMapping("")
    public ResponseEntity<List<ReviewNode>> getAll() {
        return new ResponseEntity<>(reviewNeo4jService.getAll(), HttpStatus.OK);
    }

    @Operation(summary = "Get Neo4j review by ID")
    @GetMapping("/{reviewId}")
    public ResponseEntity<ReviewNode> getByReviewId(@PathVariable UUID reviewId) {
        return new ResponseEntity<>(reviewNeo4jService.getByReviewId(reviewId), HttpStatus.OK);
    }

    @Operation(summary = "Get Neo4j reviews by product ID")
    @GetMapping("/product/{productId}")
    public ResponseEntity<List<ReviewNode>> getByProductId(@PathVariable UUID productId) {
        return new ResponseEntity<>(reviewNeo4jService.getByProductId(productId), HttpStatus.OK);
    }

    @Operation(summary = "Get Neo4j reviews by user ID")
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ReviewNode>> getByUserId(@PathVariable UUID userId) {
        return new ResponseEntity<>(reviewNeo4jService.getByUserId(userId), HttpStatus.OK);
    }

    @Operation(summary = "Get verified purchase reviews")
    @GetMapping("/verified")
    public ResponseEntity<List<ReviewNode>> getVerifiedPurchaseReviews() {
        return new ResponseEntity<>(reviewNeo4jService.getVerifiedPurchaseReviews(), HttpStatus.OK);
    }

    @Operation(summary = "Create a new Neo4j review")
    @PostMapping("")
    public ResponseEntity<ReviewNode> save(@RequestBody ReviewNode reviewNode) {
        return new ResponseEntity<>(reviewNeo4jService.save(reviewNode), HttpStatus.CREATED);
    }

    @Operation(summary = "Update an existing Neo4j review")
    @PutMapping("/{reviewId}")
    public ResponseEntity<ReviewNode> update(@PathVariable UUID reviewId, @RequestBody ReviewNode reviewNode) {
        return new ResponseEntity<>(reviewNeo4jService.update(reviewId, reviewNode), HttpStatus.OK);
    }

    @Operation(summary = "Delete a Neo4j review")
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> delete(@PathVariable UUID reviewId) {
        reviewNeo4jService.deleteByReviewId(reviewId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}

