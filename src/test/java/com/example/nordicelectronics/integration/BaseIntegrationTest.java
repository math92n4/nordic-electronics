package com.example.nordicelectronics.integration;

import com.example.nordicelectronics.config.TestDatabaseContainer;
import com.example.nordicelectronics.entity.Category;
import com.example.nordicelectronics.entity.Product;
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

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Transactional
public abstract class BaseIntegrationTest {

    static {
        TestDatabaseContainer.getInstance(); // Ensure container is started
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", TestDatabaseContainer::getJdbcUrl);
        registry.add("spring.datasource.username", TestDatabaseContainer::getUsername);
        registry.add("spring.datasource.password", TestDatabaseContainer::getPassword);
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
