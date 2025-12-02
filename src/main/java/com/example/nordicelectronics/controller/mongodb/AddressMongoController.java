package com.example.nordicelectronics.controller.mongodb;

import com.example.nordicelectronics.entity.mongodb.AddressDocument;
import com.example.nordicelectronics.service.mongodb.AddressMongoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "MongoDB Address Controller", description = "Handles operations related to addresses in MongoDB")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mongodb/addresses")
public class AddressMongoController {

    private final AddressMongoService addressMongoService;

    @Operation(summary = "Get all MongoDB addresses", description = "Fetches a list of all addresses.")
    @GetMapping("")
    public ResponseEntity<List<AddressDocument>> getAll() {
        return new ResponseEntity<>(addressMongoService.getAll(), HttpStatus.OK);
    }

    @Operation(summary = "Get MongoDB address by ID", description = "Fetches an address by its unique ID.")
    @GetMapping("/{addressId}")
    public ResponseEntity<AddressDocument> getByAddressId(@PathVariable UUID addressId) {
        return new ResponseEntity<>(addressMongoService.getByAddressId(addressId), HttpStatus.OK);
    }

    @Operation(summary = "Get MongoDB addresses by user ID", description = "Fetches all addresses for a specific user.")
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<AddressDocument>> getByUserId(@PathVariable UUID userId) {
        return new ResponseEntity<>(addressMongoService.getByUserId(userId), HttpStatus.OK);
    }

    @Operation(summary = "Create a new MongoDB address", description = "Creates a new address and returns the created address.")
    @PostMapping("")
    public ResponseEntity<AddressDocument> save(@RequestBody AddressDocument addressDocument) {
        return new ResponseEntity<>(addressMongoService.save(addressDocument), HttpStatus.CREATED);
    }

    @Operation(summary = "Update an existing MongoDB address", description = "Updates an existing address by its ID and returns the updated address.")
    @PutMapping("/{addressId}")
    public ResponseEntity<AddressDocument> update(@PathVariable UUID addressId, @RequestBody AddressDocument addressDocument) {
        return new ResponseEntity<>(addressMongoService.update(addressId, addressDocument), HttpStatus.OK);
    }

    @Operation(summary = "Delete a MongoDB address", description = "Deletes an address by its unique ID.")
    @DeleteMapping("/{addressId}")
    public ResponseEntity<Void> delete(@PathVariable UUID addressId) {
        addressMongoService.deleteByAddressId(addressId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}

