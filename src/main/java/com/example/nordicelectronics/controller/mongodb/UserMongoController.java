package com.example.nordicelectronics.controller.mongodb;

import com.example.nordicelectronics.entity.mongodb.UserDocument;
import com.example.nordicelectronics.service.mongodb.UserMongoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "MongoDB User Controller", description = "Handles operations related to users in MongoDB")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mongodb/users")
public class UserMongoController {

    private final UserMongoService userMongoService;

    @Operation(summary = "Get all MongoDB users", description = "Fetches a list of all users.")
    @GetMapping("")
    public ResponseEntity<List<UserDocument>> getAll() {
        return new ResponseEntity<>(userMongoService.getAll(), HttpStatus.OK);
    }

    @Operation(summary = "Get MongoDB user by ID", description = "Fetches a user by its unique ID.")
    @GetMapping("/{userId}")
    public ResponseEntity<UserDocument> getByUserId(@PathVariable UUID userId) {
        return new ResponseEntity<>(userMongoService.getByUserId(userId), HttpStatus.OK);
    }

    @Operation(summary = "Get MongoDB user by email", description = "Fetches a user by their email.")
    @GetMapping("/email/{email}")
    public ResponseEntity<UserDocument> getByEmail(@PathVariable String email) {
        return new ResponseEntity<>(userMongoService.getByEmail(email), HttpStatus.OK);
    }

    @Operation(summary = "Create a new MongoDB user", description = "Creates a new user and returns the created user.")
    @PostMapping("")
    public ResponseEntity<UserDocument> save(@RequestBody UserDocument userDocument) {
        return new ResponseEntity<>(userMongoService.save(userDocument), HttpStatus.CREATED);
    }

    @Operation(summary = "Update an existing MongoDB user", description = "Updates an existing user by its ID and returns the updated user.")
    @PutMapping("/{userId}")
    public ResponseEntity<UserDocument> update(@PathVariable UUID userId, @RequestBody UserDocument userDocument) {
        return new ResponseEntity<>(userMongoService.update(userId, userDocument), HttpStatus.OK);
    }

    @Operation(summary = "Delete a MongoDB user", description = "Deletes a user by its unique ID.")
    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> delete(@PathVariable UUID userId) {
        userMongoService.deleteByUserId(userId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}

