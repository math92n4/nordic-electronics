package com.example.nordicelectronics.controller.mongodb;

import com.example.nordicelectronics.entity.enums.PaymentStatus;
import com.example.nordicelectronics.entity.mongodb.PaymentDocument;
import com.example.nordicelectronics.service.mongodb.PaymentMongoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "MongoDB Payment Controller", description = "Handles operations related to payments in MongoDB")
@RestController
@ConditionalOnProperty(name = "spring.data.mongodb.enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
@RequestMapping("/api/mongodb/payments")
public class PaymentMongoController {

    private final PaymentMongoService paymentMongoService;

    @Operation(summary = "Get all MongoDB payments", description = "Fetches a list of all payments.")
    @GetMapping("")
    public ResponseEntity<List<PaymentDocument>> getAll() {
        return new ResponseEntity<>(paymentMongoService.getAll(), HttpStatus.OK);
    }

    @Operation(summary = "Get MongoDB payment by ID", description = "Fetches a payment by its unique ID.")
    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentDocument> getByPaymentId(@PathVariable UUID paymentId) {
        return new ResponseEntity<>(paymentMongoService.getByPaymentId(paymentId), HttpStatus.OK);
    }

    @Operation(summary = "Get MongoDB payment by order ID", description = "Fetches a payment by order ID.")
    @GetMapping("/order/{orderId}")
    public ResponseEntity<PaymentDocument> getByOrderId(@PathVariable UUID orderId) {
        return new ResponseEntity<>(paymentMongoService.getByOrderId(orderId), HttpStatus.OK);
    }

    @Operation(summary = "Get MongoDB payments by status", description = "Fetches all payments with a specific status.")
    @GetMapping("/status/{status}")
    public ResponseEntity<List<PaymentDocument>> getByStatus(@PathVariable PaymentStatus status) {
        return new ResponseEntity<>(paymentMongoService.getByStatus(status), HttpStatus.OK);
    }

    @Operation(summary = "Create a new MongoDB payment", description = "Creates a new payment and returns the created payment.")
    @PostMapping("")
    public ResponseEntity<PaymentDocument> save(@RequestBody PaymentDocument paymentDocument) {
        return new ResponseEntity<>(paymentMongoService.save(paymentDocument), HttpStatus.CREATED);
    }

    @Operation(summary = "Update an existing MongoDB payment", description = "Updates an existing payment by its ID and returns the updated payment.")
    @PutMapping("/{paymentId}")
    public ResponseEntity<PaymentDocument> update(@PathVariable UUID paymentId, @RequestBody PaymentDocument paymentDocument) {
        return new ResponseEntity<>(paymentMongoService.update(paymentId, paymentDocument), HttpStatus.OK);
    }

    @Operation(summary = "Delete a MongoDB payment", description = "Deletes a payment by its unique ID.")
    @DeleteMapping("/{paymentId}")
    public ResponseEntity<Void> delete(@PathVariable UUID paymentId) {
        paymentMongoService.deleteByPaymentId(paymentId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}

