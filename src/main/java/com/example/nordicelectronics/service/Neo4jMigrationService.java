package com.example.nordicelectronics.service;

import com.example.nordicelectronics.entity.*;
import com.example.nordicelectronics.entity.neo4j.*;
import com.example.nordicelectronics.repositories.neo4j.*;
import com.example.nordicelectronics.repositories.sql.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class Neo4jMigrationService {

    // PostgreSQL Repositories
    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final BrandRepository brandRepository;
    private final CategoryRepository categoryRepository;
    private final WarrantyRepository warrantyRepository;
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;
    private final CouponRepository couponRepository;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final ReviewRepository reviewRepository;

    // Neo4j Repositories
    private final UserNeo4jRepository userNeo4jRepository;
    private final AddressNeo4jRepository addressNeo4jRepository;
    private final BrandNeo4jRepository brandNeo4jRepository;
    private final CategoryNeo4jRepository categoryNeo4jRepository;
    private final WarrantyNeo4jRepository warrantyNeo4jRepository;
    private final ProductNeo4jRepository productNeo4jRepository;
    private final WarehouseNeo4jRepository warehouseNeo4jRepository;
    private final CouponNeo4jRepository couponNeo4jRepository;
    private final OrderNeo4jRepository orderNeo4jRepository;
    private final PaymentNeo4jRepository paymentNeo4jRepository;
    private final ReviewNeo4jRepository reviewNeo4jRepository;

    public Map<String, Object> migrateAllDataToNeo4j() {
        log.info("Starting full database migration from PostgreSQL to Neo4j");

        Map<String, Object> results = new HashMap<>();
        long startTime = System.currentTimeMillis();

        try {
            // Clear existing Neo4j data
            clearNeo4jData();
            results.put("cleared", "Successfully cleared existing Neo4j data");

            // Migrate in order of dependencies (simple nodes first, then nodes with relationships)
            int brandsCount = migrateBrands();
            results.put("brands", brandsCount);

            int categoriesCount = migrateCategories();
            results.put("categories", categoriesCount);

            int warrantiesCount = migrateWarranties();
            results.put("warranties", warrantiesCount);

            int usersCount = migrateUsers();
            results.put("users", usersCount);

            int addressesCount = migrateAddresses();
            results.put("addresses", addressesCount);

            int couponsCount = migrateCoupons();
            results.put("coupons", couponsCount);

            int productsCount = migrateProducts();
            results.put("products", productsCount);

            int warehousesCount = migrateWarehouses();
            results.put("warehouses", warehousesCount);

            int ordersCount = migrateOrders();
            results.put("orders", ordersCount);

            int paymentsCount = migratePayments();
            results.put("payments", paymentsCount);

            int reviewsCount = migrateReviews();
            results.put("reviews", reviewsCount);

            long duration = System.currentTimeMillis() - startTime;
            results.put("duration_ms", duration);
            results.put("status", "SUCCESS");

            log.info("Neo4j migration completed successfully in {} ms", duration);
            return results;

        } catch (Exception e) {
            log.error("Neo4j migration failed: {}", e.getMessage(), e);
            results.put("status", "FAILED");
            results.put("error", e.getMessage());
            throw new RuntimeException("Neo4j migration failed: " + e.getMessage(), e);
        }
    }

    private void clearNeo4jData() {
        log.info("Clearing existing Neo4j data");
        reviewNeo4jRepository.deleteAll();
        paymentNeo4jRepository.deleteAll();
        orderNeo4jRepository.deleteAll();
        couponNeo4jRepository.deleteAll();
        warehouseNeo4jRepository.deleteAll();
        productNeo4jRepository.deleteAll();
        addressNeo4jRepository.deleteAll();
        userNeo4jRepository.deleteAll();
        warrantyNeo4jRepository.deleteAll();
        categoryNeo4jRepository.deleteAll();
        brandNeo4jRepository.deleteAll();
        log.info("Neo4j data cleared");
    }

    private int migrateBrands() {
        log.info("Migrating brands to Neo4j...");
        List<Brand> brands = brandRepository.findAll();
        List<BrandNode> brandNodes = brands.stream()
                .map(this::convertToBrandNode)
                .collect(Collectors.toList());
        brandNeo4jRepository.saveAll(brandNodes);
        log.info("Migrated {} brands to Neo4j", brandNodes.size());
        return brandNodes.size();
    }

    private int migrateCategories() {
        log.info("Migrating categories to Neo4j...");
        List<Category> categories = categoryRepository.findAll();
        List<CategoryNode> categoryNodes = categories.stream()
                .map(this::convertToCategoryNode)
                .collect(Collectors.toList());
        categoryNeo4jRepository.saveAll(categoryNodes);
        log.info("Migrated {} categories to Neo4j", categoryNodes.size());
        return categoryNodes.size();
    }

    private int migrateWarranties() {
        log.info("Migrating warranties to Neo4j...");
        List<Warranty> warranties = warrantyRepository.findAll();
        List<WarrantyNode> warrantyNodes = warranties.stream()
                .map(this::convertToWarrantyNode)
                .collect(Collectors.toList());
        warrantyNeo4jRepository.saveAll(warrantyNodes);
        log.info("Migrated {} warranties to Neo4j", warrantyNodes.size());
        return warrantyNodes.size();
    }

    private int migrateUsers() {
        log.info("Migrating users to Neo4j...");
        List<User> users = userRepository.findAll();
        List<UserNode> userNodes = users.stream()
                .map(this::convertToUserNode)
                .collect(Collectors.toList());
        userNeo4jRepository.saveAll(userNodes);
        log.info("Migrated {} users to Neo4j", userNodes.size());
        return userNodes.size();
    }

    private int migrateAddresses() {
        log.info("Migrating addresses to Neo4j...");
        List<Address> addresses = addressRepository.findAll();
        List<AddressNode> addressNodes = addresses.stream()
                .map(this::convertToAddressNode)
                .collect(Collectors.toList());
        addressNeo4jRepository.saveAll(addressNodes);
        log.info("Migrated {} addresses to Neo4j", addressNodes.size());
        return addressNodes.size();
    }

    private int migrateCoupons() {
        log.info("Migrating coupons to Neo4j...");
        List<Coupon> coupons = couponRepository.findAll();
        List<CouponNode> couponNodes = coupons.stream()
                .map(this::convertToCouponNode)
                .collect(Collectors.toList());
        couponNeo4jRepository.saveAll(couponNodes);
        log.info("Migrated {} coupons to Neo4j", couponNodes.size());
        return couponNodes.size();
    }

    private int migrateProducts() {
        log.info("Migrating products to Neo4j...");
        List<Product> products = productRepository.findAll();
        List<ProductNode> productNodes = products.stream()
                .map(this::convertToProductNode)
                .collect(Collectors.toList());
        productNeo4jRepository.saveAll(productNodes);
        log.info("Migrated {} products to Neo4j", productNodes.size());
        return productNodes.size();
    }

    private int migrateWarehouses() {
        log.info("Migrating warehouses to Neo4j...");
        List<Warehouse> warehouses = warehouseRepository.findAll();
        List<WarehouseNode> warehouseNodes = warehouses.stream()
                .map(this::convertToWarehouseNode)
                .collect(Collectors.toList());
        warehouseNeo4jRepository.saveAll(warehouseNodes);
        log.info("Migrated {} warehouses to Neo4j", warehouseNodes.size());
        return warehouseNodes.size();
    }

    private int migrateOrders() {
        log.info("Migrating orders to Neo4j...");
        List<Order> orders = orderRepository.findAll();
        List<OrderNode> orderNodes = orders.stream()
                .map(this::convertToOrderNode)
                .collect(Collectors.toList());
        orderNeo4jRepository.saveAll(orderNodes);
        log.info("Migrated {} orders to Neo4j", orderNodes.size());
        return orderNodes.size();
    }

    private int migratePayments() {
        log.info("Migrating payments to Neo4j...");
        List<Payment> payments = paymentRepository.findAll();
        List<PaymentNode> paymentNodes = payments.stream()
                .map(this::convertToPaymentNode)
                .collect(Collectors.toList());
        paymentNeo4jRepository.saveAll(paymentNodes);
        log.info("Migrated {} payments to Neo4j", paymentNodes.size());
        return paymentNodes.size();
    }

    private int migrateReviews() {
        log.info("Migrating reviews to Neo4j...");
        List<Review> reviews = reviewRepository.findAll();
        List<ReviewNode> reviewNodes = reviews.stream()
                .map(this::convertToReviewNode)
                .collect(Collectors.toList());
        reviewNeo4jRepository.saveAll(reviewNodes);
        log.info("Migrated {} reviews to Neo4j", reviewNodes.size());
        return reviewNodes.size();
    }

    // Conversion methods
    private BrandNode convertToBrandNode(Brand brand) {
        BrandNode node = BrandNode.builder()
                .brandId(brand.getBrandId())
                .name(brand.getName())
                .description(brand.getDescription())
                .build();
        node.setCreatedAt(brand.getCreatedAt());
        node.setUpdatedAt(brand.getUpdatedAt());
        node.setDeletedAt(brand.getDeletedAt());
        return node;
    }

    private CategoryNode convertToCategoryNode(Category category) {
        CategoryNode node = CategoryNode.builder()
                .categoryId(category.getCategoryId())
                .name(category.getName())
                .description(category.getDescription())
                .build();
        node.setCreatedAt(category.getCreatedAt());
        node.setUpdatedAt(category.getUpdatedAt());
        node.setDeletedAt(category.getDeletedAt());
        return node;
    }

    private WarrantyNode convertToWarrantyNode(Warranty warranty) {
        WarrantyNode node = WarrantyNode.builder()
                .warrantyId(warranty.getWarrantyId())
                .startDate(warranty.getStartDate())
                .endDate(warranty.getEndDate())
                .description(warranty.getDescription())
                .productId(warranty.getProduct() != null ? warranty.getProduct().getProductId() : null)
                .build();
        node.setCreatedAt(warranty.getCreatedAt());
        node.setUpdatedAt(warranty.getUpdatedAt());
        node.setDeletedAt(warranty.getDeletedAt());
        return node;
    }

    private UserNode convertToUserNode(User user) {
        UserNode node = UserNode.builder()
                .userId(user.getUserId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .dateOfBirth(user.getDateOfBirth())
                .password(user.getPassword())
                .isAdmin(user.isAdmin())
                .build();
        node.setCreatedAt(user.getCreatedAt());
        node.setUpdatedAt(user.getUpdatedAt());
        node.setDeletedAt(user.getDeletedAt());
        return node;
    }

    private AddressNode convertToAddressNode(Address address) {
        AddressNode node = AddressNode.builder()
                .addressId(address.getAddressId())
                .userId(address.getUser() != null ? address.getUser().getUserId() : null)
                .street(address.getStreet())
                .streetNumber(address.getStreetNumber())
                .zip(address.getZip())
                .city(address.getCity())
                .build();
        node.setCreatedAt(address.getCreatedAt());
        node.setUpdatedAt(address.getUpdatedAt());
        node.setDeletedAt(address.getDeletedAt());
        return node;
    }

    private CouponNode convertToCouponNode(Coupon coupon) {
        CouponNode node = CouponNode.builder()
                .couponId(coupon.getCouponId())
                .code(coupon.getCode())
                .discountType(coupon.getDiscountType())
                .discountValue(coupon.getDiscountValue())
                .minimumOrderValue(coupon.getMinimumOrderValue())
                .expiryDate(coupon.getExpiryDate())
                .usageLimit(coupon.getUsageLimit())
                .timesUsed(coupon.getTimesUsed())
                .isActive(coupon.isActive())
                .build();
        node.setCreatedAt(coupon.getCreatedAt());
        node.setUpdatedAt(coupon.getUpdatedAt());
        node.setDeletedAt(coupon.getDeletedAt());
        return node;
    }

    private ProductNode convertToProductNode(Product product) {
        ProductNode node = ProductNode.builder()
                .productId(product.getProductId())
                .sku(product.getSku())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .weight(product.getWeight())
                .warrantyId(product.getWarranty() != null ? product.getWarranty().getWarrantyId() : null)
                .brandId(product.getBrand() != null ? product.getBrand().getBrandId() : null)
                .build();
        node.setCreatedAt(product.getCreatedAt());
        node.setUpdatedAt(product.getUpdatedAt());
        node.setDeletedAt(product.getDeletedAt());
        return node;
    }

    private WarehouseNode convertToWarehouseNode(Warehouse warehouse) {
        WarehouseNode node = WarehouseNode.builder()
                .warehouseId(warehouse.getWarehouseId())
                .name(warehouse.getName())
                .phoneNumber(warehouse.getPhoneNumber())
                .addressId(warehouse.getAddress() != null ? warehouse.getAddress().getAddressId() : null)
                .build();
        node.setCreatedAt(warehouse.getCreatedAt());
        node.setUpdatedAt(warehouse.getUpdatedAt());
        node.setDeletedAt(warehouse.getDeletedAt());
        return node;
    }

    private OrderNode convertToOrderNode(Order order) {
        OrderNode node = OrderNode.builder()
                .orderId(order.getOrderId())
                .userId(order.getUser() != null ? order.getUser().getUserId() : null)
                .paymentId(order.getPayment() != null ? order.getPayment().getPaymentId() : null)
                .addressId(order.getAddress() != null ? order.getAddress().getAddressId() : null)
                .orderDate(order.getOrderDate())
                .orderStatus(order.getOrderStatus())
                .totalAmount(order.getTotalAmount())
                .subtotal(order.getSubtotal())
                .taxAmount(order.getTaxAmount())
                .shippingCost(order.getShippingCost())
                .discountAmount(order.getDiscountAmount())
                .couponId(order.getCoupon() != null ? order.getCoupon().getCouponId() : null)
                .build();
        node.setCreatedAt(order.getCreatedAt());
        node.setUpdatedAt(order.getUpdatedAt());
        node.setDeletedAt(order.getDeletedAt());
        return node;
    }

    private PaymentNode convertToPaymentNode(Payment payment) {
        PaymentNode node = PaymentNode.builder()
                .paymentId(payment.getPaymentId())
                .orderId(payment.getOrder() != null ? payment.getOrder().getOrderId() : null)
                .paymentMethod(payment.getPaymentMethod())
                .paymentStatus(payment.getPaymentStatus())
                .paymentDate(payment.getPaymentDate())
                .amount(payment.getAmount())
                .build();
        node.setCreatedAt(payment.getCreatedAt());
        node.setUpdatedAt(payment.getUpdatedAt());
        node.setDeletedAt(payment.getDeletedAt());
        return node;
    }

    private ReviewNode convertToReviewNode(Review review) {
        ReviewNode node = ReviewNode.builder()
                .reviewId(review.getReviewId())
                .userId(review.getUser() != null ? review.getUser().getUserId() : null)
                .orderId(review.getOrderId())
                .reviewValue(review.getReviewValue())
                .title(review.getTitle())
                .comment(review.getComment())
                .isVerifiedPurchase(review.isVerifiedPurchase())
                .productId(review.getProduct() != null ? review.getProduct().getProductId() : null)
                .build();
        node.setCreatedAt(review.getCreatedAt());
        node.setUpdatedAt(review.getUpdatedAt());
        node.setDeletedAt(review.getDeletedAt());
        return node;
    }
}

