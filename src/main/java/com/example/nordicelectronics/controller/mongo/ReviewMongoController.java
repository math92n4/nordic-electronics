package com.example.nordicelectronics.controller.mongo;

import com.example.nordicelectronics.document.ReviewDocument;
import com.example.nordicelectronics.service.mongo.ReviewMongoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "MongoDB Review Controller", description = "Handles operations related to reviews in MongoDB")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mongodb/reviews")
public class ReviewMongoController {

    private final ReviewMongoService reviewMongoService;

    @Operation(summary = "Get all reviews from MongoDB", description = "Fetches a list of all reviews from MongoDB.")
    @GetMapping("")
    public ResponseEntity<List<ReviewDocument>> getAll() {
        return new ResponseEntity<>(reviewMongoService.getAll(), HttpStatus.OK);
    }

    @Operation(summary = "Get review by ID from MongoDB", description = "Fetches a review by its unique ID from MongoDB.")
    @GetMapping("/{id}")
    public ResponseEntity<ReviewDocument> getById(@PathVariable String id) {
        return new ResponseEntity<>(reviewMongoService.getById(id), HttpStatus.OK);
    }

    @Operation(summary = "Create a new review in MongoDB", description = "Creates a new review and returns the created review.")
    @PostMapping("")
    public ResponseEntity<ReviewDocument> save(@RequestBody ReviewDocument review) {
        return new ResponseEntity<>(reviewMongoService.save(review), HttpStatus.CREATED);
    }

    @Operation(summary = "Update an existing review in MongoDB", description = "Updates an existing review by its ID and returns the updated review.")
    @PutMapping("/{id}")
    public ResponseEntity<ReviewDocument> update(@PathVariable String id, @RequestBody ReviewDocument review) {
        return new ResponseEntity<>(reviewMongoService.update(id, review), HttpStatus.OK);
    }

    @Operation(summary = "Delete a review from MongoDB", description = "Deletes a review by its unique ID from MongoDB.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        reviewMongoService.deleteById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}

