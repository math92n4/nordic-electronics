package com.example.nordicelectronics.controller.neo4j;

import com.example.nordicelectronics.entity.enums.PaymentStatus;
import com.example.nordicelectronics.entity.neo4j.PaymentNode;
import com.example.nordicelectronics.service.neo4j.PaymentNeo4jService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Neo4j Payment Controller", description = "Handles operations related to payments in Neo4j")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/neo4j/payments")
public class PaymentNeo4jController {

    private final PaymentNeo4jService paymentNeo4jService;

    @Operation(summary = "Get all Neo4j payments")
    @GetMapping("")
    public ResponseEntity<List<PaymentNode>> getAll() {
        return new ResponseEntity<>(paymentNeo4jService.getAll(), HttpStatus.OK);
    }

    @Operation(summary = "Get Neo4j payment by ID")
    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentNode> getByPaymentId(@PathVariable UUID paymentId) {
        return new ResponseEntity<>(paymentNeo4jService.getByPaymentId(paymentId), HttpStatus.OK);
    }

    @Operation(summary = "Get Neo4j payment by order ID")
    @GetMapping("/order/{orderId}")
    public ResponseEntity<PaymentNode> getByOrderId(@PathVariable UUID orderId) {
        return new ResponseEntity<>(paymentNeo4jService.getByOrderId(orderId), HttpStatus.OK);
    }

    @Operation(summary = "Get Neo4j payments by status")
    @GetMapping("/status/{status}")
    public ResponseEntity<List<PaymentNode>> getByStatus(@PathVariable PaymentStatus status) {
        return new ResponseEntity<>(paymentNeo4jService.getByStatus(status), HttpStatus.OK);
    }

    @Operation(summary = "Create a new Neo4j payment")
    @PostMapping("")
    public ResponseEntity<PaymentNode> save(@RequestBody PaymentNode paymentNode) {
        return new ResponseEntity<>(paymentNeo4jService.save(paymentNode), HttpStatus.CREATED);
    }

    @Operation(summary = "Update an existing Neo4j payment")
    @PutMapping("/{paymentId}")
    public ResponseEntity<PaymentNode> update(@PathVariable UUID paymentId, @RequestBody PaymentNode paymentNode) {
        return new ResponseEntity<>(paymentNeo4jService.update(paymentId, paymentNode), HttpStatus.OK);
    }

    @Operation(summary = "Delete a Neo4j payment")
    @DeleteMapping("/{paymentId}")
    public ResponseEntity<Void> delete(@PathVariable UUID paymentId) {
        paymentNeo4jService.deleteByPaymentId(paymentId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}

