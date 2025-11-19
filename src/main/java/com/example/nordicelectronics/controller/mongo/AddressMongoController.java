package com.example.nordicelectronics.controller.mongo;

import com.example.nordicelectronics.document.AddressDocument;
import com.example.nordicelectronics.service.mongo.AddressMongoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "MongoDB Address Controller", description = "Handles operations related to addresses in MongoDB")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mongodb/addresses")
public class AddressMongoController {

    private final AddressMongoService addressMongoService;

    @Operation(summary = "Get all addresses from MongoDB", description = "Fetches a list of all addresses from MongoDB.")
    @GetMapping("")
    public ResponseEntity<List<AddressDocument>> getAll() {
        return new ResponseEntity<>(addressMongoService.getAll(), HttpStatus.OK);
    }

    @Operation(summary = "Get address by ID from MongoDB", description = "Fetches an address by its unique ID from MongoDB.")
    @GetMapping("/{id}")
    public ResponseEntity<AddressDocument> getById(@PathVariable String id) {
        return new ResponseEntity<>(addressMongoService.getById(id), HttpStatus.OK);
    }

    @Operation(summary = "Get address by user ID from MongoDB", description = "Fetches an address by user ID from MongoDB.")
    @GetMapping("/user/{userId}")
    public ResponseEntity<AddressDocument> getByUserId(@PathVariable String userId) {
        return new ResponseEntity<>(addressMongoService.getByUserId(userId), HttpStatus.OK);
    }

    @Operation(summary = "Create a new address in MongoDB", description = "Creates a new address and returns the created address.")
    @PostMapping("")
    public ResponseEntity<AddressDocument> save(@RequestBody AddressDocument address) {
        return new ResponseEntity<>(addressMongoService.save(address), HttpStatus.CREATED);
    }

    @Operation(summary = "Update an existing address in MongoDB", description = "Updates an existing address by its ID and returns the updated address.")
    @PutMapping("/{id}")
    public ResponseEntity<AddressDocument> update(@PathVariable String id, @RequestBody AddressDocument address) {
        return new ResponseEntity<>(addressMongoService.update(id, address), HttpStatus.OK);
    }

    @Operation(summary = "Delete an address from MongoDB", description = "Deletes an address by its unique ID from MongoDB.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        addressMongoService.deleteById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Operation(summary = "Delete address by user ID from MongoDB", description = "Deletes an address by user ID from MongoDB.")
    @DeleteMapping("/user/{userId}")
    public ResponseEntity<Void> deleteByUserId(@PathVariable String userId) {
        addressMongoService.deleteByUserId(userId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}

