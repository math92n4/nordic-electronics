package com.example.nordicelectronics.controller.mongo;

import com.example.nordicelectronics.document.UserDocument;
import com.example.nordicelectronics.service.mongo.UserMongoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "MongoDB User Controller", description = "Handles operations related to users in MongoDB (non-authenticated endpoints)")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mongodb/users")
public class UserMongoController {

    private final UserMongoService userMongoService;

    @Operation(summary = "Get all users from MongoDB", description = "Fetches a list of all users from MongoDB.")
    @GetMapping("")
    public ResponseEntity<List<UserDocument>> getAll() {
        return new ResponseEntity<>(userMongoService.getAll(), HttpStatus.OK);
    }

    @Operation(summary = "Get user by ID from MongoDB", description = "Fetches a user by its unique ID from MongoDB.")
    @GetMapping("/{id}")
    public ResponseEntity<UserDocument> getById(@PathVariable String id) {
        return new ResponseEntity<>(userMongoService.getById(id), HttpStatus.OK);
    }

    @Operation(summary = "Get user by email from MongoDB", description = "Fetches a user by email from MongoDB.")
    @GetMapping("/email/{email}")
    public ResponseEntity<UserDocument> getByEmail(@PathVariable String email) {
        return new ResponseEntity<>(userMongoService.getByEmail(email), HttpStatus.OK);
    }

    @Operation(summary = "Create a new user in MongoDB", description = "Creates a new user and returns the created user.")
    @PostMapping("")
    public ResponseEntity<UserDocument> save(@RequestBody UserDocument user) {
        return new ResponseEntity<>(userMongoService.save(user), HttpStatus.CREATED);
    }

    @Operation(summary = "Update an existing user in MongoDB", description = "Updates an existing user by its ID and returns the updated user.")
    @PutMapping("/{id}")
    public ResponseEntity<UserDocument> update(@PathVariable String id, @RequestBody UserDocument user) {
        return new ResponseEntity<>(userMongoService.update(id, user), HttpStatus.OK);
    }

    @Operation(summary = "Delete a user from MongoDB", description = "Deletes a user by its unique ID from MongoDB.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        userMongoService.deleteById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}

