package com.example.nordicelectronics.integration;

import com.example.nordicelectronics.entity.Category;
import com.example.nordicelectronics.entity.Product;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;

@Testcontainers
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Transactional
public abstract class BaseIntegrationTest {

    @Container
    public static PostgreSQLContainer<?> postgresContainer =
            new PostgreSQLContainer<>("postgres")
                    .withUsername("test")
                    .withPassword("test")
                    .withDatabaseName("nordic_test");
                    // .withInitScript("db/test-schema.sql"); // Optional: load your schema

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresContainer::getUsername);
        registry.add("spring.datasource.password", postgresContainer::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @PersistenceContext
    protected EntityManager entityManager; // For JPA tests

    @Autowired
    protected JdbcTemplate jdbcTemplate; // For stored procedure tests

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
