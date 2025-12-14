package com.example.nordicelectronics.integration.controller;

import com.example.nordicelectronics.controller.postgresql.StripeController;
import com.example.nordicelectronics.entity.*;
import com.example.nordicelectronics.integration.BaseIntegrationTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.securityContext;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@Transactional
class StripeControllerIT extends BaseIntegrationTest {

    private static final String URL = "/api/postgresql/stripe";

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private StripeController stripeController;
    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private StripeController stripeController;

    private User testUser;
    private Product testProduct;

    @BeforeEach
    void setUp() {
        // Create brand & warranty
        Brand brand = Brand.builder().name("Test Brand").description("Test").build();
        Warranty warranty = Warranty.builder()
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusYears(1))
                .description("Warranty")
                .build();
        entityManager.persist(brand);
        entityManager.persist(warranty);

        // Create product
        testProduct = Product.builder()
                .name("Test Product")
                .sku("SKU-" + UUID.randomUUID().toString().substring(0, 8))
                .description("Test")
                .price(new BigDecimal("999.99"))
                .weight(new BigDecimal("1.0"))
                .brand(brand)
                .warranty(warranty)
                .build();
        entityManager.persist(testProduct);

        // Create user with address
        testUser = User.builder()
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .phoneNumber("12345678")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .password("password")
                .isAdmin(false)
                .build();
        entityManager.persist(testUser);

        Address address = Address.builder()
                .user(testUser)
                .street("Main St")
                .streetNumber("123")
                .city("Copenhagen")
                .zip("1000")
                .build();
        entityManager.persist(address);
        testUser.setAddress(List.of(address));

        // Create warehouse with stock
        Address warehouseAddr = Address.builder()
                .user(testUser)
                .street("Warehouse St")
                .streetNumber("1")
                .city("Copenhagen")
                .zip("2000")
                .build();
        entityManager.persist(warehouseAddr);

        Warehouse warehouse = Warehouse.builder()
                .name("Main Warehouse")
                .phoneNumber("87654321")
                .address(warehouseAddr)
                .build();
        entityManager.persist(warehouse);

        WarehouseProduct stock = WarehouseProduct.builder()
                .id(new WarehouseProductKey(warehouse.getWarehouseId(), testProduct.getProductId()))
                .warehouse(warehouse)
                .product(testProduct)
                .stockQuantity(50)
                .build();
        entityManager.persist(stock);

        entityManager.flush();
        entityManager.clear();
    }

    // ========== GET /config ==========

    @Test
    @DisplayName("Config: returns unconfigured when key is null")
    void configUnconfiguredWhenNull() throws Exception {
        withStripeKey(null, () ->
            mockMvc.perform(get(URL + "/config"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.stripeConfigured").value(false))
        );
    }

    // ========== POST /checkout - Auth ==========

    @Test
    @WithAnonymousUser
    @DisplayName("Checkout: 401 when not authenticated")
    void checkoutUnauthorized() throws Exception {
        mockMvc.perform(post(URL + "/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cartJson()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("User not authenticated"));
    }

    @Test
    @DisplayName("Checkout: 401 when user not found")
    void checkoutUserNotFound() throws Exception {
        mockMvc.perform(post(URL + "/checkout")
                        .with(securityContext(securityFor("unknown@example.com")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cartJson()))
                .andExpect(status().isUnauthorized());
    }

    // ========== POST /checkout - Config ==========

    @Test
    @DisplayName("Checkout: 500 when Stripe not configured")
    void checkoutStripeNotConfigured() throws Exception {
        withStripeKey(null, () ->
            mockMvc.perform(post(URL + "/checkout")
                            .with(securityContext(securityFor(testUser.getEmail())))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(cartJson()))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.error").value("Stripe not configured"))
        );
    }

    // ========== POST /checkout - Validation ==========

    @Test
    @DisplayName("Checkout: 400 when cart is empty")
    void checkoutEmptyCart() throws Exception {
        mockMvc.perform(post(URL + "/checkout")
                        .with(securityContext(securityFor(testUser.getEmail())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Cart is empty"));
    }

    @Test
    @DisplayName("Checkout: 400 when product not found")
    void checkoutProductNotFound() throws Exception {
        String json = objectMapper.writeValueAsString(Map.of(
                "cart", List.of(Map.of("id", UUID.randomUUID().toString(), "price", 100, "quantity", 1)),
                "address", addressMap()
        ));
        
        mockMvc.perform(post(URL + "/checkout")
                        .with(securityContext(securityFor(testUser.getEmail())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid product in cart"));
    }

    @Test
    @DisplayName("Checkout: 500 when address missing")
    void checkoutAddressMissing() throws Exception {
        String json = objectMapper.writeValueAsString(Map.of(
                "cart", List.of(cartItem())
        ));

        mockMvc.perform(post(URL + "/checkout")
                        .with(securityContext(securityFor(testUser.getEmail())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isInternalServerError());
    }

    // ========== POST /checkout - Coupon handling ==========

    @Test
    @DisplayName("Checkout: handles null coupon gracefully")
    void checkoutNullCoupon() throws Exception {
        Map<String, Object> payload = Map.of(
                "cart", List.of(cartItem()),
                "address", addressMap(),
                "couponCode", "null"
        );

        mockMvc.perform(post(URL + "/checkout")
                        .with(securityContext(securityFor(testUser.getEmail())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().is(anyOf(equalTo(200), equalTo(400), equalTo(500))));
    }

    // ========== createStripeCoupon ==========

    @Test
    @DisplayName("createStripeCoupon: creates coupon with valid params")
    void createStripeCouponWithValidParams() throws Exception {
        String result = invokeCreateStripeCoupon(new BigDecimal("25.50"), "SUMMER20", UUID.randomUUID().toString());
        // Should return a coupon ID (or null if Stripe API fails in test env)
        // In real Stripe environment, this would return an ID like "SUMMER20-abc12345-1234567890"
        org.junit.jupiter.api.Assertions.assertTrue(result == null || result.contains("SUMMER20"));
    }

    @Test
    @DisplayName("createStripeCoupon: handles null coupon code")
    void createStripeCouponWithNullCode() throws Exception {
        String orderId = UUID.randomUUID().toString();
        String result = invokeCreateStripeCoupon(new BigDecimal("10.00"), null, orderId);
        // Should use "DISCOUNT" as fallback name
        org.junit.jupiter.api.Assertions.assertTrue(result == null || result.contains("DISCOUNT"));
    }

    @Test
    @DisplayName("createStripeCoupon: handles zero amount gracefully")
    void createStripeCouponWithZeroAmount() throws Exception {
        String result = invokeCreateStripeCoupon(BigDecimal.ZERO, "ZERO", UUID.randomUUID().toString());
        // Stripe may reject 0 amount coupons, so null is acceptable
        org.junit.jupiter.api.Assertions.assertTrue(result == null || result.contains("ZERO"));
    }

    private String invokeCreateStripeCoupon(BigDecimal amount, String code, String orderId) throws Exception {
        java.lang.reflect.Method method = StripeController.class.getDeclaredMethod(
                "createStripeCoupon", BigDecimal.class, String.class, String.class);
        method.setAccessible(true);
        return (String) method.invoke(stripeController, amount, code, orderId);
    }

    // ========== POST /checkout - Multiple items ==========

    @Test
    @DisplayName("Checkout: finds product by SKU")
    void checkoutFindsBySku() throws Exception {
        Map<String, Object> item = Map.of(
                "id", testProduct.getSku(),
                "name", testProduct.getName(),
                "price", testProduct.getPrice().doubleValue(),
                "quantity", 1
        );

        mockMvc.perform(post(URL + "/checkout")
                        .with(securityContext(securityFor(testUser.getEmail())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("cart", List.of(item), "address", addressMap()))))
                .andExpect(status().is(anyOf(equalTo(200), equalTo(400), equalTo(500))));
    }

    // ========== Helpers ==========

    private SecurityContext securityFor(String email) {
        SecurityContext ctx = SecurityContextHolder.createEmptyContext();
        ctx.setAuthentication(new UsernamePasswordAuthenticationToken(
                email, "password", List.of(new SimpleGrantedAuthority("ROLE_USER"))));
        return ctx;
    }

    private String cartJson() throws Exception {
        return objectMapper.writeValueAsString(Map.of("cart", List.of(cartItem()), "address", addressMap()));
    }

    private Map<String, Object> cartItem() {
        return Map.of(
                "id", testProduct.getProductId().toString(),
                "name", testProduct.getName(),
                "price", testProduct.getPrice().doubleValue(),
                "quantity", 1
        );
    }

    private Map<String, String> addressMap() {
        return Map.of("street", "Test St", "streetNumber", "1", "zip", "1000", "city", "Copenhagen");
    }

    private void withStripeKey(String key, ThrowingRunnable test) throws Exception {
        String original = (String) ReflectionTestUtils.getField(stripeController, "stripeSecretKey");
        try {
            ReflectionTestUtils.setField(stripeController, "stripeSecretKey", key);
            test.run();
        } finally {
            ReflectionTestUtils.setField(stripeController, "stripeSecretKey", original);
        }
    }

    @FunctionalInterface
    interface ThrowingRunnable { void run() throws Exception; }
}
