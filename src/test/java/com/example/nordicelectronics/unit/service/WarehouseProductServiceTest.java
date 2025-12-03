package com.example.nordicelectronics.unit.service;

import com.example.nordicelectronics.entity.Product;
import com.example.nordicelectronics.entity.Warehouse;
import com.example.nordicelectronics.entity.WarehouseProduct;
import com.example.nordicelectronics.entity.WarehouseProductKey;
import com.example.nordicelectronics.repositories.sql.ProductRepository;
import com.example.nordicelectronics.repositories.sql.WarehouseProductRepository;
import com.example.nordicelectronics.repositories.sql.WarehouseRepository;
import com.example.nordicelectronics.service.WarehouseProductService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WarehouseProductServiceTest {

    @Mock
    private WarehouseProductRepository warehouseProductRepository;

    @Mock
    private WarehouseRepository warehouseRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private WarehouseProductService warehouseProductService;

    private UUID testWarehouseId;
    private UUID testProductId;
    private WarehouseProductKey testKey;
    private Warehouse testWarehouse;
    private Product testProduct;
    private WarehouseProduct testWarehouseProduct;

    @BeforeEach
    void setUp() {
        testWarehouseId = UUID.randomUUID();
        testProductId = UUID.randomUUID();

        testKey = new WarehouseProductKey();
        testKey.setWarehouseId(testWarehouseId);
        testKey.setProductId(testProductId);

        testWarehouse = Warehouse.builder()
            .warehouseId(testWarehouseId)
            .name("Main Warehouse")
            .phoneNumber("12345678")
            .build();

        testProduct = Product.builder()
            .productId(testProductId)
            .name("Test Product")
            .build();

        testWarehouseProduct = WarehouseProduct.builder()
            .id(testKey)
            .warehouse(testWarehouse)
            .product(testProduct)
            .stockQuantity(100)
            .build();
    }

    // ===== GET ALL TESTS =====

    @Test
    void getAll_shouldReturnListOfWarehouseProducts() {
        // Arrange
        List<WarehouseProduct> products = Arrays.asList(testWarehouseProduct);
        when(warehouseProductRepository.findAll()).thenReturn(products);

        // Act
        List<WarehouseProduct> result = warehouseProductService.getAll();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(100, result.get(0).getStockQuantity());

        verify(warehouseProductRepository).findAll();
    }

    @Test
    void getAll_shouldReturnEmptyList_whenNoWarehouseProductsExist() {
        // Arrange
        when(warehouseProductRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<WarehouseProduct> result = warehouseProductService.getAll();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(warehouseProductRepository).findAll();
    }

    // ===== GET BY ID TESTS =====

    @Test
    void getById_shouldReturnWarehouseProduct_whenExists() {
        // Arrange
        when(warehouseProductRepository.findById(any(WarehouseProductKey.class)))
            .thenReturn(Optional.of(testWarehouseProduct));

        // Act
        WarehouseProduct result = warehouseProductService.getById(testWarehouseId, testProductId);

        // Assert
        assertNotNull(result);
        assertEquals(100, result.getStockQuantity());
        assertEquals(testWarehouseId, result.getId().getWarehouseId());
        assertEquals(testProductId, result.getId().getProductId());

        verify(warehouseProductRepository).findById(any(WarehouseProductKey.class));
    }

    @Test
    void getById_shouldThrowEntityNotFoundException_whenNotFound() {
        // Arrange
        UUID nonExistentWarehouseId = UUID.randomUUID();
        UUID nonExistentProductId = UUID.randomUUID();
        when(warehouseProductRepository.findById(any(WarehouseProductKey.class)))
            .thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
            () -> warehouseProductService.getById(nonExistentWarehouseId, nonExistentProductId));

        assertTrue(exception.getMessage().contains("WarehouseProduct not found"));
        verify(warehouseProductRepository).findById(any(WarehouseProductKey.class));
    }

    // ===== SAVE TESTS =====

    @Test
    void save_shouldCreateAndReturnWarehouseProduct() {
        // Arrange
        when(warehouseRepository.findById(testWarehouseId)).thenReturn(Optional.of(testWarehouse));
        when(productRepository.findById(testProductId)).thenReturn(Optional.of(testProduct));
        when(warehouseProductRepository.save(any(WarehouseProduct.class))).thenReturn(testWarehouseProduct);

        // Act
        WarehouseProduct result = warehouseProductService.save(testWarehouseId, testProductId, 100);

        // Assert
        assertNotNull(result);
        assertEquals(100, result.getStockQuantity());
        assertEquals(testWarehouse, result.getWarehouse());
        assertEquals(testProduct, result.getProduct());

        verify(warehouseRepository).findById(testWarehouseId);
        verify(productRepository).findById(testProductId);
        verify(warehouseProductRepository).save(any(WarehouseProduct.class));
    }

    @Test
    void save_shouldThrowEntityNotFoundException_whenWarehouseNotFound() {
        // Arrange
        when(warehouseRepository.findById(testWarehouseId)).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
            () -> warehouseProductService.save(testWarehouseId, testProductId, 100));

        assertEquals("Warehouse not found", exception.getMessage());
        verify(warehouseRepository).findById(testWarehouseId);
        verify(productRepository, never()).findById(any());
        verify(warehouseProductRepository, never()).save(any());
    }

    @Test
    void save_shouldThrowEntityNotFoundException_whenProductNotFound() {
        // Arrange
        when(warehouseRepository.findById(testWarehouseId)).thenReturn(Optional.of(testWarehouse));
        when(productRepository.findById(testProductId)).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
            () -> warehouseProductService.save(testWarehouseId, testProductId, 100));

        assertEquals("Product not found", exception.getMessage());
        verify(warehouseRepository).findById(testWarehouseId);
        verify(productRepository).findById(testProductId);
        verify(warehouseProductRepository, never()).save(any());
    }

    // ===== UPDATE STOCK TESTS =====

    @Test
    void updateStock_shouldUpdateAndReturnWarehouseProduct() {
        // Arrange
        WarehouseProduct updatedProduct = WarehouseProduct.builder()
            .id(testKey)
            .warehouse(testWarehouse)
            .product(testProduct)
            .stockQuantity(200)
            .build();

        when(warehouseProductRepository.findById(any(WarehouseProductKey.class)))
            .thenReturn(Optional.of(testWarehouseProduct));
        when(warehouseProductRepository.save(any(WarehouseProduct.class))).thenReturn(updatedProduct);

        // Act
        WarehouseProduct result = warehouseProductService.updateStock(testWarehouseId, testProductId, 200);

        // Assert
        assertNotNull(result);
        assertEquals(200, result.getStockQuantity());

        verify(warehouseProductRepository).findById(any(WarehouseProductKey.class));
        verify(warehouseProductRepository).save(any(WarehouseProduct.class));
    }

    @Test
    void updateStock_shouldThrowEntityNotFoundException_whenNotFound() {
        // Arrange
        when(warehouseProductRepository.findById(any(WarehouseProductKey.class)))
            .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class,
            () -> warehouseProductService.updateStock(testWarehouseId, testProductId, 200));

        verify(warehouseProductRepository).findById(any(WarehouseProductKey.class));
        verify(warehouseProductRepository, never()).save(any());
    }

    // ===== DELETE BY ID TESTS =====

    @Test
    void deleteById_shouldCallRepositoryDeleteById() {
        // Arrange
        doNothing().when(warehouseProductRepository).deleteById(any(WarehouseProductKey.class));

        // Act
        warehouseProductService.deleteById(testWarehouseId, testProductId);

        // Assert
        verify(warehouseProductRepository).deleteById(any(WarehouseProductKey.class));
    }
}

