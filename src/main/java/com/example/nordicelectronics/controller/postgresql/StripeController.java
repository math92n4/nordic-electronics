package com.example.nordicelectronics.controller.postgresql;

import com.example.nordicelectronics.entity.User;
import com.example.nordicelectronics.exception.StripeApiException;
import com.example.nordicelectronics.service.UserService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;

@Tag(name = "Stripe Controller", description = "Handles Stripe payment operations")
@CrossOrigin
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/postgresql/stripe")
public class StripeController {

    private static final Logger log = LoggerFactory.getLogger(StripeController.class);
    private static final String USER_ORDERS_SESSION_KEY = "userOrders";
    private static final String CREATED_AT_SESSION_KEY = "createdAt";
    private static final String ERROR_SESSION_KEY = "error";
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final UserService userService;

    @Value("${stripe.secret.key}")
    private String stripeSecretKey;

    @Operation(summary = "Create Stripe checkout session", description = "Creates a Stripe checkout session for cart items and creates an order")
    @PostMapping("/checkout")
    public ResponseEntity<Map<String, Object>> createCheckoutSession(@RequestBody Map<String, Object> payload, HttpSession session) {
        try {
            // Get current authenticated user from Spring Security context
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of(ERROR_SESSION_KEY, "User not logged in"));
            }

            // Get user details
            User currentUser;
            try {
                currentUser = userService.findByEmail(authentication.getName());
                if (currentUser == null) {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body(Map.of(ERROR_SESSION_KEY, "User not found"));
                }
            } catch (Exception e) {
                log.error("Error finding user: {}", e.getMessage());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of(ERROR_SESSION_KEY, "User lookup failed"));
            }

            String stripeKey = stripeSecretKey;
            if (stripeKey == null || stripeKey.isBlank()) {
                log.error("STRIPE_SECRET_KEY not configured");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of(ERROR_SESSION_KEY, "Stripe secret key not configured"));
            }

            // Parse cart
            List<Map<String, Object>> cart = parseCart(payload);
            if (cart.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of(ERROR_SESSION_KEY, "Cart is empty"));
            }

            // Create order record first
            String orderId = UUID.randomUUID().toString();

            String successUrl = (String) payload.getOrDefault("successUrl", "http://localhost:8080/?checkout=success&order=" + orderId);
            String cancelUrl = (String) payload.getOrDefault("cancelUrl", "http://localhost:8080/?checkout=cancel");

            // Create Stripe session
            Map<String, Object> stripeResponse = createStripeSession(cart, successUrl, cancelUrl, stripeKey);
            String checkoutUrl = (String) stripeResponse.get("url");
            String sessionId = (String) stripeResponse.get("id");

            // Store order in session (in production, save to database)
            Map<String, Object> orderData = new HashMap<>();
            orderData.put("orderId", orderId);
            orderData.put("cart", cart);
            orderData.put("sessionId", sessionId);
            orderData.put("status", "PENDING");
            orderData.put(CREATED_AT_SESSION_KEY, LocalDateTime.now().toString());
            orderData.put("userEmail", currentUser.getEmail());
            orderData.put("userId", currentUser.getUserId().toString());

            // Store in session (temporary solution)
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> userOrders = (List<Map<String, Object>>) session.getAttribute(USER_ORDERS_SESSION_KEY);
            if (userOrders == null) {
                userOrders = new ArrayList<>();
            }
            userOrders.add(orderData);
            session.setAttribute(USER_ORDERS_SESSION_KEY, userOrders);

            return ResponseEntity.ok(Map.of(
                "url", checkoutUrl,
                "orderId", orderId,
                "sessionId", sessionId
            ));

        } catch (StripeApiException ex) {
            log.error("Stripe API error: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(ERROR_SESSION_KEY, "Payment processing error", "detail", ex.getMessage()));
        } catch (Exception ex) {
            log.error("Error creating checkout session", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(ERROR_SESSION_KEY, "Internal server error", "detail", ex.getMessage()));
        }
    }

    @Operation(summary = "Get Stripe configuration status")
    @GetMapping("/config")
    public ResponseEntity<Map<String, Object>> getStripeConfig() {
        boolean configured = stripeSecretKey != null && !stripeSecretKey.isBlank();
        String source = configured ? "properties" : "none";

        return ResponseEntity.ok(Map.of(
            "stripeConfigured", configured,
            "source", source,
            "keyPrefix", configured ? stripeSecretKey.substring(0, 12) + "..." : "none"
        ));
    }

    @Operation(summary = "Stripe webhook endpoint")
    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(@RequestBody String payload, @RequestHeader(value = "Stripe-Signature", required = false) String signature) {
        try {
            log.info("Received Stripe webhook");

            // Parse the webhook payload
            Map<String, Object> event = objectMapper.readValue(payload, new TypeReference<Map<String, Object>>() {});
            String eventType = (String) event.get("type");

            if ("checkout.session.completed".equals(eventType)) {
                Map<String, Object> data = (Map<String, Object>) event.get("data");
                Map<String, Object> object = (Map<String, Object>) data.get("object");
                String sessionId = (String) object.get("id");

                log.info("Payment completed for session: {}", sessionId);
                // In production, you would update the order status in the database here
            }

            return ResponseEntity.ok("OK");
        } catch (Exception ex) {
            log.error("Error processing webhook", ex);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ERROR_SESSION_KEY);
        }
    }

    @Operation(summary = "Get user orders")
    @GetMapping("/orders")
    public ResponseEntity<List<Map<String, Object>>> getUserOrders(HttpSession session) {
        // Get current authenticated user from Spring Security context
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Collections.emptyList());
        }

        // Get user details
        User currentUser;
        try {
            currentUser = userService.findByEmail(authentication.getName());
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Collections.emptyList());
            }
        } catch (Exception e) {
            log.error("Error finding user for orders: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Collections.emptyList());
        }

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> userOrders = (List<Map<String, Object>>) session.getAttribute(USER_ORDERS_SESSION_KEY);
        if (userOrders == null) {
            userOrders = new ArrayList<>();
        }

        // Filter orders by current user email
        String userEmail = currentUser.getEmail();
        List<Map<String, Object>> filteredOrders = userOrders.stream()
                .filter(order -> userEmail.equals(order.get("userEmail")))
                .sorted((o1, o2) -> ((String) o2.get(CREATED_AT_SESSION_KEY)).compareTo((String) o1.get(CREATED_AT_SESSION_KEY)))
                .toList();

        return ResponseEntity.ok(filteredOrders);
    }

    private List<Map<String, Object>> parseCart(Map<String, Object> payload) {
        Object cartObj = payload.get("cart");
        if (cartObj == null) {
            return Collections.emptyList();
        }

        try {
            return objectMapper.convertValue(cartObj, new TypeReference<List<Map<String, Object>>>() {});
        } catch (Exception e) {
            log.warn("Unable to parse cart payload", e);
            return Collections.emptyList();
        }
    }

    private Map<String, Object> createStripeSession(List<Map<String, Object>> cart, String successUrl, String cancelUrl, String stripeKey) throws StripeApiException {
        try {
            StringBuilder form = new StringBuilder();
            append(form, "mode", "payment");
            append(form, "payment_method_types[]", "card");
            append(form, "success_url", successUrl);
            append(form, "cancel_url", cancelUrl);

            // Add line items
            for (int i = 0; i < cart.size(); i++) {
                Map<String, Object> item = cart.get(i);
                String name = String.valueOf(item.getOrDefault("name", "Item"));
                double price = Double.parseDouble(String.valueOf(item.getOrDefault("price", 0)));
                int quantity = Integer.parseInt(String.valueOf(item.getOrDefault("quantity", 1)));

                long unitAmount = Math.round(price * 100); // Convert to cents

                append(form, String.format("line_items[%d][price_data][currency]", i), "usd");
                append(form, String.format("line_items[%d][price_data][product_data][name]", i), name);
                append(form, String.format("line_items[%d][price_data][unit_amount]", i), String.valueOf(unitAmount));
                append(form, String.format("line_items[%d][quantity]", i), String.valueOf(quantity));
            }

            // Make HTTP request to Stripe
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.stripe.com/v1/checkout/sessions"))
                    .header("Authorization", "Bearer " + stripeKey)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(form.toString()))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return objectMapper.readValue(response.body(), new TypeReference<Map<String, Object>>() {});
            } else {
                throw new StripeApiException("Stripe API error: " + response.body(), response.statusCode(), response.body());
            }
        } catch (StripeApiException e) {
            // Re-throw StripeApiException as is
            throw e;
        } catch (Exception e) {
            // Wrap other exceptions in StripeApiException
            throw new StripeApiException("Failed to create Stripe session: " + e.getMessage(), e);
        }
    }


    private void append(StringBuilder sb, String key, String value) {
        if (!sb.isEmpty()) sb.append('&');
        sb.append(URLEncoder.encode(key, StandardCharsets.UTF_8))
          .append('=')
          .append(URLEncoder.encode(value, StandardCharsets.UTF_8));
    }
}
