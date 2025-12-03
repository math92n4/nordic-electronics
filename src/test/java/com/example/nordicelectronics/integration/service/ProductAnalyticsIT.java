package com.example.nordicelectronics.integration.service;

import com.example.nordicelectronics.entity.*;
import com.example.nordicelectronics.entity.enums.OrderStatus;
import com.example.nordicelectronics.integration.BaseIntegrationTest;
import com.example.nordicelectronics.service.ProductService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration tests for ProductService analytics methods
 * Tests materialized views and database functions
 */
class ProductAnalyticsIT extends BaseIntegrationTest {

    @Autowired
    private ProductService productService;

    /**
     * BLACK-BOX TEST: Equivalence Partitioning
     * Partition: Orders within last month (valid for best-selling calculation)
     * Expected: Products appear in best-selling list
     */
    @Test
    @DisplayName("Should return best-selling products with recent orders")
    void testGetBestSellingProducts_WithRecentOrders() {
        // Arrange - Create products
        Product product1 = createAndPersistProduct("Laptop", new BigDecimal("999.99"));
        Product product2 = createAndPersistProduct("Mouse", new BigDecimal("29.99"));
        Product product3 = createAndPersistProduct("Keyboard", new BigDecimal("79.99"));

        // Create user
        User user = createAndPersistUser("test@example.com");

        // Create orders within last month (these SHOULD appear in view)
        Order order1 = createAndPersistOrder(user, LocalDateTime.now().minusDays(5));
        addOrderProduct(order1, product1, 10); // Laptop: 10 units

        Order order2 = createAndPersistOrder(user, LocalDateTime.now().minusDays(15));
        addOrderProduct(order2, product1, 5);  // Laptop: 5 more units (15 total)
        addOrderProduct(order2, product2, 20); // Mouse: 20 units

        Order order3 = createAndPersistOrder(user, LocalDateTime.now().minusDays(25));
        addOrderProduct(order3, product3, 8);  // Keyboard: 8 units

        entityManager.flush();

        // Refresh materialized views to reflect new data
        productService.refreshAnalyticsViews();

        // Act
        List<Map<String, Object>> results = productService.getBestSellingProducts();

        // Assert
        assertThat(results).isNotEmpty();

        // Verify structure of results
        Map<String, Object> firstProduct = results.get(0);
        assertThat(firstProduct).containsKeys(
                "product_id",
                "product_name",
                "product_price",
                "total_units_sold",
                "revenue_generated"
        );

        // Verify products appear in order by units sold (descending)
        // Mouse should be first (20 units), then Laptop (15 units), then Keyboard (8 units)
        assertThat(results.get(0).get("product_name")).isEqualTo("Mouse");
        assertThat(results.get(0).get("total_units_sold")).isEqualTo(20L);

        assertThat(results.get(1).get("product_name")).isEqualTo("Laptop");
        assertThat(results.get(1).get("total_units_sold")).isEqualTo(15L);

        assertThat(results.get(2).get("product_name")).isEqualTo("Keyboard");
        assertThat(results.get(2).get("total_units_sold")).isEqualTo(8L);
    }

    /**
     * BLACK-BOX TEST: Equivalence Partitioning
     * Partition: Soft-deleted orders (should be excluded)
     * Expected: Deleted orders don't affect best-selling calculations
     */
    @Test
    @DisplayName("Should exclude soft-deleted orders from best-selling")
    void testGetBestSellingProducts_ExcludesDeletedOrders() {
        // Arrange
        Product product = createAndPersistProduct("Product", new BigDecimal("75.00"));
        User user = createAndPersistUser("deleted@example.com");

        // Create active order
        Order activeOrder = createAndPersistOrder(user, LocalDateTime.now().minusDays(5));
        addOrderProduct(activeOrder, product, 10);

        // Create deleted order (should be excluded)
        Order deletedOrder = createAndPersistOrder(user, LocalDateTime.now().minusDays(10));
        deletedOrder.setDeletedAt(LocalDateTime.now());
        addOrderProduct(deletedOrder, product, 50); // High quantity but deleted

        entityManager.flush();
        productService.refreshAnalyticsViews();

        // Act
        List<Map<String, Object>> results = productService.getBestSellingProducts();

        // Assert - only active order should count
        Map<String, Object> result = results.stream()
                .filter(map -> map.get("product_name").equals("Product"))
                .findFirst()
                .orElseThrow();

        assertThat(result.get("total_units_sold")).isEqualTo(10L); // Not 60
    }

    /**
     * BLACK-BOX TEST: Equivalence Partitioning
     * Partition: No orders (empty result set)
     * Expected: Returns empty list
     */
    @Test
    @DisplayName("Should return empty list when no orders exist")
    void testGetBestSellingProducts_NoOrders() {
        // Arrange - create products but no orders
        createAndPersistProduct("Product 1", new BigDecimal("100.00"));
        createAndPersistProduct("Product 2", new BigDecimal("200.00"));
        entityManager.flush();

        productService.refreshAnalyticsViews();

        // Act
        List<Map<String, Object>> results = productService.getBestSellingProducts();

        // Assert
        assertThat(results).isEmpty();
    }

    /**
     * BLACK-BOX TEST: Boundary Value Analysis
     * Boundary: Exactly 10 products (view LIMIT)
     * Expected: Returns maximum 10 products
     */
    @Test
    @DisplayName("Should limit results to 10 best-selling products")
    void testGetBestSellingProducts_LimitsTen() {
        // Arrange - Create 15 products with orders
        User user = createAndPersistUser("limit@example.com");

        for (int i = 1; i <= 15; i++) {
            Product product = createAndPersistProduct("Product " + i, new BigDecimal("100.00"));
            Order order = createAndPersistOrder(user, LocalDateTime.now().minusDays(1));
            addOrderProduct(order, product, i); // Different quantities for ordering
        }

        entityManager.flush();
        productService.refreshAnalyticsViews();

        // Act
        List<Map<String, Object>> results = productService.getBestSellingProducts();

        // Assert - should return exactly 10 (LIMIT 10 in view)
        assertThat(results).hasSize(10);

        // Verify it's the top 10 by units sold (15 down to 6)
        assertThat(results.get(0).get("total_units_sold")).isEqualTo(15L);
        assertThat(results.get(9).get("total_units_sold")).isEqualTo(6L);
    }

    // ============================================
    // BEST REVIEWED PRODUCTS TESTS
    // ============================================

    /**
     * BLACK-BOX TEST: Equivalence Partitioning
     * Partition: Products with reviews (valid for best-reviewed calculation)
     * Expected: Products appear in best-reviewed list ordered by rating
     */
    @Test
    @DisplayName("Should return best-reviewed products ordered by average rating")
    void testGetBestReviewedProducts_WithReviews() {
        // Arrange
        Product product1 = createAndPersistProduct("Excellent Product", new BigDecimal("999.99"));
        Product product2 = createAndPersistProduct("Good Product", new BigDecimal("499.99"));
        Product product3 = createAndPersistProduct("Average Product", new BigDecimal("299.99"));

        User user = createAndPersistUser("reviewer@example.com");

        // Product 1: 5-star reviews (average 5.0)
        createAndPersistReview(product1, user, 5, "Perfect!");
        createAndPersistReview(product1, user, 5, "Amazing!");

        // Product 2: 4-star reviews (average 4.0)
        createAndPersistReview(product2, user, 4, "Good");
        createAndPersistReview(product2, user, 4, "Nice");

        // Product 3: Mixed reviews (average 3.0)
        createAndPersistReview(product3, user, 3, "OK");
        createAndPersistReview(product3, user, 3, "Average");

        entityManager.flush();
        productService.refreshAnalyticsViews();

        // Act
        List<Map<String, Object>> results = productService.getBestReviewedProducts();

        // Assert
        assertThat(results).hasSize(3);

        // Verify ordered by rating descending
        assertThat(results.get(0).get("product_name")).isEqualTo("Excellent Product");
        assertThat(results.get(0).get("average_rating")).isEqualTo(new BigDecimal("5.00"));
        assertThat(results.get(0).get("number_of_reviews")).isEqualTo(2L);

        assertThat(results.get(1).get("product_name")).isEqualTo("Good Product");
        assertThat(results.get(1).get("average_rating")).isEqualTo(new BigDecimal("4.00"));

        assertThat(results.get(2).get("product_name")).isEqualTo("Average Product");
        assertThat(results.get(2).get("average_rating")).isEqualTo(new BigDecimal("3.00"));
    }

    /**
     * BLACK-BOX TEST: Equivalence Partitioning
     * Partition: Products with no reviews (excluded by HAVING clause)
     * Expected: Products do NOT appear in list
     */
    @Test
    @DisplayName("Should exclude products with no reviews")
    void testGetBestReviewedProducts_NoReviews() {
        // Arrange - product without reviews
        createAndPersistProduct("No Reviews Product", new BigDecimal("100.00"));

        entityManager.flush();
        productService.refreshAnalyticsViews();

        // Act
        List<Map<String, Object>> results = productService.getBestReviewedProducts();

        // Assert - should not appear
        assertThat(results)
                .extracting(map -> map.get("product_name"))
                .doesNotContain("No Reviews Product");
    }

    /**
     * BLACK-BOX TEST: Decision Table
     * Condition: Same average rating but different review counts
     * Expected: Product with more reviews appears first
     */
    @Test
    @DisplayName("Should order by review count when ratings are equal")
    void testGetBestReviewedProducts_TieBreaker() {
        // Arrange
        Product product1 = createAndPersistProduct("Many Reviews", new BigDecimal("100.00"));
        Product product2 = createAndPersistProduct("Few Reviews", new BigDecimal("100.00"));
        User user = createAndPersistUser("tiebreak@example.com");

        // Both have 4.0 average rating, but different counts
        createAndPersistReview(product1, user, 4, "Review 1");
        createAndPersistReview(product1, user, 4, "Review 2");
        createAndPersistReview(product1, user, 4, "Review 3");
        createAndPersistReview(product1, user, 4, "Review 4");
        createAndPersistReview(product1, user, 4, "Review 5"); // 5 reviews

        createAndPersistReview(product2, user, 4, "Review A");
        createAndPersistReview(product2, user, 4, "Review B"); // 2 reviews

        entityManager.flush();
        productService.refreshAnalyticsViews();

        // Act
        List<Map<String, Object>> results = productService.getBestReviewedProducts();

        // Assert - "Many Reviews" should appear first (tie-breaker by count)
        assertThat(results.get(0).get("product_name")).isEqualTo("Many Reviews");
        assertThat(results.get(0).get("number_of_reviews")).isEqualTo(5L);

        assertThat(results.get(1).get("product_name")).isEqualTo("Few Reviews");
        assertThat(results.get(1).get("number_of_reviews")).isEqualTo(2L);
    }

    /**
     * BLACK-BOX TEST: Equivalence Partitioning
     * Partition: Soft-deleted reviews (should be excluded)
     * Expected: Deleted reviews don't affect ratings
     */
    @Test
    @DisplayName("Should exclude soft-deleted reviews from ratings")
    void testGetBestReviewedProducts_ExcludesDeletedReviews() {
        // Arrange
        Product product = createAndPersistProduct("Product", new BigDecimal("100.00"));
        User user = createAndPersistUser("deleted-review@example.com");

        // Active reviews
        createAndPersistReview(product, user, 5, "Good");
        createAndPersistReview(product, user, 5, "Great");

        // Deleted review (should be excluded)
        Review deletedReview = createAndPersistReview(product, user, 1, "Bad");
        deletedReview.setDeletedAt(LocalDateTime.now());

        entityManager.flush();
        productService.refreshAnalyticsViews();

        // Act
        List<Map<String, Object>> results = productService.getBestReviewedProducts();

        // Assert - average should be 5.0 (not affected by deleted 1-star review)
        Map<String, Object> result = results.stream()
                .filter(map -> map.get("product_name").equals("Product"))
                .findFirst()
                .orElseThrow();

        assertThat(result.get("average_rating")).isEqualTo(new BigDecimal("5.00"));
        assertThat(result.get("number_of_reviews")).isEqualTo(2L); // Only active reviews
    }

    /**
     * WHITE-BOX TEST: Tests the database function call
     * Verifies fn_refresh_materialized_views() executes without errors
     */
    @Test
    @DisplayName("Should successfully refresh materialized views")
    void testRefreshAnalyticsViews() {
        // Arrange - create some data
        Product product = createAndPersistProduct("Test", new BigDecimal("100.00"));
        User user = createAndPersistUser("refresh@example.com");
        Order order = createAndPersistOrder(user, LocalDateTime.now());
        addOrderProduct(order, product, 5);

        entityManager.flush();

        // Act & Assert - should not throw exception
        assertThatCode(() -> productService.refreshAnalyticsViews())
                .doesNotThrowAnyException();
    }

    // ============================================
    // HELPER METHODS (Create and Persist Entities)
    // ============================================

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

    private User createAndPersistUser(String email) {
        User user = User.builder()
                .email(email)
                .firstName("Test")
                .lastName("User")
                .phoneNumber("12345678")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .password("hashedPassword")
                .isAdmin(false)
                .build();

        entityManager.persist(user);
        return user;
    }

    private Order createAndPersistOrder(User user, LocalDateTime orderDate) {
        Address address = Address.builder()
                .user(user)
                .street("Test Street")
                .streetNumber("123")
                .city("Test City")
                .zip("1000")
                .build();
        entityManager.persist(address);

        Order order = Order.builder()
                .user(user)
                .address(address)
                .orderDate(orderDate)
                .subtotal(new BigDecimal("100.00"))
                .taxAmount(new BigDecimal("25.00"))
                .shippingCost(new BigDecimal("10.00"))
                .discountAmount(BigDecimal.ZERO)
                .totalAmount(new BigDecimal("135.00"))
                .orderStatus(OrderStatus.confirmed)
                .build();

        entityManager.persist(order);
        return order;
    }

    private void addOrderProduct(Order order, Product product, int quantity) {
        OrderProductKey key = new OrderProductKey();
        key.setOrderId(order.getOrderId());
        key.setProductId(product.getProductId());

        OrderProduct orderProduct = OrderProduct.builder()
                .id(key)
                .order(order)
                .product(product)
                .quantity(quantity)
                .unitPrice(product.getPrice())
                .totalPrice(product.getPrice().multiply(new BigDecimal(quantity)))
                .build();

        entityManager.persist(orderProduct);
    }

    private Review createAndPersistReview(Product product, User user, int rating, String comment) {
        Order dummyOrder = createAndPersistOrder(user, LocalDateTime.now());

        Review review = Review.builder()
                .product(product)
                .user(user)
                .orderId(dummyOrder.getOrderId())
                .reviewValue(rating)
                .title("Test Review")
                .comment(comment)
                .isVerifiedPurchase(true)
                .build();

        entityManager.persist(review);
        return review;
    }
}