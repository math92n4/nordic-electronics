package com.example.nordicelectronics.controller;

import com.example.nordicelectronics.service.DataMigrationService;
import com.example.nordicelectronics.service.Neo4jMigrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@Tag(name = "Data Migration Controller", description = "Handles data migration from PostgreSQL to MongoDB and Neo4j")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/migration")
public class DataMigrationController {

    private final DataMigrationService dataMigrationService;
    private final Neo4jMigrationService neo4jMigrationService;

    @Operation(
        summary = "Migrate all data from PostgreSQL to MongoDB",
        description = "Migration of all data from PostgreSQL to MongoDB"
    )
    @PostMapping("/postgresql-to-mongodb")
    public ResponseEntity<Map<String, Object>> migratePostgreSQLToMongoDB() {
        try {
            Map<String, Object> results = dataMigrationService.migrateAllData();
            return new ResponseEntity<>(results, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(
                Map.of(
                    "status", "FAILED",
                    "error", e.getMessage()
                ),
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    @Operation(
        summary = "Migrate all data from PostgreSQL to Neo4j",
        description = "Migration of all data from PostgreSQL to Neo4j graph database"
    )
    @PostMapping("/postgresql-to-neo4j")
    public ResponseEntity<Map<String, Object>> migratePostgreSQLToNeo4j() {
        try {
            Map<String, Object> results = neo4jMigrationService.migrateAllDataToNeo4j();
            return new ResponseEntity<>(results, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(
                Map.of(
                    "status", "FAILED",
                    "error", e.getMessage()
                ),
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }
}

