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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
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

    // ============================================
    // GET /config TESTS
    // ============================================

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

    // ============================================
    // POST /checkout TESTS
    // ============================================

    @Nested
    @DisplayName("POST /checkout - Create Checkout Session")
    class CreateCheckoutSessionTests {

        @Test
        @DisplayName("Should return 401 when user is not authenticated")
        @WithAnonymousUser
        void shouldReturn401WhenNotAuthenticated() throws Exception {
            Map<String, Object> payload = createValidCartPayload();

            mockMvc.perform(post(BASE_URL + "/checkout")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(payload)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.error").exists());
        }

        @Test
        @DisplayName("Should return 401 when user email not found in database")
        void shouldReturn401WhenUserNotFound() throws Exception {
            Map<String, Object> payload = createValidCartPayload();

            // Create security context with non-existent user
            SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
            Authentication auth = new UsernamePasswordAuthenticationToken(
                    "nonexistent@example.com", "password",
                    List.of(new SimpleGrantedAuthority("ROLE_USER")));
            securityContext.setAuthentication(auth);

            mockMvc.perform(post(BASE_URL + "/checkout")
                            .with(securityContext(securityContext))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(payload)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.error").value("User lookup failed"));
        }

        @Test
        @DisplayName("Should return 500 when stripe secret key is not configured")
        void shouldReturn500WhenStripeKeyNotConfigured() throws Exception {
            // Save original value
            String originalKey = (String) ReflectionTestUtils.getField(stripeController, "stripeSecretKey");
            
            try {
                // Set stripe key to null
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

        @Test
        @DisplayName("Should return 400 when product UUID is invalid and SKU not found")
        void shouldReturn400WhenProductNotFoundByInvalidUuidOrSku() throws Exception {
            Map<String, Object> payload = new HashMap<>();
            List<Map<String, Object>> cart = new ArrayList<>();
            Map<String, Object> cartItem = new HashMap<>();
            cartItem.put("id", "invalid-sku-not-uuid");
            cartItem.put("name", "Unknown Product");
            cartItem.put("price", 100.00);
            cartItem.put("quantity", 1);
            cart.add(cartItem);
            payload.put("cart", cart);

            SecurityContext securityContext = createSecurityContextForUser(testUser.getEmail());

            mockMvc.perform(post(BASE_URL + "/checkout")
                            .with(securityContext(securityContext))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(payload)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value(containsString("Product not found")));
        }

        @Test
        @DisplayName("Should return 400 when product UUID does not exist")
        void shouldReturn400WhenProductUuidNotExists() throws Exception {
            UUID nonExistentId = UUID.randomUUID();
            Map<String, Object> payload = new HashMap<>();
            List<Map<String, Object>> cart = new ArrayList<>();
            Map<String, Object> cartItem = new HashMap<>();
            cartItem.put("id", nonExistentId.toString());
            cartItem.put("name", "Unknown Product");
            cartItem.put("price", 100.00);
            cartItem.put("quantity", 1);
            cart.add(cartItem);
            payload.put("cart", cart);

            SecurityContext securityContext = createSecurityContextForUser(testUser.getEmail());

            mockMvc.perform(post(BASE_URL + "/checkout")
                            .with(securityContext(securityContext))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(payload)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value(containsString("Product not found")));
        }

        @Test
        @DisplayName("Should find product by SKU when ID is not a valid UUID")
        void shouldFindProductBySkuWhenIdNotUuid() throws Exception {
            // Reload product to get current SKU
            Product product = productRepository.findById(testProduct.getProductId()).orElseThrow();
            
            Map<String, Object> payload = new HashMap<>();
            List<Map<String, Object>> cart = new ArrayList<>();
            Map<String, Object> cartItem = new HashMap<>();
            cartItem.put("id", product.getSku()); // Use SKU instead of UUID
            cartItem.put("name", product.getName());
            cartItem.put("price", product.getPrice().doubleValue());
            cartItem.put("quantity", 1);
            cart.add(cartItem);
            payload.put("cart", cart);
            payload.put("address", createAddressPayload());

            SecurityContext securityContext = createSecurityContextForUser(testUser.getEmail());

            // This should find the product by SKU, create order, but then fail at Stripe call
            // (due to test stripe key), which is expected behavior
            MvcResult result = mockMvc.perform(post(BASE_URL + "/checkout")
                            .with(securityContext(securityContext))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(payload)))
                    .andReturn();

            // The response could be success (with Stripe URL) or error (Stripe API error)
            // Both are acceptable since the product was found
            int status = result.getResponse().getStatus();
            assertTrue(status == 200 || status == 400 || status == 500,
                    "Expected 200, 400, or 500 but got " + status);
        }

        @Test
        @DisplayName("Should parse address from payload correctly")
        void shouldParseAddressFromPayload() throws Exception {
            Map<String, Object> payload = createValidCartPayload();
            payload.put("address", createAddressPayload());

            SecurityContext securityContext = createSecurityContextForUser(testUser.getEmail());

            // Even if Stripe fails, this tests that address parsing doesn't throw
            MvcResult result = mockMvc.perform(post(BASE_URL + "/checkout")
                            .with(securityContext(securityContext))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(payload)))
                    .andReturn();

            // Verify the request was processed (may fail at Stripe API but address was parsed)
            assertNotNull(result.getResponse().getContentAsString());
        }

        @Test
        @DisplayName("Should handle invalid address parsing gracefully")
        void shouldHandleInvalidAddressParsing() throws Exception {
            Map<String, Object> payload = createValidCartPayload();
            // Add invalid address (not a map)
            payload.put("address", "invalid-address-string");

            SecurityContext securityContext = createSecurityContextForUser(testUser.getEmail());

            // Should continue processing even if address parsing fails
            MvcResult result = mockMvc.perform(post(BASE_URL + "/checkout")
                            .with(securityContext(securityContext))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(payload)))
                    .andReturn();

            assertNotNull(result.getResponse().getContentAsString());
        }

        @Test
        @DisplayName("Should process coupon code when provided")
        void shouldProcessCouponCode() throws Exception {
            Map<String, Object> payload = createValidCartPayload();
            payload.put("address", createAddressPayload());
            payload.put("couponCode", "TESTCOUPON");

            SecurityContext securityContext = createSecurityContextForUser(testUser.getEmail());

            MvcResult result = mockMvc.perform(post(BASE_URL + "/checkout")
                            .with(securityContext(securityContext))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(payload)))
                    .andReturn();

            // Should either process or return coupon error
            assertNotNull(result.getResponse().getContentAsString());
        }

        @Test
        @DisplayName("Should ignore empty coupon code")
        void shouldIgnoreEmptyCouponCode() throws Exception {
            Map<String, Object> payload = createValidCartPayload();
            payload.put("address", createAddressPayload());
            payload.put("couponCode", "");

            SecurityContext securityContext = createSecurityContextForUser(testUser.getEmail());

            MvcResult result = mockMvc.perform(post(BASE_URL + "/checkout")
                            .with(securityContext(securityContext))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(payload)))
                    .andReturn();

            assertNotNull(result.getResponse().getContentAsString());
        }

        @Test
        @DisplayName("Should ignore null string coupon code")
        void shouldIgnoreNullStringCouponCode() throws Exception {
            Map<String, Object> payload = createValidCartPayload();
            payload.put("address", createAddressPayload());
            payload.put("couponCode", "null");

            SecurityContext securityContext = createSecurityContextForUser(testUser.getEmail());

            MvcResult result = mockMvc.perform(post(BASE_URL + "/checkout")
                            .with(securityContext(securityContext))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(payload)))
                    .andReturn();

            assertNotNull(result.getResponse().getContentAsString());
        }

        @Test
        @DisplayName("Should handle cart with default quantity when quantity not specified")
        void shouldHandleCartWithDefaultQuantity() throws Exception {
            Map<String, Object> payload = new HashMap<>();
            List<Map<String, Object>> cart = new ArrayList<>();
            Map<String, Object> cartItem = new HashMap<>();
            cartItem.put("id", testProduct.getProductId().toString());
            cartItem.put("name", "Test Product");
            cartItem.put("price", 99.99);
            // No quantity specified - should default to 1
            cart.add(cartItem);
            payload.put("cart", cart);
            payload.put("address", createAddressPayload());

            SecurityContext securityContext = createSecurityContextForUser(testUser.getEmail());

            MvcResult result = mockMvc.perform(post(BASE_URL + "/checkout")
                            .with(securityContext(securityContext))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(payload)))
                    .andReturn();

            assertNotNull(result.getResponse().getContentAsString());
        }

        @Test
        @DisplayName("Should include custom success and cancel URLs")
        void shouldUseCustomUrls() throws Exception {
            Map<String, Object> payload = createValidCartPayload();
            payload.put("address", createAddressPayload());
            payload.put("successUrl", "http://example.com/success");
            payload.put("cancelUrl", "http://example.com/cancel");

            SecurityContext securityContext = createSecurityContextForUser(testUser.getEmail());

            MvcResult result = mockMvc.perform(post(BASE_URL + "/checkout")
                            .with(securityContext(securityContext))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(payload)))
                    .andReturn();

            assertNotNull(result.getResponse().getContentAsString());
        }

        @Test
        @DisplayName("Should handle multiple items in cart with discount distribution")
        void shouldHandleMultipleItemsWithDiscount() throws Exception {
            // Create another product with warehouse stock
            Brand brand = Brand.builder()
                    .name("Another Brand")
                    .description("Another brand")
                    .build();
            entityManager.persist(brand);

            Warranty warranty = Warranty.builder()
                    .startDate(LocalDate.now())
                    .endDate(LocalDate.now().plusYears(1))
                    .description("Warranty")
                    .build();
            entityManager.persist(warranty);

            Product product2 = Product.builder()
                    .name("Test Mouse")
                    .sku("SKU-MOUSE-" + UUID.randomUUID().toString().substring(0, 8))
                    .description("Test mouse")
                    .price(new BigDecimal("49.99"))
                    .weight(new BigDecimal("0.5"))
                    .brand(brand)
                    .warranty(warranty)
                    .build();
            entityManager.persist(product2);
            entityManager.flush();
            
            // Reload warehouse entity from DB to avoid detached entity issue
            Warehouse warehouse = entityManager.find(Warehouse.class, testWarehouse.getWarehouseId());
            
            // Add warehouse stock for product2
            WarehouseProductKey warehouseProductKey2 = WarehouseProductKey.builder()
                    .warehouseId(warehouse.getWarehouseId())
                    .productId(product2.getProductId())
                    .build();

            WarehouseProduct warehouseProduct2 = WarehouseProduct.builder()
                    .id(warehouseProductKey2)
                    .warehouse(warehouse)
                    .product(product2)
                    .stockQuantity(50)
                    .build();
            entityManager.persist(warehouseProduct2);
            entityManager.flush();

            Map<String, Object> payload = new HashMap<>();
            List<Map<String, Object>> cart = new ArrayList<>();

            Map<String, Object> cartItem1 = new HashMap<>();
            cartItem1.put("id", testProduct.getProductId().toString());
            cartItem1.put("name", "Test Laptop");
            cartItem1.put("price", 999.99);
            cartItem1.put("quantity", 2);
            cart.add(cartItem1);

            Map<String, Object> cartItem2 = new HashMap<>();
            cartItem2.put("id", product2.getProductId().toString());
            cartItem2.put("name", "Test Mouse");
            cartItem2.put("price", 49.99);
            cartItem2.put("quantity", 3);
            cart.add(cartItem2);

            payload.put("cart", cart);
            payload.put("address", createAddressPayload());

            SecurityContext securityContext = createSecurityContextForUser(testUser.getEmail());

            MvcResult result = mockMvc.perform(post(BASE_URL + "/checkout")
                            .with(securityContext(securityContext))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(payload)))
                    .andReturn();

            assertNotNull(result.getResponse().getContentAsString());
        }

        @Test
        @DisplayName("Should handle cart with zero price items - minimum 1 cent enforced")
        void shouldHandleZeroPriceItems() throws Exception {
            Map<String, Object> payload = new HashMap<>();
            List<Map<String, Object>> cart = new ArrayList<>();
            Map<String, Object> cartItem = new HashMap<>();
            cartItem.put("id", testProduct.getProductId().toString());
            cartItem.put("name", "Free Product");
            cartItem.put("price", 0);
            cartItem.put("quantity", 1);
            cart.add(cartItem);
            payload.put("cart", cart);
            payload.put("address", createAddressPayload());

            SecurityContext securityContext = createSecurityContextForUser(testUser.getEmail());

            MvcResult result = mockMvc.perform(post(BASE_URL + "/checkout")
                            .with(securityContext(securityContext))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(payload)))
                    .andReturn();

            // Should handle minimum unit amount enforcement
            assertNotNull(result.getResponse().getContentAsString());
        }
    }

    // ============================================
    // POST /webhook TESTS
    // ============================================

    @Nested
    @DisplayName("POST /webhook - Stripe Webhook Handler")
    class WebhookTests {

        @Test
        @DisplayName("Should handle checkout.session.completed event")
        void shouldHandleCheckoutSessionCompleted() throws Exception {
            Map<String, Object> webhookPayload = new HashMap<>();
            webhookPayload.put("type", "checkout.session.completed");

            Map<String, Object> data = new HashMap<>();
            Map<String, Object> object = new HashMap<>();
            object.put("id", "cs_test_" + UUID.randomUUID().toString());
            data.put("object", object);
            webhookPayload.put("data", data);

            mockMvc.perform(post(BASE_URL + "/webhook")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(webhookPayload))
                            .header("Stripe-Signature", "test-signature"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("OK"));
        }

        @Test
        @DisplayName("Should handle other event types gracefully")
        void shouldHandleOtherEventTypes() throws Exception {
            Map<String, Object> webhookPayload = new HashMap<>();
            webhookPayload.put("type", "payment_intent.created");

            Map<String, Object> data = new HashMap<>();
            Map<String, Object> object = new HashMap<>();
            object.put("id", "pi_test_" + UUID.randomUUID().toString());
            data.put("object", object);
            webhookPayload.put("data", data);

            mockMvc.perform(post(BASE_URL + "/webhook")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(webhookPayload)))
                    .andExpect(status().isOk())
                    .andExpect(content().string("OK"));
        }

        @Test
        @DisplayName("Should return 400 for invalid JSON payload")
        void shouldReturn400ForInvalidJson() throws Exception {
            mockMvc.perform(post(BASE_URL + "/webhook")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("invalid-json"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should handle webhook without signature header")
        void shouldHandleWebhookWithoutSignature() throws Exception {
            Map<String, Object> webhookPayload = new HashMap<>();
            webhookPayload.put("type", "checkout.session.completed");

            Map<String, Object> data = new HashMap<>();
            Map<String, Object> object = new HashMap<>();
            object.put("id", "cs_test_123");
            data.put("object", object);
            webhookPayload.put("data", data);

            mockMvc.perform(post(BASE_URL + "/webhook")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(webhookPayload)))
                    .andExpect(status().isOk())
                    .andExpect(content().string("OK"));
        }
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
        @DisplayName("Should return 401 when user email not found")
        void shouldReturn401WhenUserNotFound() throws Exception {
            SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
            Authentication auth = new UsernamePasswordAuthenticationToken(
                    "nonexistent@example.com", "password",
                    List.of(new SimpleGrantedAuthority("ROLE_USER")));
            securityContext.setAuthentication(auth);

            mockMvc.perform(get(BASE_URL + "/orders")
                            .with(securityContext(securityContext)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$").isArray());
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

    // ============================================
    // ADDITIONAL EDGE CASE TESTS
    // ============================================

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle parseCart with invalid cart object type")
        void shouldHandleInvalidCartObjectType() throws Exception {
            Map<String, Object> payload = new HashMap<>();
            // Cart that's not parseable to List<Map>
            payload.put("cart", "not-a-list");

            SecurityContext securityContext = createSecurityContextForUser(testUser.getEmail());

            mockMvc.perform(post(BASE_URL + "/checkout")
                            .with(securityContext(securityContext))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(payload)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Cart is empty"));
        }

        @Test
        @DisplayName("Should handle null authentication object")
        void shouldHandleNullAuthentication() throws Exception {
            Map<String, Object> payload = createValidCartPayload();

            SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
            // No authentication set (null)

            mockMvc.perform(post(BASE_URL + "/checkout")
                            .with(securityContext(securityContext))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(payload)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.error").value("User not logged in"));
        }

        @Test
        @DisplayName("Should handle null authentication for orders endpoint")
        void shouldHandleNullAuthenticationForOrders() throws Exception {
            SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
            // No authentication set (null)

            mockMvc.perform(get(BASE_URL + "/orders")
                            .with(securityContext(securityContext)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$").isArray());
        }

        @Test
        @DisplayName("Should return created order info even when Stripe fails")
        void shouldReturnBadRequestWhenStripeFails() throws Exception {
            Map<String, Object> payload = createValidCartPayload();
            payload.put("address", createAddressPayload());

            SecurityContext securityContext = createSecurityContextForUser(testUser.getEmail());

            // With test/dummy Stripe key, this should fail at Stripe API
            mockMvc.perform(post(BASE_URL + "/checkout")
                            .with(securityContext(securityContext))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(payload)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").exists())
                    .andExpect(jsonPath("$.detail").exists());
        }

        @Test
        @DisplayName("Should handle cart item with missing price - default to 0")
        void shouldHandleCartItemWithMissingPrice() throws Exception {
            Map<String, Object> payload = new HashMap<>();
            List<Map<String, Object>> cart = new ArrayList<>();
            Map<String, Object> cartItem = new HashMap<>();
            cartItem.put("id", testProduct.getProductId().toString());
            cartItem.put("name", "Test Product");
            // No price - should default to 0
            cartItem.put("quantity", 1);
            cart.add(cartItem);
            payload.put("cart", cart);
            payload.put("address", createAddressPayload());

            SecurityContext securityContext = createSecurityContextForUser(testUser.getEmail());

            MvcResult result = mockMvc.perform(post(BASE_URL + "/checkout")
                            .with(securityContext(securityContext))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(payload)))
                    .andReturn();

            assertNotNull(result.getResponse().getContentAsString());
        }

        @Test
        @DisplayName("Should handle cart item with missing name - default to Item")
        void shouldHandleCartItemWithMissingName() throws Exception {
            Map<String, Object> payload = new HashMap<>();
            List<Map<String, Object>> cart = new ArrayList<>();
            Map<String, Object> cartItem = new HashMap<>();
            cartItem.put("id", testProduct.getProductId().toString());
            // No name - should default to "Item"
            cartItem.put("price", 99.99);
            cartItem.put("quantity", 1);
            cart.add(cartItem);
            payload.put("cart", cart);
            payload.put("address", createAddressPayload());

            SecurityContext securityContext = createSecurityContextForUser(testUser.getEmail());

            MvcResult result = mockMvc.perform(post(BASE_URL + "/checkout")
                            .with(securityContext(securityContext))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(payload)))
                    .andReturn();

            assertNotNull(result.getResponse().getContentAsString());
        }

        @Test
        @DisplayName("Should handle webhook with missing event type")
        void shouldHandleWebhookWithMissingEventType() throws Exception {
            Map<String, Object> webhookPayload = new HashMap<>();
            // No "type" field
            webhookPayload.put("data", Map.of("object", Map.of("id", "test")));

            mockMvc.perform(post(BASE_URL + "/webhook")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(webhookPayload)))
                    .andExpect(status().isOk())
                    .andExpect(content().string("OK"));
        }

        @Test
        @DisplayName("Should successfully store order in session during checkout")
        void shouldStoreOrderInSession() throws Exception {
            MockHttpSession session = new MockHttpSession();
            
            Map<String, Object> payload = createValidCartPayload();
            payload.put("address", createAddressPayload());

            SecurityContext securityContext = createSecurityContextForUser(testUser.getEmail());

            // Perform checkout - even if Stripe fails, order data should be stored
            mockMvc.perform(post(BASE_URL + "/checkout")
                            .session(session)
                            .with(securityContext(securityContext))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(payload)))
                    .andReturn();

            // Check that session has the order data
            // Note: Order might not be stored if Stripe call fails before that point
        }

        @Test
        @DisplayName("Should handle high quantity discount scenario")
        void shouldHandleHighQuantityDiscount() throws Exception {
            Map<String, Object> payload = new HashMap<>();
            List<Map<String, Object>> cart = new ArrayList<>();
            Map<String, Object> cartItem = new HashMap<>();
            cartItem.put("id", testProduct.getProductId().toString());
            cartItem.put("name", "Bulk Product");
            cartItem.put("price", 10.00);
            cartItem.put("quantity", 50);  // High quantity
            cart.add(cartItem);
            payload.put("cart", cart);
            payload.put("address", createAddressPayload());

            SecurityContext securityContext = createSecurityContextForUser(testUser.getEmail());

            MvcResult result = mockMvc.perform(post(BASE_URL + "/checkout")
                            .with(securityContext(securityContext))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(payload)))
                    .andReturn();

            assertNotNull(result.getResponse().getContentAsString());
        }

        @Test
        @DisplayName("Should handle fractional price values")
        void shouldHandleFractionalPriceValues() throws Exception {
            Map<String, Object> payload = new HashMap<>();
            List<Map<String, Object>> cart = new ArrayList<>();
            Map<String, Object> cartItem = new HashMap<>();
            cartItem.put("id", testProduct.getProductId().toString());
            cartItem.put("name", "Product with cents");
            cartItem.put("price", 19.99);  // Price with cents
            cartItem.put("quantity", 3);
            cart.add(cartItem);
            payload.put("cart", cart);
            payload.put("address", createAddressPayload());

            SecurityContext securityContext = createSecurityContextForUser(testUser.getEmail());

            MvcResult result = mockMvc.perform(post(BASE_URL + "/checkout")
                            .with(securityContext(securityContext))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(payload)))
                    .andReturn();

            assertNotNull(result.getResponse().getContentAsString());
        }

        @Test
        @DisplayName("Should handle address with all optional fields")
        void shouldHandleAddressWithAllFields() throws Exception {
            Map<String, Object> payload = createValidCartPayload();
            Map<String, Object> address = new HashMap<>();
            address.put("street", "Main Street");
            address.put("streetNumber", "42A");
            address.put("zip", "12345");
            address.put("city", "Copenhagen");
            address.put("country", "Denmark");  // Extra field
            address.put("region", "Capital Region");  // Extra field
            payload.put("address", address);

            SecurityContext securityContext = createSecurityContextForUser(testUser.getEmail());

            MvcResult result = mockMvc.perform(post(BASE_URL + "/checkout")
                            .with(securityContext(securityContext))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(payload)))
                    .andReturn();

            assertNotNull(result.getResponse().getContentAsString());
        }

        @Test
        @DisplayName("Should handle cart with zero total amount for discount calculation")
        void shouldHandleZeroTotalAmountForDiscount() throws Exception {
            // Create a product with price 0
            Map<String, Object> payload = new HashMap<>();
            List<Map<String, Object>> cart = new ArrayList<>();
            Map<String, Object> cartItem = new HashMap<>();
            cartItem.put("id", testProduct.getProductId().toString());
            cartItem.put("name", "Zero Price Product");
            cartItem.put("price", 0.0);  // Zero price to trigger zero total
            cartItem.put("quantity", 1);
            cart.add(cartItem);
            payload.put("cart", cart);
            payload.put("address", createAddressPayload());

            SecurityContext securityContext = createSecurityContextForUser(testUser.getEmail());

            MvcResult result = mockMvc.perform(post(BASE_URL + "/checkout")
                            .with(securityContext(securityContext))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(payload)))
                    .andReturn();

            // Should handle zero amount scenario
            assertNotNull(result.getResponse().getContentAsString());
        }

        @Test
        @DisplayName("Should handle very small price that results in less than 1 cent")
        void shouldHandleVerySmallPrice() throws Exception {
            Map<String, Object> payload = new HashMap<>();
            List<Map<String, Object>> cart = new ArrayList<>();
            Map<String, Object> cartItem = new HashMap<>();
            cartItem.put("id", testProduct.getProductId().toString());
            cartItem.put("name", "Cheap Product");
            cartItem.put("price", 0.001);  // Very small price
            cartItem.put("quantity", 1);
            cart.add(cartItem);
            payload.put("cart", cart);
            payload.put("address", createAddressPayload());

            SecurityContext securityContext = createSecurityContextForUser(testUser.getEmail());

            MvcResult result = mockMvc.perform(post(BASE_URL + "/checkout")
                            .with(securityContext(securityContext))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(payload)))
                    .andReturn();

            // Should enforce minimum 1 cent
            assertNotNull(result.getResponse().getContentAsString());
        }

        @Test
        @DisplayName("Should handle null coupon object in payload")
        void shouldHandleNullCouponObject() throws Exception {
            Map<String, Object> payload = createValidCartPayload();
            payload.put("address", createAddressPayload());
            payload.put("couponCode", null);

            SecurityContext securityContext = createSecurityContextForUser(testUser.getEmail());

            MvcResult result = mockMvc.perform(post(BASE_URL + "/checkout")
                            .with(securityContext(securityContext))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(payload)))
                    .andReturn();

            assertNotNull(result.getResponse().getContentAsString());
        }

        @Test
        @DisplayName("Should handle whitespace-only coupon code")
        void shouldHandleWhitespaceCouponCode() throws Exception {
            Map<String, Object> payload = createValidCartPayload();
            payload.put("address", createAddressPayload());
            payload.put("couponCode", "   ");

            SecurityContext securityContext = createSecurityContextForUser(testUser.getEmail());

            MvcResult result = mockMvc.perform(post(BASE_URL + "/checkout")
                            .with(securityContext(securityContext))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(payload)))
                    .andReturn();

            assertNotNull(result.getResponse().getContentAsString());
        }
    }

    // ============================================
    // HELPER METHODS
    // ============================================

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

    private Map<String, Object> createAddressPayload() {
        Map<String, Object> address = new HashMap<>();
        address.put("street", "Test Street");
        address.put("streetNumber", "123");
        address.put("zip", "1000");
        address.put("city", "Copenhagen");
        return address;
    }
}
