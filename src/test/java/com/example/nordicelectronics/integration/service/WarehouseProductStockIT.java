package com.example.nordicelectronics.integration.service;

import com.example.nordicelectronics.entity.*;
import com.example.nordicelectronics.entity.dto.address.AddressRequestDTO;
import com.example.nordicelectronics.entity.dto.order.OrderProductRequestDTO;
import com.example.nordicelectronics.entity.dto.order.OrderRequestDTO;
import com.example.nordicelectronics.integration.BaseIntegrationTest;
import com.example.nordicelectronics.service.OrderService;
import com.example.nordicelectronics.service.WarehouseProductService;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

public class WarehouseProductStockIT extends BaseIntegrationTest {

    @Autowired
    private WarehouseProductService warehouseProductService;
    @Autowired
    private OrderService orderService;

    // ====================================================
    // EP + BVA: Valid stock values
    // ====================================================
    @Test
    @DisplayName("Should save warehouse product with valid stock quantities")
    void shouldSaveWarehouseProductWithValidStock() {
        int maxStock = 1000;
        int[] validStocks = {0, 1, maxStock - 1, maxStock};


        for (int stock : validStocks) {
            // create a new key for each WarehouseProduct
            Warehouse warehouse = createAndPersistWarehouse("Main Warehouse");
            Product product = createAndPersistProduct("Product", new BigDecimal("100.00"));

            WarehouseProductKey key = new WarehouseProductKey();
            key.setProductId(product.getProductId());
            key.setWarehouseId(warehouse.getWarehouseId());

            WarehouseProduct wp = WarehouseProduct.builder()
                    .id(key)
                    .warehouse(warehouse)
                    .product(product)
                    .stockQuantity(stock)
                    .build();

            entityManager.persist(wp);
        }

        entityManager.flush();

        // Verify persisted stock quantities
        List<WarehouseProduct> list = entityManager
                .createQuery("SELECT wp FROM WarehouseProduct wp", WarehouseProduct.class)
                .getResultList();

        for (WarehouseProduct wp : list) {
            assertThat(wp.getStockQuantity()).isBetween(0, maxStock);
        }
    }

    // ====================================================
    // EP + BVA: Invalid negative stock
    // ====================================================
    @Test
    @DisplayName("Should not save warehouse product with negative stock")
    void shouldNotSaveWarehouseProductWithNegativeStock() {
        Warehouse warehouse = createAndPersistWarehouse("Secondary Warehouse");
        Product product = createAndPersistProduct("NegativeStockProduct", new BigDecimal("50.00"));

        WarehouseProductKey key = new WarehouseProductKey();
        key.setProductId(product.getProductId());
        key.setWarehouseId(warehouse.getWarehouseId());

        WarehouseProduct wp = WarehouseProduct.builder()
                .id(key)
                .warehouse(warehouse)
                .product(product)
                .stockQuantity(-1) // invalid
                .build();

        assertThatThrownBy(() -> {
            entityManager.persist(wp);
            entityManager.flush();
        }).isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    @DisplayName("Should not allow creating an order when product is not in stock")
    void shouldNotCreateOrderWhenProductNotInStock() {
        User testUser = User.builder()
                .firstName("Test User")
                .lastName("Last name")
                .phoneNumber("88888888")
                .dateOfBirth(LocalDate.now())
                .email("test@example.com")
                .password("password")
                .build();

        entityManager.persist(testUser);
        entityManager.flush();

        Warehouse warehouse = createAndPersistWarehouse("Empty Warehouse");
        Product product = createAndPersistProduct("OutOfStockProduct", new BigDecimal("150.00"));

        // Build OrderProductRequestDTO
        OrderProductRequestDTO orderProductRequest = OrderProductRequestDTO.builder()
                .productId(product.getProductId())
                .quantity(1) // trying to order 1
                .build();

        // Build OrderRequestDTO
        OrderRequestDTO orderRequest = OrderRequestDTO.builder()
                .userId(testUser.getUserId())
                .address(AddressRequestDTO.builder()
                        .street("Test Street")
                        .streetNumber("123")
                        .city("Test City")
                        .zip("1000")
                        .build())
                .orderProducts(List.of(orderProductRequest))
                .couponCode(null)
                .build();

        // Persisting the order should fail because the product has no stock
        assertThatThrownBy(() -> orderService.createOrder(orderRequest))
                .isInstanceOf(org.springframework.jdbc.UncategorizedSQLException.class)
                .hasMessageContaining("Insufficient stock for product");
    }


    // ====================================================
    // Helper methods
    // ====================================================
    private Product createAndPersistProduct(String name, BigDecimal price) {
        Brand brand = Brand.builder()
                .name("Test Brand")
                .description("Test brand description")
                .build();
        entityManager.persist(brand);

        Warranty warranty = Warranty.builder()
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusYears(1))
                .description("Test warranty")
                .build();
        entityManager.persist(warranty);

        Product product = Product.builder()
                .name(name)
                .sku("SKU-" + UUID.randomUUID().toString().substring(0, 8))
                .description("Test description")
                .price(price)
                .weight(new BigDecimal("1.0"))
                .brand(brand)
                .warranty(warranty)
                .build();

        entityManager.persist(product);
        return product;
    }

    private Warehouse createAndPersistWarehouse(String name) {
        Address address = Address.builder()
                .street("Test Street")
                .streetNumber("123")
                .city("Test City")
                .zip("1000")
                .build();
        entityManager.persist(address);

        Warehouse warehouse = Warehouse.builder()
                .name(name)
                .phoneNumber("12345678") // satisfy NOT NULL
                .address(address)         // satisfy NOT NULL
                .build();
        entityManager.persist(warehouse);
        return warehouse;
    }
}
