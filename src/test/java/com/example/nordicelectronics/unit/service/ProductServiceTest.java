package com.example.nordicelectronics.unit.service;

import com.example.nordicelectronics.entity.Brand;
import com.example.nordicelectronics.entity.Category;
import com.example.nordicelectronics.entity.Product;
import com.example.nordicelectronics.entity.Warranty;
import com.example.nordicelectronics.entity.dto.product.ProductRequestDTO;
import com.example.nordicelectronics.entity.dto.product.ProductResponseDTO;
import com.example.nordicelectronics.entity.mapper.ProductMapper;
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
import java.util.ArrayList;
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
        productId = UUID.randomUUID();

        // Setup existing product with all fields populated
        existingProduct = new Product();
        existingProduct.setProductId(productId);
        existingProduct.setSku("OLD-SKU-001");
        existingProduct.setName("Old Product Name");
        existingProduct.setDescription("Old description");
        existingProduct.setPrice(new BigDecimal("99.99"));
        existingProduct.setWeight(new BigDecimal("1.5"));

        Brand oldBrand = new Brand();
        oldBrand.setBrandId(UUID.randomUUID());
        oldBrand.setName("Old Brand");
        existingProduct.setBrand(oldBrand);

        Warranty oldWarranty = new Warranty();
        oldWarranty.setWarrantyId(UUID.randomUUID());
        existingProduct.setWarranty(oldWarranty);

        Set<Category> oldCategories = new HashSet<>();
        Category oldCategory = new Category();
        oldCategory.setCategoryId(UUID.randomUUID());
        oldCategory.setName("Old Category");
        oldCategories.add(oldCategory);
        existingProduct.setCategories(oldCategories);
    }

    // AAA Pattern tests
    @Test
    void shouldGetProductById() {
        // Arrange
        when(productRepository.findById(existingProduct.getProductId()))
                .thenReturn(Optional.of(existingProduct));

        // Act
        ProductResponseDTO result = productService.getById(existingProduct.getProductId());

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getProductId()).isEqualTo(existingProduct.getProductId());
        assertThat(result.getSku()).isEqualTo("OLD-SKU-001");
        assertThat(result.getName()).isEqualTo("Old Product Name");

        verify(productRepository, times(1)).findById(existingProduct.getProductId());
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
    void shouldDeleteProduct() {
        // Arrange
        when(productRepository.findById(existingProduct.getProductId()))
                .thenReturn(Optional.of(existingProduct));
        when(productRepository.save(any(Product.class))).thenReturn(existingProduct);

        // Act
        productService.deleteById(existingProduct.getProductId());

        // Assert - soft delete should find entity, set deletedAt, and save
        verify(productRepository, times(1)).findById(existingProduct.getProductId());
        verify(productRepository, times(1)).save(existingProduct);
        assertThat(existingProduct.getDeletedAt()).isNotNull();
    }
    /**
     * BLACK-BOX TEST: Equivalence Partitioning
     * Partition: Valid update with all basic fields changed
     * Expected: All basic fields are updated correctly
     */
    @Test
    @DisplayName("Should update all basic product fields")
    void testUpdateBasicFields() {
        // Arrange
        when(productRepository.findById(productId)).thenReturn(Optional.of(existingProduct));

        ProductRequestDTO updateDTO = ProductRequestDTO.builder()
                .sku("NEW-SKU-001")
                .name("New Product Name")
                .description("New description")
                .price(new BigDecimal("149.99"))
                .weight(new BigDecimal("2.5"))
                .build();

        when(productRepository.save(any(Product.class))).thenReturn(existingProduct);

        // Act
        ProductResponseDTO result = productService.update(productId, updateDTO);

        // Assert - AAA Pattern
        assertThat(result.getSku()).isEqualTo("NEW-SKU-001");
        assertThat(result.getName()).isEqualTo("New Product Name");
        assertThat(result.getDescription()).isEqualTo("New description");
        assertThat(result.getPrice()).isEqualByComparingTo(new BigDecimal("149.99"));
        assertThat(result.getWeight()).isEqualByComparingTo(new BigDecimal("2.5"));

        verify(productRepository).save(existingProduct);
    }

    /**
     * WHITE-BOX TEST: Branch Coverage - Brand update branch (true path)
     * Covers: if (dto.getBrandId() != null)
     * Path: Condition true -> execute brand update
     */
    @Test
    @DisplayName("Should update brand when brandId is provided")
    void testUpdateWithNewBrand() {
        // Arrange
        when(productRepository.findById(productId)).thenReturn(Optional.of(existingProduct));

        UUID newBrandId = UUID.randomUUID();
        Brand newBrand = new Brand();
        newBrand.setBrandId(newBrandId);
        newBrand.setName("New Brand");

        ProductRequestDTO updateDTO = ProductRequestDTO.builder()
                .sku("SKU-001")
                .name("Product")
                .description("Description")
                .price(new BigDecimal("100.00"))
                .weight(new BigDecimal("1.0"))
                .brandId(newBrandId)
                .build();

        when(brandService.getById(newBrandId)).thenReturn(newBrand);
        when(productRepository.save(any(Product.class))).thenReturn(existingProduct);

        // Act
        ProductResponseDTO result = productService.update(productId, updateDTO);

        // Assert
        verify(brandService).getById(newBrandId);
        verify(productRepository).save(existingProduct);
    }

    /**
     * WHITE-BOX TEST: Branch Coverage - Brand update branch (false path - null brandId)
     * Covers: if (dto.getBrandId() != null)
     * Path: Condition false -> skip brand update
     */
    @Test
    @DisplayName("Should not update brand when brandId is null")
    void testUpdateWithNullBrand() {
        // Arrange
        when(productRepository.findById(productId)).thenReturn(Optional.of(existingProduct));

        Brand originalBrand = existingProduct.getBrand();
        ProductRequestDTO updateDTO = ProductRequestDTO.builder()
                .sku("SKU-001")
                .name("Product")
                .description("Description")
                .price(new BigDecimal("100.00"))
                .weight(new BigDecimal("1.0"))
                .brandId(null) // Null brandId
                .build();

        when(productRepository.save(any(Product.class))).thenReturn(existingProduct);

        // Act
        ProductResponseDTO result = productService.update(productId, updateDTO);

        // Assert
        verify(brandService, never()).getById(any(UUID.class));
        verify(productRepository).save(existingProduct);
    }

    /**
     * WHITE-BOX TEST: Branch Coverage - Warranty update branch (true path)
     * Covers: if (dto.getWarrantyId() != null)
     * Path: Condition true -> execute warranty update
     */
    @Test
    @DisplayName("Should update warranty when warrantyId is provided")
    void testUpdateWithNewWarranty() {
        // Arrange
        when(productRepository.findById(productId)).thenReturn(Optional.of(existingProduct));
        UUID newWarrantyId = UUID.randomUUID();
        Warranty newWarranty = new Warranty();
        newWarranty.setWarrantyId(newWarrantyId);

        ProductRequestDTO updateDTO = ProductRequestDTO.builder()
                .sku("SKU-001")
                .name("Product")
                .description("Description")
                .price(new BigDecimal("100.00"))
                .weight(new BigDecimal("1.0"))
                .warrantyId(newWarrantyId)
                .build();

        when(warrantyService.getById(newWarrantyId)).thenReturn(newWarranty);
        when(productRepository.save(any(Product.class))).thenReturn(existingProduct);

        // Act
        ProductResponseDTO result = productService.update(productId, updateDTO);

        // Assert
        verify(warrantyService).getById(newWarrantyId);
        verify(productRepository).save(existingProduct);
    }

    /**
     * WHITE-BOX TEST: Branch Coverage - Warranty update branch (false path - null warrantyId)
     * Covers: if (dto.getWarrantyId() != null)
     * Path: Condition false -> skip warranty update
     */
    @Test
    @DisplayName("Should not update warranty when warrantyId is null")
    void testUpdateWithNullWarranty() {
        // Arrange
        when(productRepository.findById(productId)).thenReturn(Optional.of(existingProduct));
        Warranty originalWarranty = existingProduct.getWarranty();

        ProductRequestDTO updateDTO = ProductRequestDTO.builder()
                .sku("SKU-001")
                .name("Product")
                .description("Description")
                .price(new BigDecimal("100.00"))
                .weight(new BigDecimal("1.0"))
                .warrantyId(null) // Null warrantyId
                .build();

        when(productRepository.save(any(Product.class))).thenReturn(existingProduct);

        // Act
        ProductResponseDTO result = productService.update(productId, updateDTO);

        // Assert
        verify(warrantyService, never()).getById(any(UUID.class));
        verify(productRepository).save(existingProduct);
    }


    /**
     * WHITE-BOX TEST: Branch Coverage - Categories update branch (true path)
     * Covers: if (dto.getCategoryIds() != null && !dto.getCategoryIds().isEmpty())
     * Path: Both conditions true -> execute category update
     */
    @Test
    @DisplayName("Should update categories when categoryIds are provided")
    void testUpdateWithNewCategories() {
        // Arrange
        when(productRepository.findById(productId)).thenReturn(Optional.of(existingProduct));
        UUID categoryId1 = UUID.randomUUID();
        UUID categoryId2 = UUID.randomUUID();

        Category category1 = new Category();
        category1.setCategoryId(categoryId1);
        category1.setName("Category 1");

        Category category2 = new Category();
        category2.setCategoryId(categoryId2);
        category2.setName("Category 2");

        ProductRequestDTO updateDTO = ProductRequestDTO.builder()
                .sku("SKU-001")
                .name("Product")
                .description("Description")
                .price(new BigDecimal("100.00"))
                .weight(new BigDecimal("1.0"))
                .categoryIds(List.of(categoryId1, categoryId2))
                .build();

        when(categoryService.getById(categoryId1)).thenReturn(category1);
        when(categoryService.getById(categoryId2)).thenReturn(category2);
        when(productRepository.save(any(Product.class))).thenReturn(existingProduct);

        // Act
        ProductResponseDTO result = productService.update(productId, updateDTO);

        // Assert
        verify(categoryService).getById(categoryId1);
        verify(categoryService).getById(categoryId2);
        verify(productRepository).save(existingProduct);
    }

    /**
     * WHITE-BOX TEST: Branch Coverage - Categories update branch (false path - null categoryIds)
     * Covers: if (dto.getCategoryIds() != null && !dto.getCategoryIds().isEmpty())
     * Path: First condition false -> skip category update
     */
    @Test
    @DisplayName("Should not update categories when categoryIds are null")
    void testUpdateWithNullCategories() {
        // Arrange
        when(productRepository.findById(productId)).thenReturn(Optional.of(existingProduct));
        Set<Category> originalCategories = existingProduct.getCategories();

        ProductRequestDTO updateDTO = ProductRequestDTO.builder()
                .sku("SKU-001")
                .name("Product")
                .description("Description")
                .price(new BigDecimal("100.00"))
                .weight(new BigDecimal("1.0"))
                .categoryIds(null) // Null categoryIds
                .build();

        when(productRepository.save(any(Product.class))).thenReturn(existingProduct);

        // Act
        ProductResponseDTO result = productService.update(productId, updateDTO);

        // Assert
        verify(categoryService, never()).getById(any(UUID.class));
        verify(productRepository).save(existingProduct);
    }

    /**
     * WHITE-BOX TEST: Branch Coverage - Categories update branch (false path - empty categoryIds)
     * Covers: if (dto.getCategoryIds() != null && !dto.getCategoryIds().isEmpty())
     * Path: First condition true, second false -> skip category update
     * BOUNDARY VALUE: Empty collection edge case
     */
    @Test
    @DisplayName("Should not update categories when categoryIds are empty")
    void testUpdateWithEmptyCategories() {
        // Arrange
        when(productRepository.findById(productId)).thenReturn(Optional.of(existingProduct));
        Set<Category> originalCategories = existingProduct.getCategories();

        ProductRequestDTO updateDTO = ProductRequestDTO.builder()
                .sku("SKU-001")
                .name("Product")
                .description("Description")
                .price(new BigDecimal("100.00"))
                .weight(new BigDecimal("1.0"))
                .categoryIds(new ArrayList<>()) // Empty list
                .build();

        when(productRepository.save(any(Product.class))).thenReturn(existingProduct);

        // Act
        ProductResponseDTO result = productService.update(productId, updateDTO);

        // Assert
        verify(categoryService, never()).getById(any(UUID.class));
        verify(productRepository).save(existingProduct);
    }

    /**
     * BLACK-BOX TEST: Combination testing
     * Tests combination of updating brand, warranty, and categories together
     */
    @Test
    @DisplayName("Should update brand, warranty, and categories together")
    void testUpdateAllRelatedEntities() {
        // Arrange
        when(productRepository.findById(productId)).thenReturn(Optional.of(existingProduct));
        UUID newBrandId = UUID.randomUUID();
        Brand newBrand = new Brand();
        newBrand.setBrandId(newBrandId);

        UUID newWarrantyId = UUID.randomUUID();
        Warranty newWarranty = new Warranty();
        newWarranty.setWarrantyId(newWarrantyId);

        UUID categoryId = UUID.randomUUID();
        Category category = new Category();
        category.setCategoryId(categoryId);

        ProductRequestDTO updateDTO = ProductRequestDTO.builder()
                .sku("SKU-001")
                .name("Product")
                .description("Description")
                .price(new BigDecimal("100.00"))
                .weight(new BigDecimal("1.0"))
                .brandId(newBrandId)
                .warrantyId(newWarrantyId)
                .categoryIds(List.of(categoryId))
                .build();

        when(brandService.getById(newBrandId)).thenReturn(newBrand);
        when(warrantyService.getById(newWarrantyId)).thenReturn(newWarranty);
        when(categoryService.getById(categoryId)).thenReturn(category);
        when(productRepository.save(any(Product.class))).thenReturn(existingProduct);

        // Act
        ProductResponseDTO result = productService.update(productId, updateDTO);

        // Assert
        verify(brandService).getById(newBrandId);
        verify(warrantyService).getById(newWarrantyId);
        verify(categoryService).getById(categoryId);
        verify(productRepository).save(existingProduct);
    }

    /**
     * BLACK-BOX TEST: Minimal update scenario
     * Only basic fields, no related entities updated
     */
    @Test
    @DisplayName("Should update only basic fields without touching related entities")
    void testUpdateOnlyBasicFieldsNoRelatedEntities() {
        // Arrange
        when(productRepository.findById(productId)).thenReturn(Optional.of(existingProduct));
        Brand originalBrand = existingProduct.getBrand();
        Warranty originalWarranty = existingProduct.getWarranty();
        Set<Category> originalCategories = existingProduct.getCategories();

        ProductRequestDTO updateDTO = ProductRequestDTO.builder()
                .sku("MINIMAL-SKU")
                .name("Minimal Product")
                .description("Minimal description")
                .price(new BigDecimal("50.00"))
                .weight(new BigDecimal("0.5"))
                // No brandId, warrantyId, or categoryIds set
                .build();

        when(productRepository.save(any(Product.class))).thenReturn(existingProduct);

        // Act
        ProductResponseDTO result = productService.update(productId, updateDTO);

        // Assert
        assertThat(result.getSku()).isEqualTo("MINIMAL-SKU");
        assertThat(result.getName()).isEqualTo("Minimal Product");
        verify(brandService, never()).getById(any(UUID.class));
        verify(warrantyService, never()).getById(any(UUID.class));
        verify(categoryService, never()).getById(any(UUID.class));
        verify(productRepository).save(existingProduct);
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
        assertThat(results).hasSize(3);
        assertThat(results).extracting("sku").containsExactlyInAnyOrder("SKU-001", "SKU-002");

        verify(productRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("getEntityById should return Product when found")
    void testGetEntityByIdFound() {
        // Arrange
        when(productRepository.findById(productId)).thenReturn(Optional.of(existingProduct));

        // Act
        Product result = productService.getEntityById(productId);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getProductId()).isEqualTo(productId);

        verify(productRepository, times(1)).findById(productId);
    }




}







