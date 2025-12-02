package com.example.nordicelectronics.controller;

import com.example.nordicelectronics.service.DataMigrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "Data Migration Controller", description = "Handles data migration from PostgreSQL to MongoDB")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/migration")
public class DataMigrationController {

    private final DataMigrationService dataMigrationService;

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
        summary = "Health check for migration service",
        description = "Checks if the migration service is ready and accessible"
    )
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "service", "Data Migration Service",
            "message", "Ready to migrate data from PostgreSQL to MongoDB"
        ));
    }
}

