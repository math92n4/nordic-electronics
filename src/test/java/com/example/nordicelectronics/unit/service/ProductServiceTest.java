package com.example.nordicelectronics.unit.service;

import com.example.nordicelectronics.entity.Brand;
import com.example.nordicelectronics.entity.Category;
import com.example.nordicelectronics.entity.Product;
import com.example.nordicelectronics.entity.Warranty;
import com.example.nordicelectronics.repositories.sql.BrandRepository;
import com.example.nordicelectronics.repositories.sql.ProductRepository;
import com.example.nordicelectronics.repositories.sql.WarrantyRepository;
import com.example.nordicelectronics.service.BrandService;
import com.example.nordicelectronics.service.CategoryService;
import com.example.nordicelectronics.service.ProductService;
import com.example.nordicelectronics.service.WarrantyService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
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
    private BrandRepository brandRepository;

    @Mock
    private WarrantyRepository warrantyRepository;

    @Mock
    private BrandService brandService;

    @Mock
    private CategoryService categoryService;

    @Mock
    private WarrantyService warrantyService;

    @InjectMocks
    private ProductService productService;

    private Product testProduct;
    private Brand testBrand;
    private Warranty testWarranty;
    private UUID testProductId;

    @BeforeEach
    void setUp() {
        testProductId = UUID.randomUUID();

        testBrand = Brand.builder()
                .brandId(UUID.randomUUID())
                .name("Brand Name")
                .description("Brand Description")
                .build();

        testWarranty = Warranty.builder()
                .warrantyId(UUID.randomUUID())
                .description("Warranty Description")
                .build();

        testProduct = Product.builder()
                .productId(testProductId)
                .sku("ABC-123456")
                .name("Product Name")
                .description("Product Description")
                .price(new BigDecimal("999.99"))
                .weight(new BigDecimal("0.168"))
                .brand(testBrand)
                .warranty(testWarranty)
                .build();
    }

    // AAA Pattern tests
    @Test
    void shouldGetProductById() {
        // Arrange
        when(productRepository.findById(testProductId))
                .thenReturn(Optional.of(testProduct));

        // Act
        Product result = productService.getById(testProductId);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getProductId()).isEqualTo(testProductId);
        assertThat(result.getSku()).isEqualTo("ABC-123456");
        assertThat(result.getName()).isEqualTo("Product Name");

        verify(productRepository, times(1)).findById(testProductId);
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

        // Mock the service calls with specific UUIDs
        when(brandService.getById(brandId)).thenReturn(brand);
        when(categoryService.getById(categoryId)).thenReturn(category);
        when(warrantyService.getById(warrantyId)).thenReturn(warranty);

        Product newProduct = Product.builder()
                .sku("ABC-999999")
                .name("New Product")
                .description("Description")
                .price(new BigDecimal("500.00"))
                .weight(new BigDecimal("1.0"))
                .brand(brand)
                .warranty(warranty)
                .categories(Set.of(category))
                .build();

        when(productRepository.save(any(Product.class)))
                .thenAnswer(invocation -> {
                    Product product = invocation.getArgument(0);
                    product.setProductId(UUID.randomUUID());
                    return product;
                });

        // Act
        Product result = productService.save(newProduct);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getSku()).isEqualTo("ABC-999999");

        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void shouldDeleteProduct() {
        // Arrange
        // Act
        productService.deleteById(testProductId);

        // Assert
        verify(productRepository, times(1)).deleteById(testProductId);
    }

}
