package com.example.nordicelectronics.controller.neo4j;

import com.example.nordicelectronics.entity.neo4j.UserNode;
import com.example.nordicelectronics.service.neo4j.UserNeo4jService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Neo4j User Controller", description = "Handles operations related to users in Neo4j")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/neo4j/users")
public class UserNeo4jController {

    private final UserNeo4jService userNeo4jService;

    @Operation(summary = "Get all Neo4j users")
    @GetMapping("")
    public ResponseEntity<List<UserNode>> getAll() {
        return new ResponseEntity<>(userNeo4jService.getAll(), HttpStatus.OK);
    }

    @Operation(summary = "Get Neo4j user by ID")
    @GetMapping("/{userId}")
    public ResponseEntity<UserNode> getByUserId(@PathVariable UUID userId) {
        return new ResponseEntity<>(userNeo4jService.getByUserId(userId), HttpStatus.OK);
    }

    @Operation(summary = "Get Neo4j user by email")
    @GetMapping("/email/{email}")
    public ResponseEntity<UserNode> getByEmail(@PathVariable String email) {
        return new ResponseEntity<>(userNeo4jService.getByEmail(email), HttpStatus.OK);
    }

    @Operation(summary = "Create a new Neo4j user")
    @PostMapping("")
    public ResponseEntity<UserNode> save(@RequestBody UserNode userNode) {
        return new ResponseEntity<>(userNeo4jService.save(userNode), HttpStatus.CREATED);
    }

    @Operation(summary = "Update an existing Neo4j user")
    @PutMapping("/{userId}")
    public ResponseEntity<UserNode> update(@PathVariable UUID userId, @RequestBody UserNode userNode) {
        return new ResponseEntity<>(userNeo4jService.update(userId, userNode), HttpStatus.OK);
    }

    @Operation(summary = "Delete a Neo4j user")
    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> delete(@PathVariable UUID userId) {
        userNeo4jService.deleteByUserId(userId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}

