package com.example.nordicelectronics.unit.service;

import com.example.nordicelectronics.entity.Brand;
import com.example.nordicelectronics.entity.Category;
import com.example.nordicelectronics.entity.Product;
import com.example.nordicelectronics.entity.Warranty;
import com.example.nordicelectronics.entity.dto.product.ProductRequestDTO;
import com.example.nordicelectronics.entity.dto.product.ProductResponseDTO;
import com.example.nordicelectronics.repositories.sql.ProductRepository;
import com.example.nordicelectronics.service.BrandService;
import com.example.nordicelectronics.service.CategoryService;
import com.example.nordicelectronics.service.ProductService;
import com.example.nordicelectronics.service.WarrantyService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private BrandService brandService;

    @Mock
    private CategoryService categoryService;

    @Mock
    private WarrantyService warrantyService;

    @InjectMocks
    private ProductService productService;

    private UUID productId;
    private Product existingProduct;

    @BeforeEach
    void setUp() {
        existingProduct = Product.builder()
                .productId(UUID.randomUUID())
                .sku("OLD-SKU-001")
                .name("Old Product Name")
                .description("Old description")
                .price(new BigDecimal("99.99"))
                .weight(new BigDecimal("1.5"))
                .brand(Brand.builder().name("Old Brand").build())
                .warranty(Warranty.builder().warrantyId(UUID.randomUUID()).description("Old Warranty").build())
                .categories(Set.of(Category.builder().categoryId(UUID.randomUUID()).name("Old Category").build()))
                .build();

        productId = existingProduct.getProductId();
    }

    @Test
    void duplicateSkuShouldThrowException() {
        // Arrange
        when(productRepository.findBySku(anyString()))
                .thenReturn(Optional.of(existingProduct));

        // Act & Assert
        assertThatThrownBy(() -> productService.save(ProductRequestDTO.builder()
                        .sku("OLD-SKU-001")
                        .name("name")
                        .description("description")
                        .price(new BigDecimal("99.99"))
                        .brandId(UUID.randomUUID())
                        .warrantyId(UUID.randomUUID())
                        .categoryIds(List.of(UUID.randomUUID()))
                        .build()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("SKU already exists");

        verify(productRepository, times(1)).findBySku(anyString());
    }

    @Test
    void shouldGetProductById() {
        // Arrange
        when(productRepository.findById(productId))
                .thenReturn(Optional.of(existingProduct));

        // Act
        ProductResponseDTO result = productService.getById(productId);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getProductId()).isEqualTo(productId);
        assertThat(result.getSku()).isEqualTo("OLD-SKU-001");
        assertThat(result.getName()).isEqualTo("Old Product Name");

        verify(productRepository, times(1)).findById(productId);
    }

    @Test
    void shouldThrowExceptionWhenProductNotFound() {
        // Arrange
        UUID nonExistentId = UUID.randomUUID();
        when(productRepository.findById(nonExistentId))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> productService.getById(nonExistentId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Product not found");

        verify(productRepository, times(1)).findById(nonExistentId);
    }

    @Test
    void shouldCreateProduct() {
        // Arrange
        UUID brandId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        UUID warrantyId = UUID.randomUUID();

        Brand brand = Brand.builder()
                .brandId(brandId)
                .name("Test Brand")
                .build();

        Category category = Category.builder()
                .categoryId(categoryId)
                .name("Test Category")
                .build();

        Warranty warranty = Warranty.builder()
                .warrantyId(warrantyId)
                .description("Test Warranty")
                .build();

        when(brandService.getById(brandId)).thenReturn(brand);
        when(categoryService.getById(categoryId)).thenReturn(category);
        when(warrantyService.getById(warrantyId)).thenReturn(warranty);

        ProductRequestDTO requestDTO = ProductRequestDTO.builder()
                .sku("ABC-999999")
                .name("New Product")
                .description("Description")
                .price(new BigDecimal("500.00"))
                .weight(new BigDecimal("1.0"))
                .brandId(brandId)
                .warrantyId(warrantyId)
                .categoryIds(List.of(categoryId))
                .build();

        Product savedProduct = Product.builder()
                .productId(UUID.randomUUID())
                .sku("ABC-999999")
                .name("New Product")
                .description("Description")
                .price(new BigDecimal("500.00"))
                .weight(new BigDecimal("1.0"))
                .brand(brand)
                .warranty(warranty)
                .categories(Set.of(category))
                .reviews(new HashSet<>())
                .build();

        when(productRepository.save(any(Product.class)))
                .thenReturn(savedProduct);

        // Act
        ProductResponseDTO result = productService.save(requestDTO);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getSku()).isEqualTo("ABC-999999");

        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void shouldSoftDeleteProduct() {
        // Arrange
        when(productRepository.findById(productId))
                .thenReturn(Optional.of(existingProduct));
        when(productRepository.save(any(Product.class))).thenReturn(existingProduct);

        // Act
        productService.deleteById(productId);

        // Assert - soft delete should find entity, set deletedAt, and save
        verify(productRepository, times(1)).findById(productId);
        verify(productRepository, times(1)).save(existingProduct);
        assertThat(existingProduct.getDeletedAt()).isNotNull();
    }

    @Test
    @DisplayName("deleteById throws EntityNotFoundException when product not found")
    void shouldThrowExceptionWhenDeletingNonExistentProduct() {
        // Arrange
        UUID nonExistentId = UUID.randomUUID();
        when(productRepository.findById(nonExistentId))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> productService.deleteById(nonExistentId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Product not found");

        verify(productRepository, times(1)).findById(nonExistentId);
        verify(productRepository, never()).save(any(Product.class));
    }
    
    @Test
    @DisplayName("Get all products")
    void shouldGetAllProducts() {
        // Arrange
        Product product1 = Product.builder()
                .productId(UUID.randomUUID())
                .sku("SKU-001")
                .name("Product 1")
                .build();

        Product product2 = Product.builder()
                .productId(UUID.randomUUID())
                .sku("SKU-002")
                .name("Product 2")
                .build();

        when(productRepository.findAll()).thenReturn(List.of(product1, product2));

        // Act
        List<ProductResponseDTO> results = productService.getAll();

        // Assert
        assertThat(results).isNotNull();
        assertThat(results).hasSize(2);
        assertThat(results).extracting("sku").containsExactlyInAnyOrder("SKU-001", "SKU-002");
        verify(productRepository, times(1)).findAll();
    }
}