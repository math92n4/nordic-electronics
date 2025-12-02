package com.example.nordicelectronics.integration.repository;

import com.example.nordicelectronics.entity.Brand;
import com.example.nordicelectronics.entity.Product;
import com.example.nordicelectronics.integration.BaseIntegrationTest;
import com.example.nordicelectronics.repositories.sql.ProductRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Transactional
class ProductRepositoryIT extends BaseIntegrationTest {

    @Autowired
    private ProductRepository productRepository;

    @Test
    void shouldSaveAndRetrieveProduct() {
        // Arrange
        Brand brand = createTestBrand("Test Brand");
        Product product = createTestProduct("Test Product", brand);

        // Act
        entityManager.persist(product); // persist = save
        entityManager.flush(); // flush = commit to DB
        UUID productId = product.getProductId(); // get generated ID
        entityManager.clear(); // clear persistence context to force DB retrieval

        Product retrieved = productRepository.findById(productId).orElseThrow();

        // Assert
        assertNotNull(retrieved);
        assertEquals("Test Product", retrieved.getName());
        assertEquals("Test Brand", retrieved.getBrand().getName());
    }

    private Brand createTestBrand(String name) {
        Brand brand = new Brand();
        brand.setName(name);
        brand.setDescription(name + " Description");
        entityManager.persist(brand);
        entityManager.flush();
        return brand;
    }

    private Product createTestProduct(String name, Brand brand) {
        Product product = new Product();
        product.setName(name);
        product.setPrice(new BigDecimal("99.99"));
        product.setSku("SKU-" + System.currentTimeMillis());
        product.setDescription(name + " Description");
        product.setBrand(brand);
        product.setWeight(new BigDecimal("1.5"));
        // Set other required fields
        return product;
    }
}