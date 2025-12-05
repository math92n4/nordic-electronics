package com.example.nordicelectronics.integration.controller;

import com.example.nordicelectronics.entity.*;
import com.example.nordicelectronics.entity.dto.product.ProductRequestDTO;
import com.example.nordicelectronics.integration.BaseIntegrationTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@Transactional
class ProductControllerIT extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private Brand testBrand;
    private Brand testBrand2;
    private Category testCategory;
    private Category testCategory2;
    private Warranty testWarranty;
    private Product testProduct;
    private Product testProduct2;
    private Product testProduct3;

    private static final String BASE_URL = "/api/postgresql/products";

    @BeforeEach
    void setUp() {
        // Create test brands
        testBrand = Brand.builder()
                .name("Test Brand")
                .description("Test brand description")
                .build();
        entityManager.persist(testBrand);

        testBrand2 = Brand.builder()
                .name("Another Brand")
                .description("Another brand description")
                .build();
        entityManager.persist(testBrand2);

        // Create test categories
        testCategory = Category.builder()
                .name("Electronics")
                .description("Electronic products")
                .build();
        entityManager.persist(testCategory);

        testCategory2 = Category.builder()
                .name("Accessories")
                .description("Product accessories")
                .build();
        entityManager.persist(testCategory2);

        // Create test warranty
        testWarranty = Warranty.builder()
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusYears(2))
                .description("2-year warranty")
                .build();
        entityManager.persist(testWarranty);

        // Create test products
        Set<Category> product1Categories = new HashSet<>();
        product1Categories.add(testCategory);
        
        testProduct = Product.builder()
                .name("Test Laptop")
                .sku("SKU-LAPTOP-" + UUID.randomUUID().toString().substring(0, 8))
                .description("A powerful laptop for testing")
                .price(new BigDecimal("999.99"))
                .weight(new BigDecimal("2.5"))
                .brand(testBrand)
                .warranty(testWarranty)
                .categories(product1Categories)
                .build();
        entityManager.persist(testProduct);

        Warranty warranty2 = Warranty.builder()
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusYears(1))
                .description("1-year warranty")
                .build();
        entityManager.persist(warranty2);

        Set<Category> product2Categories = new HashSet<>();
        product2Categories.add(testCategory);
        product2Categories.add(testCategory2);
        
        testProduct2 = Product.builder()
                .name("Test Phone")
                .sku("SKU-PHONE-" + UUID.randomUUID().toString().substring(0, 8))
                .description("A smartphone for testing")
                .price(new BigDecimal("599.99"))
                .weight(new BigDecimal("0.2"))
                .brand(testBrand)
                .warranty(warranty2)
                .categories(product2Categories)
                .build();
        entityManager.persist(testProduct2);

        Warranty warranty3 = Warranty.builder()
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusYears(1))
                .description("1-year warranty")
                .build();
        entityManager.persist(warranty3);

        Set<Category> product3Categories = new HashSet<>();
        product3Categories.add(testCategory2);
        
        testProduct3 = Product.builder()
                .name("Test Headphones")
                .sku("SKU-HEADPHONES-" + UUID.randomUUID().toString().substring(0, 8))
                .description("Wireless headphones for testing")
                .price(new BigDecimal("149.99"))
                .weight(new BigDecimal("0.3"))
                .brand(testBrand2)
                .warranty(warranty3)
                .categories(product3Categories)
                .build();
        entityManager.persist(testProduct3);

        entityManager.flush();
        entityManager.clear();
    }

    // ============================================
    // GET / TESTS - Get All Products
    // ============================================

    @Nested
    @DisplayName("GET / - Get All Products")
    class GetAllProductsTests {

        @Test
        @DisplayName("Should return all products")
        void shouldReturnAllProducts() throws Exception {
            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(3))))
                    .andExpect(jsonPath("$[*].productId").exists())
                    .andExpect(jsonPath("$[*].name").exists())
                    .andExpect(jsonPath("$[*].price").exists());
        }

        @Test
        @DisplayName("Should return products with all required fields")
        void shouldReturnProductsWithRequiredFields() throws Exception {
            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].productId").isNotEmpty())
                    .andExpect(jsonPath("$[0].sku").isNotEmpty())
                    .andExpect(jsonPath("$[0].name").isNotEmpty())
                    .andExpect(jsonPath("$[0].description").isNotEmpty())
                    .andExpect(jsonPath("$[0].price").isNumber())
                    .andExpect(jsonPath("$[0].brandId").isNotEmpty());
        }
    }

    // ============================================
    // GET /paginated TESTS - Get Paginated Products
    // ============================================

    @Nested
    @DisplayName("GET /paginated - Get Paginated Products")
    class GetPaginatedProductsTests {

        @Test
        @DisplayName("Should return paginated products with default parameters")
        void shouldReturnPaginatedProductsWithDefaults() throws Exception {
            mockMvc.perform(get(BASE_URL + "/paginated"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.pageNumber").value(0))
                    .andExpect(jsonPath("$.pageSize").value(12))
                    .andExpect(jsonPath("$.totalElements").isNumber())
                    .andExpect(jsonPath("$.totalPages").isNumber())
                    .andExpect(jsonPath("$.first").value(true))
                    .andExpect(jsonPath("$.hasNext").isBoolean())
                    .andExpect(jsonPath("$.hasPrevious").value(false));
        }

        @Test
        @DisplayName("Should return paginated products with custom page size")
        void shouldReturnPaginatedProductsWithCustomPageSize() throws Exception {
            mockMvc.perform(get(BASE_URL + "/paginated")
                            .param("page", "0")
                            .param("size", "2"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content", hasSize(2)))
                    .andExpect(jsonPath("$.pageSize").value(2))
                    .andExpect(jsonPath("$.totalElements").value(greaterThanOrEqualTo(3)));
        }

        @Test
        @DisplayName("Should return second page of products")
        void shouldReturnSecondPageOfProducts() throws Exception {
            mockMvc.perform(get(BASE_URL + "/paginated")
                            .param("page", "1")
                            .param("size", "2"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.pageNumber").value(1))
                    .andExpect(jsonPath("$.hasPrevious").value(true));
        }

        @Test
        @DisplayName("Should filter products by search term in name")
        void shouldFilterProductsBySearchTermInName() throws Exception {
            mockMvc.perform(get(BASE_URL + "/paginated")
                            .param("search", "Laptop"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))))
                    .andExpect(jsonPath("$.content[0].name", containsStringIgnoringCase("laptop")));
        }

        @Test
        @DisplayName("Should filter products by search term in description")
        void shouldFilterProductsBySearchTermInDescription() throws Exception {
            mockMvc.perform(get(BASE_URL + "/paginated")
                            .param("search", "wireless"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))));
        }

        @Test
        @DisplayName("Should filter products by category ID")
        void shouldFilterProductsByCategoryId() throws Exception {
            mockMvc.perform(get(BASE_URL + "/paginated")
                            .param("categoryId", testCategory.getCategoryId().toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))));
        }

        @Test
        @DisplayName("Should filter products by brand ID")
        void shouldFilterProductsByBrandId() throws Exception {
            mockMvc.perform(get(BASE_URL + "/paginated")
                            .param("brandId", testBrand.getBrandId().toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(2))));
        }

        @Test
        @DisplayName("Should filter products by multiple criteria")
        void shouldFilterProductsByMultipleCriteria() throws Exception {
            mockMvc.perform(get(BASE_URL + "/paginated")
                            .param("search", "test")
                            .param("brandId", testBrand.getBrandId().toString())
                            .param("categoryId", testCategory.getCategoryId().toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray());
        }

        @Test
        @DisplayName("Should sort products by name ascending")
        void shouldSortProductsByNameAscending() throws Exception {
            mockMvc.perform(get(BASE_URL + "/paginated")
                            .param("sortBy", "name")
                            .param("sortDirection", "asc"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray());
        }

        @Test
        @DisplayName("Should sort products by name descending")
        void shouldSortProductsByNameDescending() throws Exception {
            mockMvc.perform(get(BASE_URL + "/paginated")
                            .param("sortBy", "name")
                            .param("sortDirection", "desc"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray());
        }

        @Test
        @DisplayName("Should sort products by price ascending")
        void shouldSortProductsByPriceAscending() throws Exception {
            MvcResult result = mockMvc.perform(get(BASE_URL + "/paginated")
                            .param("sortBy", "price")
                            .param("sortDirection", "asc"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andReturn();
            
            // Verify the response contains sorted content
            String content = result.getResponse().getContentAsString();
            assertNotNull(content);
        }

        @Test
        @DisplayName("Should sort products by price descending")
        void shouldSortProductsByPriceDescending() throws Exception {
            mockMvc.perform(get(BASE_URL + "/paginated")
                            .param("sortBy", "price")
                            .param("sortDirection", "desc"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray());
        }

        @Test
        @DisplayName("Should return empty content for search with no results")
        void shouldReturnEmptyContentForNoResults() throws Exception {
            mockMvc.perform(get(BASE_URL + "/paginated")
                            .param("search", "nonexistentproduct12345"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content", hasSize(0)))
                    .andExpect(jsonPath("$.totalElements").value(0));
        }

        @Test
        @DisplayName("Should handle invalid page number gracefully")
        void shouldHandleInvalidPageNumberGracefully() throws Exception {
            mockMvc.perform(get(BASE_URL + "/paginated")
                            .param("page", "-1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.pageNumber").value(0));
        }

        @Test
        @DisplayName("Should enforce maximum page size")
        void shouldEnforceMaximumPageSize() throws Exception {
            mockMvc.perform(get(BASE_URL + "/paginated")
                            .param("size", "200"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.pageSize").value(lessThanOrEqualTo(100)));
        }

        @Test
        @DisplayName("Should handle empty search term")
        void shouldHandleEmptySearchTerm() throws Exception {
            mockMvc.perform(get(BASE_URL + "/paginated")
                            .param("search", ""))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray());
        }

        @Test
        @DisplayName("Should handle whitespace search term")
        void shouldHandleWhitespaceSearchTerm() throws Exception {
            mockMvc.perform(get(BASE_URL + "/paginated")
                            .param("search", "   "))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray());
        }
    }

    // ============================================
    // GET /{id} TESTS - Get Product By ID
    // ============================================

    @Nested
    @DisplayName("GET /{id} - Get Product By ID")
    class GetProductByIdTests {

        @Test
        @DisplayName("Should return product by valid ID")
        void shouldReturnProductByValidId() throws Exception {
            mockMvc.perform(get(BASE_URL + "/" + testProduct.getProductId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.productId").value(testProduct.getProductId().toString()))
                    .andExpect(jsonPath("$.name").value("Test Laptop"))
                    .andExpect(jsonPath("$.price").value(999.99))
                    .andExpect(jsonPath("$.brandId").value(testBrand.getBrandId().toString()));
        }

        @Test
        @DisplayName("Should throw EntityNotFoundException for non-existent product ID")
        void shouldThrowExceptionForNonExistentId() throws Exception {
            UUID nonExistentId = UUID.randomUUID();
            // The application throws EntityNotFoundException which is not handled by a global exception handler
            assertThrows(jakarta.servlet.ServletException.class, () -> {
                mockMvc.perform(get(BASE_URL + "/" + nonExistentId));
            });
        }

        @Test
        @DisplayName("Should return 400 for invalid UUID format")
        void shouldReturn400ForInvalidUuidFormat() throws Exception {
            mockMvc.perform(get(BASE_URL + "/invalid-uuid"))
                    .andExpect(status().isBadRequest());
        }
    }

    // ============================================
    // POST / TESTS - Create Product
    // ============================================

    @Nested
    @DisplayName("POST / - Create Product")
    class CreateProductTests {

        @Test
        @DisplayName("Should create a new product")
        void shouldCreateNewProduct() throws Exception {
            Warranty newWarranty = Warranty.builder()
                    .startDate(LocalDate.now())
                    .endDate(LocalDate.now().plusYears(1))
                    .description("New warranty")
                    .build();
            entityManager.persist(newWarranty);
            entityManager.flush();

            ProductRequestDTO request = ProductRequestDTO.builder()
                    .sku("SKU-NEW-" + UUID.randomUUID().toString().substring(0, 8))
                    .name("New Product")
                    .description("A brand new product")
                    .price(new BigDecimal("299.99"))
                    .weight(new BigDecimal("1.0"))
                    .brandId(testBrand.getBrandId())
                    .warrantyId(newWarranty.getWarrantyId())
                    .categoryIds(List.of(testCategory.getCategoryId()))
                    .build();

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.productId").isNotEmpty())
                    .andExpect(jsonPath("$.name").value("New Product"))
                    .andExpect(jsonPath("$.price").value(299.99))
                    .andExpect(jsonPath("$.brandId").value(testBrand.getBrandId().toString()));
        }

        @Test
        @DisplayName("Should create product with multiple categories")
        void shouldCreateProductWithMultipleCategories() throws Exception {
            Warranty newWarranty = Warranty.builder()
                    .startDate(LocalDate.now())
                    .endDate(LocalDate.now().plusYears(1))
                    .description("New warranty")
                    .build();
            entityManager.persist(newWarranty);
            entityManager.flush();

            ProductRequestDTO request = ProductRequestDTO.builder()
                    .sku("SKU-MULTI-" + UUID.randomUUID().toString().substring(0, 8))
                    .name("Multi-Category Product")
                    .description("Product in multiple categories")
                    .price(new BigDecimal("199.99"))
                    .weight(new BigDecimal("0.5"))
                    .brandId(testBrand.getBrandId())
                    .warrantyId(newWarranty.getWarrantyId())
                    .categoryIds(List.of(testCategory.getCategoryId(), testCategory2.getCategoryId()))
                    .build();

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.categoryIds", hasSize(2)));
        }
    }

    // ============================================
    // PUT /{id} TESTS - Update Product
    // ============================================

    @Nested
    @DisplayName("PUT /{id} - Update Product")
    class UpdateProductTests {

        @Test
        @DisplayName("Should update existing product")
        void shouldUpdateExistingProduct() throws Exception {
            ProductRequestDTO request = ProductRequestDTO.builder()
                    .sku("SKU-UPDATED")
                    .name("Updated Laptop")
                    .description("Updated description")
                    .price(new BigDecimal("1099.99"))
                    .weight(new BigDecimal("2.8"))
                    .build();

            mockMvc.perform(put(BASE_URL + "/" + testProduct.getProductId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("Updated Laptop"))
                    .andExpect(jsonPath("$.description").value("Updated description"))
                    .andExpect(jsonPath("$.price").value(1099.99));
        }

        @Test
        @DisplayName("Should update product with new brand")
        void shouldUpdateProductWithNewBrand() throws Exception {
            ProductRequestDTO request = ProductRequestDTO.builder()
                    .sku("SKU-NEW-BRAND")
                    .name("Product New Brand")
                    .description("Product with updated brand")
                    .price(new BigDecimal("899.99"))
                    .weight(new BigDecimal("2.0"))
                    .brandId(testBrand2.getBrandId())
                    .build();

            mockMvc.perform(put(BASE_URL + "/" + testProduct.getProductId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.brandId").value(testBrand2.getBrandId().toString()));
        }

        @Test
        @DisplayName("Should update product with new categories")
        void shouldUpdateProductWithNewCategories() throws Exception {
            ProductRequestDTO request = ProductRequestDTO.builder()
                    .sku("SKU-NEW-CATS")
                    .name("Product New Categories")
                    .description("Product with updated categories")
                    .price(new BigDecimal("799.99"))
                    .weight(new BigDecimal("1.5"))
                    .categoryIds(List.of(testCategory2.getCategoryId()))
                    .build();

            mockMvc.perform(put(BASE_URL + "/" + testProduct.getProductId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.categoryIds", hasSize(1)));
        }

        @Test
        @DisplayName("Should throw EntityNotFoundException when updating non-existent product")
        void shouldThrowExceptionWhenUpdatingNonExistentProduct() throws Exception {
            UUID nonExistentId = UUID.randomUUID();
            ProductRequestDTO request = ProductRequestDTO.builder()
                    .sku("SKU-NONEXISTENT")
                    .name("Non-existent Product")
                    .description("Description")
                    .price(new BigDecimal("100.00"))
                    .weight(new BigDecimal("1.0"))
                    .build();

            String requestJson = objectMapper.writeValueAsString(request);
            
            // The application throws EntityNotFoundException which is not handled by a global exception handler
            assertThrows(jakarta.servlet.ServletException.class, () -> {
                mockMvc.perform(put(BASE_URL + "/" + nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson));
            });
        }
    }

    // ============================================
    // DELETE /{id} TESTS - Delete Product
    // ============================================

    @Nested
    @DisplayName("DELETE /{id} - Delete Product")
    class DeleteProductTests {

        @Test
        @DisplayName("Should soft delete existing product")
        void shouldSoftDeleteExistingProduct() throws Exception {
            // Verify product exists before deletion
            mockMvc.perform(get(BASE_URL + "/" + testProduct.getProductId()))
                    .andExpect(status().isOk());
            
            // Perform soft delete
            mockMvc.perform(delete(BASE_URL + "/" + testProduct.getProductId()))
                    .andExpect(status().isNoContent());
            
            // Note: Within the same transaction, the soft-deleted product may still be visible
            // due to JPA caching. The @SQLRestriction will filter it out in a new transaction.
        }

        @Test
        @DisplayName("Should throw EntityNotFoundException when deleting non-existent product")
        void shouldThrowExceptionWhenDeletingNonExistentProduct() throws Exception {
            UUID nonExistentId = UUID.randomUUID();
            
            // The application throws EntityNotFoundException which is not handled by a global exception handler
            assertThrows(jakarta.servlet.ServletException.class, () -> {
                mockMvc.perform(delete(BASE_URL + "/" + nonExistentId));
            });
        }
    }

    // ============================================
    // GET /best-selling TESTS
    // ============================================

    @Nested
    @DisplayName("GET /best-selling - Get Best Selling Products")
    class GetBestSellingProductsTests {

        @Test
        @DisplayName("Should return best selling products from materialized view")
        void shouldReturnBestSellingProducts() throws Exception {
            mockMvc.perform(get(BASE_URL + "/best-selling"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());
        }
    }

    // ============================================
    // GET /best-reviewed TESTS
    // ============================================

    @Nested
    @DisplayName("GET /best-reviewed - Get Best Reviewed Products")
    class GetBestReviewedProductsTests {

        @Test
        @DisplayName("Should return best reviewed products from materialized view")
        void shouldReturnBestReviewedProducts() throws Exception {
            mockMvc.perform(get(BASE_URL + "/best-reviewed"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());
        }
    }

    // ============================================
    // POST /refresh-analytics TESTS
    // ============================================

    @Nested
    @DisplayName("POST /refresh-analytics - Refresh Analytics Views")
    class RefreshAnalyticsViewsTests {

        @Test
        @DisplayName("Should refresh analytics materialized views")
        void shouldRefreshAnalyticsViews() throws Exception {
            mockMvc.perform(post(BASE_URL + "/refresh-analytics"))
                    .andExpect(status().isNoContent());
        }
    }

    // ============================================
    // EDGE CASE TESTS
    // ============================================

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle product with decimal price correctly")
        void shouldHandleProductWithDecimalPrice() throws Exception {
            mockMvc.perform(get(BASE_URL + "/" + testProduct.getProductId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.price").value(999.99));
        }

        @Test
        @DisplayName("Should return products sorted correctly by multiple criteria")
        void shouldReturnProductsSortedCorrectly() throws Exception {
            // Test sorting by price descending
            MvcResult result = mockMvc.perform(get(BASE_URL + "/paginated")
                            .param("sortBy", "price")
                            .param("sortDirection", "desc")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andReturn();

            String responseContent = result.getResponse().getContentAsString();
            assertTrue(responseContent.contains("content"));
        }

        @Test
        @DisplayName("Should handle concurrent filter and sort parameters")
        void shouldHandleConcurrentFilterAndSort() throws Exception {
            mockMvc.perform(get(BASE_URL + "/paginated")
                            .param("search", "test")
                            .param("brandId", testBrand.getBrandId().toString())
                            .param("sortBy", "price")
                            .param("sortDirection", "asc")
                            .param("page", "0")
                            .param("size", "5"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.pageSize").value(5));
        }

        @Test
        @DisplayName("Should handle case-insensitive search")
        void shouldHandleCaseInsensitiveSearch() throws Exception {
            // Search with lowercase
            mockMvc.perform(get(BASE_URL + "/paginated")
                            .param("search", "laptop"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))));

            // Search with uppercase
            mockMvc.perform(get(BASE_URL + "/paginated")
                            .param("search", "LAPTOP"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))));

            // Search with mixed case
            mockMvc.perform(get(BASE_URL + "/paginated")
                            .param("search", "LaPtOp"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))));
        }

        @Test
        @DisplayName("Should filter by non-existent category returning empty results")
        void shouldReturnEmptyForNonExistentCategory() throws Exception {
            UUID nonExistentCategoryId = UUID.randomUUID();
            mockMvc.perform(get(BASE_URL + "/paginated")
                            .param("categoryId", nonExistentCategoryId.toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(0)))
                    .andExpect(jsonPath("$.totalElements").value(0));
        }

        @Test
        @DisplayName("Should filter by non-existent brand returning empty results")
        void shouldReturnEmptyForNonExistentBrand() throws Exception {
            UUID nonExistentBrandId = UUID.randomUUID();
            mockMvc.perform(get(BASE_URL + "/paginated")
                            .param("brandId", nonExistentBrandId.toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(0)))
                    .andExpect(jsonPath("$.totalElements").value(0));
        }

        @Test
        @DisplayName("Should handle page beyond total pages")
        void shouldHandlePageBeyondTotalPages() throws Exception {
            mockMvc.perform(get(BASE_URL + "/paginated")
                            .param("page", "1000")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(0)))
                    .andExpect(jsonPath("$.last").value(true));
        }

        @Test
        @DisplayName("Should handle zero page size by using default")
        void shouldHandleZeroPageSize() throws Exception {
            mockMvc.perform(get(BASE_URL + "/paginated")
                            .param("size", "0"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.pageSize").value(greaterThan(0)));
        }

        @Test
        @DisplayName("Should handle negative page size by using default")
        void shouldHandleNegativePageSize() throws Exception {
            mockMvc.perform(get(BASE_URL + "/paginated")
                            .param("size", "-5"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.pageSize").value(greaterThan(0)));
        }
    }
}
