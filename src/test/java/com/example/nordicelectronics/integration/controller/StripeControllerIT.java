package com.example.nordicelectronics.integration.controller;

import com.example.nordicelectronics.controller.postgresql.StripeController;
import com.example.nordicelectronics.entity.*;
import com.example.nordicelectronics.integration.BaseIntegrationTest;
import com.example.nordicelectronics.repositories.sql.ProductRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.securityContext;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@Transactional
class StripeControllerIT extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private StripeController stripeController;

    private User testUser;
    private Product testProduct;
    private Address testAddress;
    private Warehouse testWarehouse;

    private static final String BASE_URL = "/api/postgresql/stripe";

    @BeforeEach
    void setUp() {
        // Create test brand
        Brand brand = Brand.builder()
                .name("Test Brand")
                .description("Test brand description")
                .build();
        entityManager.persist(brand);

        // Create test warranty
        Warranty warranty = Warranty.builder()
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusYears(1))
                .description("Test warranty")
                .build();
        entityManager.persist(warranty);

        // Create test product
        testProduct = Product.builder()
                .name("Test Laptop")
                .sku("SKU-TEST-" + UUID.randomUUID().toString().substring(0, 8))
                .description("Test laptop description")
                .price(new BigDecimal("999.99"))
                .weight(new BigDecimal("2.5"))
                .brand(brand)
                .warranty(warranty)
                .build();
        entityManager.persist(testProduct);

        // Create test user with address
        testUser = User.builder()
                .email("stripe-test@example.com")
                .firstName("Stripe")
                .lastName("Test")
                .phoneNumber("12345678")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .password("hashedPassword")
                .isAdmin(false)
                .build();
        entityManager.persist(testUser);

        // Create test address for user
        testAddress = Address.builder()
                .user(testUser)
                .street("Test Street")
                .streetNumber("123")
                .city("Test City")
                .zip("1000")
                .build();
        entityManager.persist(testAddress);

        // Link address to user
        testUser.setAddress(List.of(testAddress));
        
        // Create warehouse with its own address
        Address warehouseAddress = Address.builder()
                .user(testUser)
                .street("Warehouse Street")
                .streetNumber("456")
                .city("Warehouse City")
                .zip("2000")
                .build();
        entityManager.persist(warehouseAddress);

        testWarehouse = Warehouse.builder()
                .name("Test Warehouse")
                .phoneNumber("87654321")
                .address(warehouseAddress)
                .build();
        entityManager.persist(testWarehouse);

        // Create warehouse product with stock
        WarehouseProductKey warehouseProductKey = WarehouseProductKey.builder()
                .warehouseId(testWarehouse.getWarehouseId())
                .productId(testProduct.getProductId())
                .build();

        WarehouseProduct warehouseProduct = WarehouseProduct.builder()
                .id(warehouseProductKey)
                .warehouse(testWarehouse)
                .product(testProduct)
                .stockQuantity(50)
                .build();
        entityManager.persist(warehouseProduct);

        entityManager.flush();
        entityManager.clear();
    }

    // GET /config TESTS

    @Nested
    @DisplayName("GET /config - Stripe Configuration Status")
    class GetStripeConfigTests {

        @Test
        @DisplayName("Should return stripe configuration status when configured")
        void shouldReturnStripeConfigStatus() throws Exception {
            mockMvc.perform(get(BASE_URL + "/config"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.stripeConfigured").value(true))
                    .andExpect(jsonPath("$.source").value("properties"))
                    .andExpect(jsonPath("$.keyPrefix").isString());
        }

        @Test
        @DisplayName("Should return unconfigured status when stripe key is null")
        void shouldReturnUnconfiguredWhenStripeKeyNull() throws Exception {
            // Save original value
            String originalKey = (String) ReflectionTestUtils.getField(stripeController, "stripeSecretKey");
            
            try {
                // Set stripe key to null
                ReflectionTestUtils.setField(stripeController, "stripeSecretKey", null);
                
                mockMvc.perform(get(BASE_URL + "/config"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.stripeConfigured").value(false))
                        .andExpect(jsonPath("$.source").value("none"))
                        .andExpect(jsonPath("$.keyPrefix").value("none"));
            } finally {
                // Restore original value
                ReflectionTestUtils.setField(stripeController, "stripeSecretKey", originalKey);
            }
        }

        @Test
        @DisplayName("Should return unconfigured status when stripe key is blank")
        void shouldReturnUnconfiguredWhenStripeKeyBlank() throws Exception {
            // Save original value
            String originalKey = (String) ReflectionTestUtils.getField(stripeController, "stripeSecretKey");
            
            try {
                // Set stripe key to blank
                ReflectionTestUtils.setField(stripeController, "stripeSecretKey", "   ");
                
                mockMvc.perform(get(BASE_URL + "/config"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.stripeConfigured").value(false))
                        .andExpect(jsonPath("$.source").value("none"))
                        .andExpect(jsonPath("$.keyPrefix").value("none"));
            } finally {
                // Restore original value
                ReflectionTestUtils.setField(stripeController, "stripeSecretKey", originalKey);
            }
        }
    }

    // POST /checkout TESTS

    @Nested
    @DisplayName("POST /checkout - Create Checkout Session")
    class CreateCheckoutSessionTests {

        @Test
        @DisplayName("Should return 500 when stripe secret key is not configured")
        void shouldReturn500WhenStripeKeyNotConfigured() throws Exception {
            // Get actual stripeSecretKey
            String originalKey = (String) ReflectionTestUtils.getField(stripeController, "stripeSecretKey");

            try {
                // Set actual stripe key to null
                ReflectionTestUtils.setField(stripeController, "stripeSecretKey", null);

                Map<String, Object> payload = createValidCartPayload();
                SecurityContext securityContext = createSecurityContextForUser(testUser.getEmail());

                mockMvc.perform(post(BASE_URL + "/checkout")
                                .with(securityContext(securityContext))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(payload)))
                        .andExpect(status().isInternalServerError())
                        .andExpect(jsonPath("$.error").value("Stripe secret key not configured"));
            } finally {
                // Restore original value
                ReflectionTestUtils.setField(stripeController, "stripeSecretKey", originalKey);
            }
        }

        @Test
        @DisplayName("Should return 500 when stripe secret key is blank")
        void shouldReturn500WhenStripeKeyBlank() throws Exception {
            // Save original value
            String originalKey = (String) ReflectionTestUtils.getField(stripeController, "stripeSecretKey");
            
            try {
                // Set stripe key to blank
                ReflectionTestUtils.setField(stripeController, "stripeSecretKey", "");
                
                Map<String, Object> payload = createValidCartPayload();
                SecurityContext securityContext = createSecurityContextForUser(testUser.getEmail());

                mockMvc.perform(post(BASE_URL + "/checkout")
                                .with(securityContext(securityContext))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(payload)))
                        .andExpect(status().isInternalServerError())
                        .andExpect(jsonPath("$.error").value("Stripe secret key not configured"));
            } finally {
                // Restore original value
                ReflectionTestUtils.setField(stripeController, "stripeSecretKey", originalKey);
            }
        }

        @Test
        @DisplayName("Should return 400 when cart is empty")
        void shouldReturn400WhenCartEmpty() throws Exception {
            Map<String, Object> payload = new HashMap<>();
            payload.put("cart", Collections.emptyList());

            SecurityContext securityContext = createSecurityContextForUser(testUser.getEmail());

            mockMvc.perform(post(BASE_URL + "/checkout")
                            .with(securityContext(securityContext))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(payload)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Cart is empty"));
        }

        @Test
        @DisplayName("Should return 400 when cart is null")
        void shouldReturn400WhenCartNull() throws Exception {
            Map<String, Object> payload = new HashMap<>();
            // No cart in payload

            SecurityContext securityContext = createSecurityContextForUser(testUser.getEmail());

            mockMvc.perform(post(BASE_URL + "/checkout")
                            .with(securityContext(securityContext))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(payload)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Cart is empty"));
        }

    // ============================================
    // GET /orders TESTS
    // ============================================

    @Nested
    @DisplayName("GET /orders - Get User Orders")
    class GetUserOrdersTests {

        @Test
        @DisplayName("Should return 401 when user is not authenticated")
        @WithAnonymousUser
        void shouldReturn401WhenNotAuthenticated() throws Exception {
            mockMvc.perform(get(BASE_URL + "/orders"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$").isEmpty());
        }

        @Test
        @DisplayName("Should return empty list when user has no orders in session")
        void shouldReturnEmptyListWhenNoOrders() throws Exception {
            SecurityContext securityContext = createSecurityContextForUser(testUser.getEmail());

            mockMvc.perform(get(BASE_URL + "/orders")
                            .with(securityContext(securityContext)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$").isEmpty());
        }

        @Test
        @DisplayName("Should return orders filtered by user email from session")
        void shouldReturnOrdersFilteredByUserEmail() throws Exception {
            MockHttpSession session = new MockHttpSession();

            // Simulate stored orders in session
            List<Map<String, Object>> userOrders = new ArrayList<>();

            Map<String, Object> order1 = new HashMap<>();
            order1.put("orderId", UUID.randomUUID().toString());
            order1.put("userEmail", testUser.getEmail());
            order1.put("createdAt", LocalDateTime.now().toString());
            order1.put("status", "PENDING");
            userOrders.add(order1);

            Map<String, Object> order2 = new HashMap<>();
            order2.put("orderId", UUID.randomUUID().toString());
            order2.put("userEmail", "other@example.com"); // Different user
            order2.put("createdAt", LocalDateTime.now().minusHours(1).toString());
            order2.put("status", "COMPLETED");
            userOrders.add(order2);

            session.setAttribute("userOrders", userOrders);

            SecurityContext securityContext = createSecurityContextForUser(testUser.getEmail());

            mockMvc.perform(get(BASE_URL + "/orders")
                            .session(session)
                            .with(securityContext(securityContext)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].userEmail").value(testUser.getEmail()));
        }

        @Test
        @DisplayName("Should return orders sorted by createdAt descending")
        void shouldReturnOrdersSortedByCreatedAtDesc() throws Exception {
            MockHttpSession session = new MockHttpSession();

            List<Map<String, Object>> userOrders = new ArrayList<>();

            Map<String, Object> olderOrder = new HashMap<>();
            olderOrder.put("orderId", "older-order");
            olderOrder.put("userEmail", testUser.getEmail());
            olderOrder.put("createdAt", LocalDateTime.now().minusDays(1).toString());
            userOrders.add(olderOrder);

            Map<String, Object> newerOrder = new HashMap<>();
            newerOrder.put("orderId", "newer-order");
            newerOrder.put("userEmail", testUser.getEmail());
            newerOrder.put("createdAt", LocalDateTime.now().toString());
            userOrders.add(newerOrder);

            session.setAttribute("userOrders", userOrders);

            SecurityContext securityContext = createSecurityContextForUser(testUser.getEmail());

            mockMvc.perform(get(BASE_URL + "/orders")
                            .session(session)
                            .with(securityContext(securityContext)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[0].orderId").value("newer-order"))
                    .andExpect(jsonPath("$[1].orderId").value("older-order"));
        }
    }

    // HELPER METHODS

    private SecurityContext createSecurityContextForUser(String email) {
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        Authentication auth = new UsernamePasswordAuthenticationToken(
                email, "password",
                List.of(new SimpleGrantedAuthority("ROLE_USER")));
        securityContext.setAuthentication(auth);
        return securityContext;
    }

    private Map<String, Object> createValidCartPayload() {
        Map<String, Object> payload = new HashMap<>();
        List<Map<String, Object>> cart = new ArrayList<>();
        Map<String, Object> cartItem = new HashMap<>();
        cartItem.put("id", testProduct.getProductId().toString());
        cartItem.put("name", testProduct.getName());
        cartItem.put("price", testProduct.getPrice().doubleValue());
        cartItem.put("quantity", 1);
        cart.add(cartItem);
        payload.put("cart", cart);
        return payload;
    }

}
}
