package com.example.nordicelectronics.controller.mongo;

import com.example.nordicelectronics.document.PaymentDocument;
import com.example.nordicelectronics.service.mongo.PaymentMongoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "MongoDB Payment Controller", description = "Handles operations related to payments in MongoDB")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mongodb/payments")
public class PaymentMongoController {

    private final PaymentMongoService paymentMongoService;

    @Operation(summary = "Get all payments from MongoDB", description = "Fetches a list of all payments from MongoDB.")
    @GetMapping("")
    public ResponseEntity<List<PaymentDocument>> getAll() {
        return new ResponseEntity<>(paymentMongoService.getAll(), HttpStatus.OK);
    }

    @Operation(summary = "Get payment by ID from MongoDB", description = "Fetches a payment by its unique ID from MongoDB.")
    @GetMapping("/{id}")
    public ResponseEntity<PaymentDocument> getById(@PathVariable String id) {
        return new ResponseEntity<>(paymentMongoService.getById(id), HttpStatus.OK);
    }

    @Operation(summary = "Get payment by order ID from MongoDB", description = "Fetches a payment by order ID from MongoDB.")
    @GetMapping("/order/{orderId}")
    public ResponseEntity<PaymentDocument> getByOrderId(@PathVariable String orderId) {
        return new ResponseEntity<>(paymentMongoService.getByOrderId(orderId), HttpStatus.OK);
    }

    @Operation(summary = "Get payments by status from MongoDB", description = "Fetches payments by status from MongoDB.")
    @GetMapping("/status/{status}")
    public ResponseEntity<List<PaymentDocument>> getByStatus(@PathVariable String status) {
        return new ResponseEntity<>(paymentMongoService.getByStatus(status), HttpStatus.OK);
    }

    @Operation(summary = "Create a new payment in MongoDB", description = "Creates a new payment and returns the created payment.")
    @PostMapping("")
    public ResponseEntity<PaymentDocument> save(@RequestBody PaymentDocument payment) {
        return new ResponseEntity<>(paymentMongoService.save(payment), HttpStatus.CREATED);
    }

    @Operation(summary = "Update an existing payment in MongoDB", description = "Updates an existing payment by its ID and returns the updated payment.")
    @PutMapping("/{id}")
    public ResponseEntity<PaymentDocument> update(@PathVariable String id, @RequestBody PaymentDocument payment) {
        return new ResponseEntity<>(paymentMongoService.update(id, payment), HttpStatus.OK);
    }

    @Operation(summary = "Delete a payment from MongoDB", description = "Deletes a payment by its unique ID from MongoDB.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        paymentMongoService.deleteById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}

