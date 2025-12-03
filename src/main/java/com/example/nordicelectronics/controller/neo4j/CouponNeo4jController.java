package com.example.nordicelectronics.controller.neo4j;

import com.example.nordicelectronics.entity.neo4j.CouponNode;
import com.example.nordicelectronics.service.neo4j.CouponNeo4jService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Neo4j Coupon Controller", description = "Handles operations related to coupons in Neo4j")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/neo4j/coupons")
public class CouponNeo4jController {

    private final CouponNeo4jService couponNeo4jService;

    @Operation(summary = "Get all Neo4j coupons")
    @GetMapping("")
    public ResponseEntity<List<CouponNode>> getAll() {
        return new ResponseEntity<>(couponNeo4jService.getAll(), HttpStatus.OK);
    }

    @Operation(summary = "Get Neo4j coupon by ID")
    @GetMapping("/{couponId}")
    public ResponseEntity<CouponNode> getByCouponId(@PathVariable UUID couponId) {
        return new ResponseEntity<>(couponNeo4jService.getByCouponId(couponId), HttpStatus.OK);
    }

    @Operation(summary = "Get Neo4j coupon by code")
    @GetMapping("/code/{code}")
    public ResponseEntity<CouponNode> getByCode(@PathVariable String code) {
        return new ResponseEntity<>(couponNeo4jService.getByCode(code), HttpStatus.OK);
    }

    @Operation(summary = "Get active Neo4j coupons")
    @GetMapping("/active")
    public ResponseEntity<List<CouponNode>> getActiveCoupons() {
        return new ResponseEntity<>(couponNeo4jService.getActiveCoupons(), HttpStatus.OK);
    }

    @Operation(summary = "Create a new Neo4j coupon")
    @PostMapping("")
    public ResponseEntity<CouponNode> save(@RequestBody CouponNode couponNode) {
        return new ResponseEntity<>(couponNeo4jService.save(couponNode), HttpStatus.CREATED);
    }

    @Operation(summary = "Update an existing Neo4j coupon")
    @PutMapping("/{couponId}")
    public ResponseEntity<CouponNode> update(@PathVariable UUID couponId, @RequestBody CouponNode couponNode) {
        return new ResponseEntity<>(couponNeo4jService.update(couponId, couponNode), HttpStatus.OK);
    }

    @Operation(summary = "Delete a Neo4j coupon")
    @DeleteMapping("/{couponId}")
    public ResponseEntity<Void> delete(@PathVariable UUID couponId) {
        couponNeo4jService.deleteByCouponId(couponId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}

