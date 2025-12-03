package com.example.nordicelectronics.integration;

import com.example.nordicelectronics.config.TestDatabaseContainer;
import com.example.nordicelectronics.entity.Category;
import com.example.nordicelectronics.entity.Product;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Transactional
public abstract class BaseIntegrationTest {

    @PersistenceContext
    protected EntityManager entityManager;

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    private static boolean schemaInitialized = false;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", TestDatabaseContainer.getInstance()::getJdbcUrl);
        registry.add("spring.datasource.username", TestDatabaseContainer.getInstance()::getUsername);
        registry.add("spring.datasource.password", TestDatabaseContainer.getInstance()::getPassword);
    }

    @PostConstruct
    void initializeDatabaseObjects() {
        if (!schemaInitialized) {
            try {
                String sql = new String(Files.readAllBytes(
                        Paths.get("src/main/resources/db/init.sql")
                ), StandardCharsets.UTF_8);

                // Remove pg_cron related code (not available in test container)
                sql = removePgCronCode(sql);

                jdbcTemplate.execute(sql);

                schemaInitialized = true;
                System.out.println("Database objects initialized from init.sql");
            } catch (Exception e) {
                System.err.println("Failed to initialize database objects: " + e.getMessage());
                throw new RuntimeException("Could not initialize test database", e);
            }
        }
    }

    /**
     * Removes pg_cron extension and cron.schedule() calls from SQL since
     * pg_cron is not available in the PostgreSQL test container.
     */
    private String removePgCronCode(String sql) {
        // Remove CREATE EXTENSION for pg_cron
        sql = sql.replaceAll("(?i)CREATE\\s+EXTENSION\\s+IF\\s+NOT\\s+EXISTS\\s+pg_cron\\s*;", "");
        
        // Remove everything after "EVENTS (pg_cron)" section comment
        // This is cleaner than trying to parse complex SQL with nested semicolons
        int eventsSection = sql.indexOf("-- EVENTS (pg_cron)");
        if (eventsSection != -1) {
            sql = sql.substring(0, eventsSection);
        }
        
        return sql;
    }

    @BeforeEach
    void setUpBase() {
        // Clean database before each test
        //cleanDatabase();
    }

    protected void cleanDatabase() {
        jdbcTemplate.execute("TRUNCATE TABLE product, category, \"order\" CASCADE");
    }

    protected Product createTestProduct(String name, BigDecimal price) {
        return Product.builder()
                .name(name)
                .price(price)
                .build();
    }

    protected Category createTestCategory(String name) {
        return Category.builder()
                .name(name)
                .build();
    }
}