package com.example.nordicelectronics.controller.mongodb;

import com.example.nordicelectronics.entity.mongodb.CouponDocument;
import com.example.nordicelectronics.service.mongodb.CouponMongoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "MongoDB Coupon Controller", description = "Handles operations related to coupons in MongoDB")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mongodb/coupons")
public class CouponMongoController {

    private final CouponMongoService couponMongoService;

    @Operation(summary = "Get all MongoDB coupons", description = "Fetches a list of all coupons.")
    @GetMapping("")
    public ResponseEntity<List<CouponDocument>> getAll() {
        return new ResponseEntity<>(couponMongoService.getAll(), HttpStatus.OK);
    }

    @Operation(summary = "Get MongoDB coupon by ID", description = "Fetches a coupon by its unique ID.")
    @GetMapping("/{couponId}")
    public ResponseEntity<CouponDocument> getByCouponId(@PathVariable UUID couponId) {
        return new ResponseEntity<>(couponMongoService.getByCouponId(couponId), HttpStatus.OK);
    }

    @Operation(summary = "Get MongoDB coupon by code", description = "Fetches a coupon by its code.")
    @GetMapping("/code/{code}")
    public ResponseEntity<CouponDocument> getByCode(@PathVariable String code) {
        return new ResponseEntity<>(couponMongoService.getByCode(code), HttpStatus.OK);
    }

    @Operation(summary = "Get active MongoDB coupons", description = "Fetches all active coupons.")
    @GetMapping("/active")
    public ResponseEntity<List<CouponDocument>> getActiveCoupons() {
        return new ResponseEntity<>(couponMongoService.getActiveCoupons(), HttpStatus.OK);
    }

    @Operation(summary = "Create a new MongoDB coupon", description = "Creates a new coupon and returns the created coupon.")
    @PostMapping("")
    public ResponseEntity<CouponDocument> save(@RequestBody CouponDocument couponDocument) {
        return new ResponseEntity<>(couponMongoService.save(couponDocument), HttpStatus.CREATED);
    }

    @Operation(summary = "Update an existing MongoDB coupon", description = "Updates an existing coupon by its ID and returns the updated coupon.")
    @PutMapping("/{couponId}")
    public ResponseEntity<CouponDocument> update(@PathVariable UUID couponId, @RequestBody CouponDocument couponDocument) {
        return new ResponseEntity<>(couponMongoService.update(couponId, couponDocument), HttpStatus.OK);
    }

    @Operation(summary = "Delete a MongoDB coupon", description = "Deletes a coupon by its unique ID.")
    @DeleteMapping("/{couponId}")
    public ResponseEntity<Void> delete(@PathVariable UUID couponId) {
        couponMongoService.deleteByCouponId(couponId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}

