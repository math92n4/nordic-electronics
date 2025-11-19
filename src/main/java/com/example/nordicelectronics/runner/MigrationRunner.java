package com.example.nordicelectronics.runner;

import com.example.nordicelectronics.service.MigrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Command-line runner for migration
 * 
 * To enable this runner, set the following property in application.properties:
 * migration.enabled=true
 * 
 * Or run with command line argument:
 * java -jar app.jar --migration.enabled=true
 */
@Component
@ConditionalOnProperty(name = "migration.enabled", havingValue = "true")
@RequiredArgsConstructor
@Slf4j
public class MigrationRunner implements CommandLineRunner {

    private final MigrationService migrationService;

    @Override
    public void run(String... args) throws Exception {
        log.info("========================================");
        log.info("Starting Migration Runner");
        log.info("========================================");

        try {
            migrationService.migrateAll();
            log.info("========================================");
            log.info("Migration completed successfully!");
            log.info("========================================");
        } catch (Exception e) {
            log.error("========================================");
            log.error("Migration failed!");
            log.error("========================================");
            log.error("Error: {}", e.getMessage(), e);
            throw e;
        }
    }
}

