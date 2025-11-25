package com.example.nordicelectronics.unit.service;

import com.example.nordicelectronics.entity.Brand;
import com.example.nordicelectronics.entity.Category;
import com.example.nordicelectronics.entity.Product;
import com.example.nordicelectronics.entity.Warranty;
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
    private Product updateProduct;

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
        Product result = productService.getById(existingProduct.getProductId());

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
        productService.deleteById(existingProduct.getProductId());

        // Assert
        verify(productRepository, times(1)).deleteById(existingProduct.getProductId());
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

        updateProduct = new Product();
        updateProduct.setSku("NEW-SKU-001");
        updateProduct.setName("New Product Name");
        updateProduct.setDescription("New description");
        updateProduct.setPrice(new BigDecimal("149.99"));
        updateProduct.setWeight(new BigDecimal("2.5"));

        when(productRepository.save(any(Product.class))).thenReturn(existingProduct);

        // Act
        Product result = productService.update(productId, updateProduct);

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
     * Covers: if (product.getBrand() != null && product.getBrand().getBrandId() != null)
     * Path: Both conditions true -> execute brand update
     */
    @Test
    @DisplayName("Should update brand when brand and brandId are provided")
    void testUpdateWithNewBrand() {
        // Arrange
        when(productRepository.findById(productId)).thenReturn(Optional.of(existingProduct));

        UUID newBrandId = UUID.randomUUID();
        Brand newBrand = new Brand();
        newBrand.setBrandId(newBrandId);
        newBrand.setName("New Brand");

        updateProduct = new Product();
        updateProduct.setSku("SKU-001");
        updateProduct.setName("Product");
        updateProduct.setDescription("Description");
        updateProduct.setPrice(new BigDecimal("100.00"));
        updateProduct.setWeight(new BigDecimal("1.0"));
        updateProduct.setBrand(newBrand);

        when(brandService.getById(newBrandId)).thenReturn(newBrand);
        when(productRepository.save(any(Product.class))).thenReturn(existingProduct);

        // Act
        Product result = productService.update(productId, updateProduct);

        // Assert
        assertThat(result.getBrand()).isEqualTo(newBrand);
        verify(brandService).getById(newBrandId);
        verify(productRepository).save(existingProduct);
    }

    /**
     * WHITE-BOX TEST: Branch Coverage - Brand update branch (false path - null brand)
     * Covers: if (product.getBrand() != null && product.getBrand().getBrandId() != null)
     * Path: First condition false -> skip brand update
     */
    @Test
    @DisplayName("Should not update brand when brand is null")
    void testUpdateWithNullBrand() {
        // Arrange
        when(productRepository.findById(productId)).thenReturn(Optional.of(existingProduct));

        Brand originalBrand = existingProduct.getBrand();
        updateProduct = new Product();
        updateProduct.setSku("SKU-001");
        updateProduct.setName("Product");
        updateProduct.setDescription("Description");
        updateProduct.setPrice(new BigDecimal("100.00"));
        updateProduct.setWeight(new BigDecimal("1.0"));
        updateProduct.setBrand(null); // Null brand

        when(productRepository.save(any(Product.class))).thenReturn(existingProduct);

        // Act
        Product result = productService.update(productId, updateProduct);

        // Assert
        assertThat(result.getBrand()).isEqualTo(originalBrand); // Brand unchanged
        verify(brandService, never()).getById(any(UUID.class));
        verify(productRepository).save(existingProduct);
    }

    /**
     * WHITE-BOX TEST: Branch Coverage - Brand update branch (false path - null brandId)
     * Covers: if (product.getBrand() != null && product.getBrand().getBrandId() != null)
     * Path: First condition true, second false -> skip brand update
     */
    @Test
    @DisplayName("Should not update brand when brandId is null")
    void testUpdateWithNullBrandId() {
        // Arrange
        when(productRepository.findById(productId)).thenReturn(Optional.of(existingProduct));
        Brand originalBrand = existingProduct.getBrand();

        Brand brandWithoutId = new Brand();
        brandWithoutId.setBrandId(null); // Null brandId
        brandWithoutId.setName("Brand Without ID");

        updateProduct = new Product();
        updateProduct.setSku("SKU-001");
        updateProduct.setName("Product");
        updateProduct.setDescription("Description");
        updateProduct.setPrice(new BigDecimal("100.00"));
        updateProduct.setWeight(new BigDecimal("1.0"));
        updateProduct.setBrand(brandWithoutId);

        when(productRepository.save(any(Product.class))).thenReturn(existingProduct);

        // Act
        Product result = productService.update(productId, updateProduct);

        // Assert
        assertThat(result.getBrand()).isEqualTo(originalBrand); // Brand unchanged
        verify(brandService, never()).getById(any(UUID.class));
        verify(productRepository).save(existingProduct);
    }

    /**
     * WHITE-BOX TEST: Branch Coverage - Warranty update branch (true path)
     * Covers: if (product.getWarranty() != null && product.getWarranty().getWarrantyId() != null)
     * Path: Both conditions true -> execute warranty update
     */
    @Test
    @DisplayName("Should update warranty when warranty and warrantyId are provided")
    void testUpdateWithNewWarranty() {
        // Arrange
        when(productRepository.findById(productId)).thenReturn(Optional.of(existingProduct));
        UUID newWarrantyId = UUID.randomUUID();
        Warranty newWarranty = new Warranty();
        newWarranty.setWarrantyId(newWarrantyId);

        updateProduct = new Product();
        updateProduct.setSku("SKU-001");
        updateProduct.setName("Product");
        updateProduct.setDescription("Description");
        updateProduct.setPrice(new BigDecimal("100.00"));
        updateProduct.setWeight(new BigDecimal("1.0"));
        updateProduct.setWarranty(newWarranty);

        when(warrantyService.getById(newWarrantyId)).thenReturn(newWarranty);
        when(productRepository.save(any(Product.class))).thenReturn(existingProduct);

        // Act
        Product result = productService.update(productId, updateProduct);

        // Assert
        assertThat(result.getWarranty()).isEqualTo(newWarranty);
        verify(warrantyService).getById(newWarrantyId);
        verify(productRepository).save(existingProduct);
    }

    /**
     * WHITE-BOX TEST: Branch Coverage - Warranty update branch (false path - null warranty)
     * Covers: if (product.getWarranty() != null && product.getWarranty().getWarrantyId() != null)
     * Path: First condition false -> skip warranty update
     */
    @Test
    @DisplayName("Should not update warranty when warranty is null")
    void testUpdateWithNullWarranty() {
        // Arrange
        when(productRepository.findById(productId)).thenReturn(Optional.of(existingProduct));
        Warranty originalWarranty = existingProduct.getWarranty();

        updateProduct = new Product();
        updateProduct.setSku("SKU-001");
        updateProduct.setName("Product");
        updateProduct.setDescription("Description");
        updateProduct.setPrice(new BigDecimal("100.00"));
        updateProduct.setWeight(new BigDecimal("1.0"));
        updateProduct.setWarranty(null); // Null warranty

        when(productRepository.save(any(Product.class))).thenReturn(existingProduct);

        // Act
        Product result = productService.update(productId, updateProduct);

        // Assert
        assertThat(result.getWarranty()).isEqualTo(originalWarranty); // Warranty unchanged
        verify(warrantyService, never()).getById(any(UUID.class));
        verify(productRepository).save(existingProduct);
    }

    /**
     * WHITE-BOX TEST: Branch Coverage - Warranty update branch (false path - null warrantyId)
     * Covers: if (product.getWarranty() != null && product.getWarranty().getWarrantyId() != null)
     * Path: First condition true, second false -> skip warranty update
     */
    @Test
    @DisplayName("Should not update warranty when warrantyId is null")
    void testUpdateWithNullWarrantyId() {
        // Arrange
        when(productRepository.findById(productId)).thenReturn(Optional.of(existingProduct));
        Warranty originalWarranty = existingProduct.getWarranty();

        Warranty warrantyWithoutId = new Warranty();
        warrantyWithoutId.setWarrantyId(null); // Null warrantyId

        updateProduct = new Product();
        updateProduct.setSku("SKU-001");
        updateProduct.setName("Product");
        updateProduct.setDescription("Description");
        updateProduct.setPrice(new BigDecimal("100.00"));
        updateProduct.setWeight(new BigDecimal("1.0"));
        updateProduct.setWarranty(warrantyWithoutId);

        when(productRepository.save(any(Product.class))).thenReturn(existingProduct);

        // Act
        Product result = productService.update(productId, updateProduct);

        // Assert
        assertThat(result.getWarranty()).isEqualTo(originalWarranty); // Warranty unchanged
        verify(warrantyService, never()).getById(any(UUID.class));
        verify(productRepository).save(existingProduct);
    }

    /**
     * WHITE-BOX TEST: Branch Coverage - Categories update branch (true path)
     * Covers: if (product.getCategories() != null && !product.getCategories().isEmpty())
     * Path: Both conditions true -> execute category update
     */
    @Test
    @DisplayName("Should update categories when categories are provided")
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

        Set<Category> newCategories = new HashSet<>();
        newCategories.add(category1);
        newCategories.add(category2);

        updateProduct = new Product();
        updateProduct.setSku("SKU-001");
        updateProduct.setName("Product");
        updateProduct.setDescription("Description");
        updateProduct.setPrice(new BigDecimal("100.00"));
        updateProduct.setWeight(new BigDecimal("1.0"));
        updateProduct.setCategories(newCategories);

        when(categoryService.getById(categoryId1)).thenReturn(category1);
        when(categoryService.getById(categoryId2)).thenReturn(category2);
        when(productRepository.save(any(Product.class))).thenReturn(existingProduct);

        // Act
        Product result = productService.update(productId, updateProduct);

        // Assert
        assertThat(result.getCategories()).hasSize(2);
        assertThat(result.getCategories()).containsExactlyInAnyOrder(category1, category2);
        verify(categoryService).getById(categoryId1);
        verify(categoryService).getById(categoryId2);
        verify(productRepository).save(existingProduct);
    }

    /**
     * WHITE-BOX TEST: Branch Coverage - Categories update branch (false path - null categories)
     * Covers: if (product.getCategories() != null && !product.getCategories().isEmpty())
     * Path: First condition false -> skip category update
     */
    @Test
    @DisplayName("Should not update categories when categories are null")
    void testUpdateWithNullCategories() {
        // Arrange
        when(productRepository.findById(productId)).thenReturn(Optional.of(existingProduct));
        Set<Category> originalCategories = existingProduct.getCategories();

        updateProduct = new Product();
        updateProduct.setSku("SKU-001");
        updateProduct.setName("Product");
        updateProduct.setDescription("Description");
        updateProduct.setPrice(new BigDecimal("100.00"));
        updateProduct.setWeight(new BigDecimal("1.0"));
        updateProduct.setCategories(null); // Null categories

        when(productRepository.save(any(Product.class))).thenReturn(existingProduct);

        // Act
        Product result = productService.update(productId, updateProduct);

        // Assert
        assertThat(result.getCategories()).isEqualTo(originalCategories); // Categories unchanged
        verify(categoryService, never()).getById(any(UUID.class));
        verify(productRepository).save(existingProduct);
    }

    /**
     * WHITE-BOX TEST: Branch Coverage - Categories update branch (false path - empty categories)
     * Covers: if (product.getCategories() != null && !product.getCategories().isEmpty())
     * Path: First condition true, second false -> skip category update
     * BOUNDARY VALUE: Empty collection edge case
     */
    @Test
    @DisplayName("Should not update categories when categories are empty")
    void testUpdateWithEmptyCategories() {
        // Arrange
        when(productRepository.findById(productId)).thenReturn(Optional.of(existingProduct));
        Set<Category> originalCategories = existingProduct.getCategories();

        updateProduct = new Product();
        updateProduct.setSku("SKU-001");
        updateProduct.setName("Product");
        updateProduct.setDescription("Description");
        updateProduct.setPrice(new BigDecimal("100.00"));
        updateProduct.setWeight(new BigDecimal("1.0"));
        updateProduct.setCategories(new HashSet<>()); // Empty set

        when(productRepository.save(any(Product.class))).thenReturn(existingProduct);

        // Act
        Product result = productService.update(productId, updateProduct);

        // Assert
        assertThat(result.getCategories()).isEqualTo(originalCategories); // Categories unchanged
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

        Set<Category> categories = new HashSet<>();
        categories.add(category);

        updateProduct = new Product();
        updateProduct.setSku("SKU-001");
        updateProduct.setName("Product");
        updateProduct.setDescription("Description");
        updateProduct.setPrice(new BigDecimal("100.00"));
        updateProduct.setWeight(new BigDecimal("1.0"));
        updateProduct.setBrand(newBrand);
        updateProduct.setWarranty(newWarranty);
        updateProduct.setCategories(categories);

        when(brandService.getById(newBrandId)).thenReturn(newBrand);
        when(warrantyService.getById(newWarrantyId)).thenReturn(newWarranty);
        when(categoryService.getById(categoryId)).thenReturn(category);
        when(productRepository.save(any(Product.class))).thenReturn(existingProduct);

        // Act
        Product result = productService.update(productId, updateProduct);

        // Assert
        assertThat(result.getBrand()).isEqualTo(newBrand);
        assertThat(result.getWarranty()).isEqualTo(newWarranty);
        assertThat(result.getCategories()).containsExactly(category);
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

        updateProduct = new Product();
        updateProduct.setSku("MINIMAL-SKU");
        updateProduct.setName("Minimal Product");
        updateProduct.setDescription("Minimal description");
        updateProduct.setPrice(new BigDecimal("50.00"));
        updateProduct.setWeight(new BigDecimal("0.5"));
        // No brand, warranty, or categories set

        when(productRepository.save(any(Product.class))).thenReturn(existingProduct);

        // Act
        Product result = productService.update(productId, updateProduct);

        // Assert
        assertThat(result.getSku()).isEqualTo("MINIMAL-SKU");
        assertThat(result.getName()).isEqualTo("Minimal Product");
        assertThat(result.getBrand()).isEqualTo(originalBrand);
        assertThat(result.getWarranty()).isEqualTo(originalWarranty);
        assertThat(result.getCategories()).isEqualTo(originalCategories);
        verify(brandService, never()).getById(any(UUID.class));
        verify(warrantyService, never()).getById(any(UUID.class));
        verify(categoryService, never()).getById(any(UUID.class));
        verify(productRepository).save(existingProduct);
    }
}







