package com.example.nordicelectronics.controller.postgresql;

import com.example.nordicelectronics.entity.Order;
import com.example.nordicelectronics.entity.Product;
import com.example.nordicelectronics.entity.User;
import com.example.nordicelectronics.entity.dto.address.AddressRequestDTO;
import com.example.nordicelectronics.entity.dto.order.OrderProductRequestDTO;
import com.example.nordicelectronics.entity.dto.order.OrderRequestDTO;
import com.example.nordicelectronics.entity.enums.OrderStatus;
import com.example.nordicelectronics.exception.StripeApiException;
import com.example.nordicelectronics.repositories.sql.ProductRepository;
import com.example.nordicelectronics.service.OrderService;
import com.example.nordicelectronics.service.UserService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Handles Stripe payment checkout sessions.
 * Creates orders in the database and redirects to Stripe for payment.
 */
@Slf4j
@Tag(name = "Stripe Controller", description = "Handles Stripe payment operations")
@CrossOrigin
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/postgresql/stripe")
public class StripeController {

    private final ObjectMapper objectMapper;
    private final UserService userService;
    private final OrderService orderService;
    private final ProductRepository productRepository;

    @Value("${stripe.secret.key}")
    private String stripeSecretKey;

    /**
     * Returns whether Stripe is configured.
     */
    @Operation(summary = "Get Stripe configuration status")
    @GetMapping("/config")
    public ResponseEntity<Map<String, Object>> getStripeConfig() {
        boolean configured = stripeSecretKey != null && !stripeSecretKey.isBlank();
        
        Map<String, Object> response = new HashMap<>();
        response.put("stripeConfigured", configured);
        response.put("source", configured ? "properties" : "none");
        response.put("keyPrefix", configured ? stripeSecretKey.substring(0, 12) + "..." : "none");
        
        return ResponseEntity.ok(response);
    }

    /**
     * Creates a Stripe checkout session and an order in the database.
     */
    @Operation(summary = "Create Stripe checkout session")
    @PostMapping("/checkout")
    public ResponseEntity<Map<String, Object>> createCheckoutSession(@RequestBody Map<String, Object> payload) {
        try {
            // 1. Check user is logged in
            User user = getCurrentUser();
            if (user == null) {
                return errorResponse(HttpStatus.UNAUTHORIZED, "User not authenticated");
            }

            // 2. Check Stripe is configured
            if (stripeSecretKey == null || stripeSecretKey.isBlank()) {
                return errorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Stripe not configured");
            }

            // 3. Parse cart items
            List<Map<String, Object>> cartItems = getCartFromPayload(payload);
            if (cartItems.isEmpty()) {
                return errorResponse(HttpStatus.BAD_REQUEST, "Cart is empty");
            }

            // 4. Validate products exist
            List<OrderProductRequestDTO> orderProducts = validateAndConvertCart(cartItems);
            if (orderProducts == null) {
                return errorResponse(HttpStatus.BAD_REQUEST, "Invalid product in cart");
            }

            // 5. Parse optional fields
            AddressRequestDTO address = getAddressFromPayload(payload);
            String couponCode = getStringFromPayload(payload, "couponCode");
            String successUrl = getStringFromPayload(payload, "successUrl");
            String cancelUrl = getStringFromPayload(payload, "cancelUrl");

            // 6. Create order in database
            Order order = saveOrder(user.getUserId(), orderProducts, address, couponCode);
            if (order == null) {
                return errorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to create order");
            }

            // 7. Create Stripe checkout session
            String orderId = order.getOrderId().toString();
            BigDecimal discount = order.getDiscountAmount() != null ? order.getDiscountAmount() : BigDecimal.ZERO;
            
            if (successUrl == null) {
                successUrl = "http://localhost:8080/?checkout=success&order=" + orderId;
            }
            if (cancelUrl == null) {
                cancelUrl = "http://localhost:8080/?checkout=cancel";
            }

            Map<String, Object> stripeSession = callStripeCheckout(
                    cartItems, successUrl, cancelUrl, discount, orderId, user.getEmail(), address, couponCode
            );

            // 8. Mark order as confirmed
            order.setOrderStatus(OrderStatus.confirmed);
            orderService.save(order);

            // 9. Return checkout URL
            Map<String, Object> response = new HashMap<>();
            response.put("url", stripeSession.get("url"));
            response.put("orderId", orderId);
            response.put("sessionId", stripeSession.get("id"));
            return ResponseEntity.ok(response);

        } catch (StripeApiException e) {
            log.error("Stripe API error: {}", e.getMessage());
            return errorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            log.error("Checkout error", e);
            return errorResponse(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    // ==================== PRIVATE HELPERS ====================

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            return null;
        }
        try {
            return userService.findByEmail(auth.getName());
        } catch (Exception e) {
            log.error("Failed to find user: {}", e.getMessage());
            return null;
        }
    }

    private ResponseEntity<Map<String, Object>> errorResponse(HttpStatus status, String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("error", message);
        return ResponseEntity.status(status).body(body);
    }

    private List<Map<String, Object>> getCartFromPayload(Map<String, Object> payload) {
        Object cart = payload.get("cart");
        if (cart == null) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.convertValue(cart, new TypeReference<List<Map<String, Object>>>() {});
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private String getStringFromPayload(Map<String, Object> payload, String key) {
        Object value = payload.get(key);
        if (value == null) {
            return null;
        }
        String str = String.valueOf(value).trim();
        if (str.isEmpty() || "null".equals(str)) {
            return null;
        }
        return str;
    }

    private AddressRequestDTO getAddressFromPayload(Map<String, Object> payload) {
        Object addressObj = payload.get("address");
        if (addressObj == null) {
            return null;
        }
        try {
            Map<String, Object> addressMap = objectMapper.convertValue(addressObj, new TypeReference<Map<String, Object>>() {});
            return AddressRequestDTO.builder()
                    .street((String) addressMap.get("street"))
                    .streetNumber((String) addressMap.get("streetNumber"))
                    .zip((String) addressMap.get("zip"))
                    .city((String) addressMap.get("city"))
                    .build();
        } catch (Exception e) {
            return null;
        }
    }

    private List<OrderProductRequestDTO> validateAndConvertCart(List<Map<String, Object>> cartItems) {
        List<OrderProductRequestDTO> orderProducts = new ArrayList<>();
        
        for (Map<String, Object> item : cartItems) {
            String itemId = String.valueOf(item.get("id"));
            int quantity = Integer.parseInt(String.valueOf(item.getOrDefault("quantity", 1)));
            
            // Try to find product by UUID or SKU
            UUID productId = findProductId(itemId);
            if (productId == null) {
                log.error("Product not found: {}", itemId);
                return null;
            }
            
            orderProducts.add(OrderProductRequestDTO.builder()
                    .productId(productId)
                    .quantity(quantity)
                    .build());
        }
        
        return orderProducts;
    }

    private UUID findProductId(String idOrSku) {
        // First try as UUID
        try {
            UUID uuid = UUID.fromString(idOrSku);
            if (productRepository.existsById(uuid)) {
                return uuid;
            }
        } catch (IllegalArgumentException ignored) {
            // Not a valid UUID, try as SKU
        }
        
        // Try as SKU
        Optional<Product> product = productRepository.findBySku(idOrSku);
        return product.map(Product::getProductId).orElse(null);
    }

    private Order saveOrder(UUID userId, List<OrderProductRequestDTO> products, AddressRequestDTO address, String couponCode) {
        try {
            OrderRequestDTO.OrderRequestDTOBuilder builder = OrderRequestDTO.builder()
                    .userId(userId)
                    .orderProducts(products);
            
            if (address != null) {
                builder.address(address);
            }
            if (couponCode != null) {
                builder.couponCode(couponCode);
            }
            
            return orderService.createOrder(builder.build());
        } catch (Exception e) {
            log.error("Failed to create order: {}", e.getMessage());
            return null;
        }
    }

    // ==================== STRIPE API CALLS ====================

    private Map<String, Object> callStripeCheckout(
            List<Map<String, Object>> cartItems,
            String successUrl,
            String cancelUrl,
            BigDecimal discount,
            String orderId,
            String customerEmail,
            AddressRequestDTO address,
            String couponCode
    ) throws StripeApiException {
        
        StringBuilder params = new StringBuilder();
        
        // Basic session params
        addParam(params, "mode", "payment");
        addParam(params, "payment_method_types[]", "card");
        addParam(params, "success_url", successUrl);
        addParam(params, "cancel_url", cancelUrl);
        addParam(params, "customer_email", customerEmail);
        addParam(params, "metadata[orderId]", orderId);
        
        // Add coupon metadata if present
        if (couponCode != null) {
            addParam(params, "metadata[couponCode]", couponCode);
            addParam(params, "metadata[discountAmount]", discount.setScale(2, RoundingMode.HALF_UP).toString());
        }
        
        // Add address metadata if present
        if (address != null) {
            String fullAddress = String.format("%s %s, %s %s",
                    safe(address.getStreet()),
                    safe(address.getStreetNumber()),
                    safe(address.getZip()),
                    safe(address.getCity())
            ).trim();
            addParam(params, "metadata[shippingAddress]", fullAddress);
            addParam(params, "metadata[street]", address.getStreet());
            addParam(params, "metadata[streetNumber]", address.getStreetNumber());
            addParam(params, "metadata[zip]", address.getZip());
            addParam(params, "metadata[city]", address.getCity());
        }
        
        // Add line items (products)
        for (int i = 0; i < cartItems.size(); i++) {
            Map<String, Object> item = cartItems.get(i);
            String name = String.valueOf(item.get("name"));
            double price = Double.parseDouble(String.valueOf(item.get("price")));
            int quantity = Integer.parseInt(String.valueOf(item.getOrDefault("quantity", 1)));
            long priceInCents = Math.max(1, Math.round(price * 100));
            
            addParam(params, "line_items[" + i + "][price_data][currency]", "usd");
            addParam(params, "line_items[" + i + "][price_data][product_data][name]", name);
            addParam(params, "line_items[" + i + "][price_data][unit_amount]", String.valueOf(priceInCents));
            addParam(params, "line_items[" + i + "][quantity]", String.valueOf(quantity));
        }
        
        // Create and apply Stripe coupon if there's a discount
        if (discount.compareTo(BigDecimal.ZERO) > 0) {
            String stripeCouponId = createStripeCoupon(discount, couponCode, orderId);
            if (stripeCouponId != null) {
                addParam(params, "discounts[0][coupon]", stripeCouponId);
            }
        }
        
        return callStripeApi("/v1/checkout/sessions", params.toString());
    }

    private String createStripeCoupon(BigDecimal amount, String couponCode, String orderId) {
        try {
            String couponName = couponCode != null ? couponCode : "DISCOUNT";
            String couponId = couponName + "-" + orderId.substring(0, 8) + "-" + System.currentTimeMillis();
            long amountInCents = Math.round(amount.doubleValue() * 100);
            
            StringBuilder params = new StringBuilder();
            addParam(params, "id", couponId);
            addParam(params, "name", couponName);
            addParam(params, "amount_off", String.valueOf(amountInCents));
            addParam(params, "currency", "usd");
            addParam(params, "duration", "once");
            
            Map<String, Object> response = callStripeApi("/v1/coupons", params.toString());
            return (String) response.get("id");
        } catch (Exception e) {
            log.warn("Failed to create Stripe coupon: {}", e.getMessage());
            return null;
        }
    }

    private Map<String, Object> callStripeApi(String endpoint, String formData) throws StripeApiException {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.stripe.com" + endpoint))
                    .header("Authorization", "Bearer " + stripeSecretKey)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(formData))
                    .build();

            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return objectMapper.readValue(response.body(), new TypeReference<Map<String, Object>>() {});
            } else {
                throw new StripeApiException("Stripe API error: " + response.body(), response.statusCode(), response.body());
            }
        } catch (StripeApiException e) {
            throw e;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new StripeApiException("Request interrupted", e);
        } catch (Exception e) {
            throw new StripeApiException("Stripe API call failed: " + e.getMessage(), e);
        }
    }

    private void addParam(StringBuilder sb, String key, String value) {
        if (value == null || value.isEmpty()) {
            return;
        }
        if (sb.length() > 0) {
            sb.append("&");
        }
        sb.append(URLEncoder.encode(key, StandardCharsets.UTF_8));
        sb.append("=");
        sb.append(URLEncoder.encode(value, StandardCharsets.UTF_8));
    }

    private String safe(String value) {
        return value != null ? value : "";
    }
}
