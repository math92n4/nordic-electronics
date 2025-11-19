package com.example.nordicelectronics.controller;

import com.example.nordicelectronics.service.MigrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Tag(name = "Migration Controller", description = "Handles data migration between PostgreSQL and MongoDB")
@RestController
@RequestMapping("/api/migration")
@RequiredArgsConstructor
@Slf4j
public class MigrationController {

    private final MigrationService migrationService;

    @PostMapping("/migrate-all")
    @Operation(summary = "Migrate all data from PostgreSQL to MongoDB", description = "Triggers the migration of all data from PostgreSQL to MongoDB.")
    public ResponseEntity<Map<String, String>> migrateAll() {
        log.info("Received request to migrate all data from PostgreSQL to MongoDB");
        Map<String, String> response = new HashMap<>();

        try {
            migrationService.migrateAll();
            response.put("status", "success");
            response.put("message", "All data migrated successfully from PostgreSQL to MongoDB");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Migration failed: {}", e.getMessage(), e);
            response.put("status", "error");
            response.put("message", "Migration failed: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}

