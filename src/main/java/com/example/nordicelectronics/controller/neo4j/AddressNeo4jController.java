package com.example.nordicelectronics.controller.neo4j;

import com.example.nordicelectronics.entity.neo4j.AddressNode;
import com.example.nordicelectronics.service.neo4j.AddressNeo4jService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Neo4j Address Controller", description = "Handles operations related to addresses in Neo4j")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/neo4j/addresses")
public class AddressNeo4jController {

    private final AddressNeo4jService addressNeo4jService;

    @Operation(summary = "Get all Neo4j addresses")
    @GetMapping("")
    public ResponseEntity<List<AddressNode>> getAll() {
        return new ResponseEntity<>(addressNeo4jService.getAll(), HttpStatus.OK);
    }

    @Operation(summary = "Get Neo4j address by ID")
    @GetMapping("/{addressId}")
    public ResponseEntity<AddressNode> getByAddressId(@PathVariable UUID addressId) {
        return new ResponseEntity<>(addressNeo4jService.getByAddressId(addressId), HttpStatus.OK);
    }

    @Operation(summary = "Get Neo4j addresses by user ID")
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<AddressNode>> getByUserId(@PathVariable UUID userId) {
        return new ResponseEntity<>(addressNeo4jService.getByUserId(userId), HttpStatus.OK);
    }

    @Operation(summary = "Create a new Neo4j address")
    @PostMapping("")
    public ResponseEntity<AddressNode> save(@RequestBody AddressNode addressNode) {
        return new ResponseEntity<>(addressNeo4jService.save(addressNode), HttpStatus.CREATED);
    }

    @Operation(summary = "Update an existing Neo4j address")
    @PutMapping("/{addressId}")
    public ResponseEntity<AddressNode> update(@PathVariable UUID addressId, @RequestBody AddressNode addressNode) {
        return new ResponseEntity<>(addressNeo4jService.update(addressId, addressNode), HttpStatus.OK);
    }

    @Operation(summary = "Delete a Neo4j address")
    @DeleteMapping("/{addressId}")
    public ResponseEntity<Void> delete(@PathVariable UUID addressId) {
        addressNeo4jService.deleteByAddressId(addressId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}

