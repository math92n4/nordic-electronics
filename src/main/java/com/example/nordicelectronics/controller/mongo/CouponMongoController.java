package com.example.nordicelectronics.controller.mongo;

import com.example.nordicelectronics.document.CouponDocument;
import com.example.nordicelectronics.service.mongo.CouponMongoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "MongoDB Coupon Controller", description = "Handles operations related to coupons in MongoDB")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mongodb/coupons")
public class CouponMongoController {

    private final CouponMongoService couponMongoService;

    @Operation(summary = "Get all coupons from MongoDB", description = "Fetches a list of all coupons from MongoDB.")
    @GetMapping("")
    public ResponseEntity<List<CouponDocument>> getAll() {
        return new ResponseEntity<>(couponMongoService.getAll(), HttpStatus.OK);
    }

    @Operation(summary = "Get coupon by ID from MongoDB", description = "Fetches a coupon by its unique ID from MongoDB.")
    @GetMapping("/{id}")
    public ResponseEntity<CouponDocument> getById(@PathVariable String id) {
        return new ResponseEntity<>(couponMongoService.getById(id), HttpStatus.OK);
    }

    @Operation(summary = "Create a new coupon in MongoDB", description = "Creates a new coupon and returns the created coupon.")
    @PostMapping("")
    public ResponseEntity<CouponDocument> save(@RequestBody CouponDocument coupon) {
        return new ResponseEntity<>(couponMongoService.save(coupon), HttpStatus.CREATED);
    }

    @Operation(summary = "Update an existing coupon in MongoDB", description = "Updates an existing coupon by its ID and returns the updated coupon.")
    @PutMapping("/{id}")
    public ResponseEntity<CouponDocument> update(@PathVariable String id, @RequestBody CouponDocument coupon) {
        return new ResponseEntity<>(couponMongoService.update(id, coupon), HttpStatus.OK);
    }

    @Operation(summary = "Delete a coupon from MongoDB", description = "Deletes a coupon by its unique ID from MongoDB.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        couponMongoService.deleteById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}

