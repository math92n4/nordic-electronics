package com.example.nordicelectronics.integration.repository;

import com.example.nordicelectronics.entity.Brand;
import com.example.nordicelectronics.entity.Product;
import com.example.nordicelectronics.entity.Warranty;
import com.example.nordicelectronics.integration.BaseIntegrationTest;
import com.example.nordicelectronics.repositories.sql.ProductRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

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

        Product retrieved = productRepository.findById(productId).orElseThrow();

        // Assert
        assertNotNull(retrieved);
        assertEquals("Test Product", retrieved.getName());
        assertEquals("Test Brand", retrieved.getBrand().getName());
    }

    @Test
    void shouldUpdateProduct() {
        // Arrange
        Brand brand = createTestBrand("Original Brand");
        Product product = createTestProduct("Original Product", brand);

        entityManager.persist(product);
        entityManager.flush();
        UUID productId = product.getProductId();

        // Clear cache to ensure we're working with fresh data
        entityManager.clear();

        // Act - Update product
        Product toUpdate = productRepository.findById(productId).orElseThrow();
        toUpdate.setName("Updated Product");
        toUpdate.setPrice(BigDecimal.valueOf(399.99));

        entityManager.flush();
        entityManager.clear();

        // Assert
        Product updated = productRepository.findById(productId).orElseThrow();
        assertEquals("Updated Product", updated.getName());
        assertEquals(BigDecimal.valueOf(399.99), updated.getPrice());
        assertNotNull(updated.getUpdatedAt());
    }

    @Test
    void shouldSoftDeleteProduct() {
        // Arrange
        Brand brand = createTestBrand("Test Brand");
        Product product = createTestProduct("Product To Delete", brand);
        product.softDelete();

        entityManager.persist(product);
        entityManager.flush();

        UUID productId = product.getProductId();

        // Act
        Product softDeletedProduct = productRepository.findById(productId).orElseThrow();

        entityManager.persist(softDeletedProduct);
        entityManager.flush();

        assertNotNull(softDeletedProduct);
        assertNotNull(softDeletedProduct.getDeletedAt());
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
        Warranty warranty = new Warranty();
        warranty.setDescription("Warranty Description");
        warranty.setStartDate(LocalDate.now().minusDays(1));
        warranty.setEndDate(LocalDate.now().plusDays(1));
        entityManager.persist(warranty);
        entityManager.flush();

        Product product = new Product();
        product.setName(name);
        product.setPrice(new BigDecimal("99.99"));
        product.setSku("SKU-" + System.currentTimeMillis());
        product.setDescription(name + " Description");
        product.setBrand(brand);
        product.setWeight(new BigDecimal("1.5"));
        product.setWarranty(warranty);
        return product;
    }
}