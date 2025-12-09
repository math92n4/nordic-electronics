package com.example.nordicelectronics.service;

import com.example.nordicelectronics.entity.*;
import com.example.nordicelectronics.entity.neo4j.*;
import com.example.nordicelectronics.repositories.neo4j.*;
import com.example.nordicelectronics.repositories.sql.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
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

            // Phase 1: Migrate independent nodes (no relationships needed)
            int brandsCount = migrateBrands();
            results.put("brands", brandsCount);

            int categoriesCount = migrateCategories();
            results.put("categories", categoriesCount);

            int warrantiesCount = migrateWarranties();
            results.put("warranties", warrantiesCount);

            int usersCount = migrateUsers();
            results.put("users", usersCount);

            int couponsCount = migrateCoupons();
            results.put("coupons", couponsCount);

            // Phase 2: Migrate addresses (depends on User)
            int addressesCount = migrateAddressesWithRelationships();
            results.put("addresses", addressesCount);

            // Phase 3: Migrate warehouses (depends on Address)
            int warehousesCount = migrateWarehousesWithRelationships();
            results.put("warehouses", warehousesCount);

            // Phase 4: Migrate products (depends on Brand, Category, Warranty, Warehouse)
            int productsCount = migrateProductsWithRelationships();
            results.put("products", productsCount);

            // Phase 5: Migrate orders (depends on User, Address, Coupon, Product)
            int ordersCount = migrateOrdersWithRelationships();
            results.put("orders", ordersCount);

            // Phase 6: Migrate payments and reviews (depend on Order/Product)
            int paymentsCount = migratePaymentsWithRelationships();
            results.put("payments", paymentsCount);

            int reviewsCount = migrateReviewsWithRelationships();
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

    // ==================== Phase 1: Independent nodes ====================

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

    // ==================== Phase 2: Addresses (depends on User) ====================

    private int migrateAddressesWithRelationships() {
        log.info("Migrating addresses with relationships to Neo4j...");
        List<Address> addresses = addressRepository.findAll();
        List<AddressNode> addressNodes = new ArrayList<>();

        for (Address address : addresses) {
            AddressNode node = convertToAddressNode(address);

            // Set relationship to User
            if (address.getUser() != null) {
                userNeo4jRepository.findByUserId(address.getUser().getUserId())
                        .ifPresent(node::setUser);
            }

            addressNodes.add(node);
        }

        addressNeo4jRepository.saveAll(addressNodes);
        log.info("Migrated {} addresses with relationships to Neo4j", addressNodes.size());
        return addressNodes.size();
    }

    // ==================== Phase 3: Warehouses (depends on Address) ====================

    private int migrateWarehousesWithRelationships() {
        log.info("Migrating warehouses with relationships to Neo4j...");
        List<Warehouse> warehouses = warehouseRepository.findAll();
        List<WarehouseNode> warehouseNodes = new ArrayList<>();

        for (Warehouse warehouse : warehouses) {
            WarehouseNode node = convertToWarehouseNode(warehouse);

            // Set relationship to Address
            if (warehouse.getAddress() != null) {
                addressNeo4jRepository.findByAddressId(warehouse.getAddress().getAddressId())
                        .ifPresent(node::setAddress);
            }

            warehouseNodes.add(node);
        }

        warehouseNeo4jRepository.saveAll(warehouseNodes);
        log.info("Migrated {} warehouses with relationships to Neo4j", warehouseNodes.size());
        return warehouseNodes.size();
    }

    // ==================== Phase 4: Products (depends on Brand, Category, Warranty, Warehouse) ====================

    private int migrateProductsWithRelationships() {
        log.info("Migrating products with relationships to Neo4j...");
        List<Product> products = productRepository.findAll();
        List<ProductNode> productNodes = new ArrayList<>();

        for (Product product : products) {
            ProductNode node = convertToProductNode(product);

            // Set relationship to Brand
            if (product.getBrand() != null) {
                brandNeo4jRepository.findByBrandId(product.getBrand().getBrandId())
                        .ifPresent(node::setBrand);
            }

            // Set relationship to Warranty
            if (product.getWarranty() != null) {
                warrantyNeo4jRepository.findByWarrantyId(product.getWarranty().getWarrantyId())
                        .ifPresent(node::setWarranty);
            }

            // Set relationships to Categories
            if (product.getCategories() != null && !product.getCategories().isEmpty()) {
                Set<CategoryNode> categoryNodes = new HashSet<>();
                for (Category category : product.getCategories()) {
                    categoryNeo4jRepository.findByCategoryId(category.getCategoryId())
                            .ifPresent(categoryNodes::add);
                }
                node.setCategories(categoryNodes);
            }

            // Set relationships to Warehouses (via WarehouseProductRelationship)
            if (product.getWarehouseProducts() != null && !product.getWarehouseProducts().isEmpty()) {
                List<WarehouseProductRelationship> warehouseRelationships = new ArrayList<>();
                for (WarehouseProduct wp : product.getWarehouseProducts()) {
                    warehouseNeo4jRepository.findByWarehouseId(wp.getWarehouse().getWarehouseId())
                            .ifPresent(warehouseNode -> {
                                WarehouseProductRelationship rel = WarehouseProductRelationship.builder()
                                        .warehouseId(wp.getWarehouse().getWarehouseId())
                                        .productId(product.getProductId())
                                        .stockQuantity(wp.getStockQuantity())
                                        .warehouse(warehouseNode)
                                        .build();
                                warehouseRelationships.add(rel);
                            });
                }
                node.setWarehouseProducts(warehouseRelationships);
            }

            productNodes.add(node);
        }

        productNeo4jRepository.saveAll(productNodes);
        log.info("Migrated {} products with relationships to Neo4j", productNodes.size());
        return productNodes.size();
    }

    // ==================== Phase 5: Orders (depends on User, Address, Coupon, Product) ====================

    private int migrateOrdersWithRelationships() {
        log.info("Migrating orders with relationships to Neo4j...");
        List<Order> orders = orderRepository.findAll();
        List<OrderNode> orderNodes = new ArrayList<>();

        for (Order order : orders) {
            OrderNode node = convertToOrderNode(order);

            // Set relationship to User
            if (order.getUser() != null) {
                userNeo4jRepository.findByUserId(order.getUser().getUserId())
                        .ifPresent(node::setUser);
            }

            // Set relationship to Address
            if (order.getAddress() != null) {
                addressNeo4jRepository.findByAddressId(order.getAddress().getAddressId())
                        .ifPresent(node::setAddress);
            }

            // Set relationship to Coupon
            if (order.getCoupon() != null) {
                couponNeo4jRepository.findByCouponId(order.getCoupon().getCouponId())
                        .ifPresent(node::setCoupon);
            }

            // Set relationships to Products (via OrderProductRelationship)
            if (order.getOrderProducts() != null && !order.getOrderProducts().isEmpty()) {
                List<OrderProductRelationship> productRelationships = new ArrayList<>();
                for (OrderProduct op : order.getOrderProducts()) {
                    productNeo4jRepository.findByProductId(op.getProduct().getProductId())
                            .ifPresent(productNode -> {
                                OrderProductRelationship rel = OrderProductRelationship.builder()
                                        .orderId(order.getOrderId())
                                        .productId(op.getProduct().getProductId())
                                        .quantity(op.getQuantity())
                                        .unitPrice(op.getUnitPrice())
                                        .totalPrice(op.getTotalPrice())
                                        .product(productNode)
                                        .build();
                                productRelationships.add(rel);
                            });
                }
                node.setOrderProducts(productRelationships);
            }

            orderNodes.add(node);
        }

        orderNeo4jRepository.saveAll(orderNodes);
        log.info("Migrated {} orders with relationships to Neo4j", orderNodes.size());
        return orderNodes.size();
    }

    // ==================== Phase 6: Payments and Reviews ====================

    private int migratePaymentsWithRelationships() {
        log.info("Migrating payments with relationships to Neo4j...");
        List<Payment> payments = paymentRepository.findAll();
        List<PaymentNode> paymentNodes = new ArrayList<>();

        for (Payment payment : payments) {
            PaymentNode node = convertToPaymentNode(payment);

            // Set relationship to Order
            if (payment.getOrder() != null) {
                orderNeo4jRepository.findByOrderId(payment.getOrder().getOrderId())
                        .ifPresent(node::setOrder);
            }

            paymentNodes.add(node);
        }

        paymentNeo4jRepository.saveAll(paymentNodes);
        log.info("Migrated {} payments with relationships to Neo4j", paymentNodes.size());
        return paymentNodes.size();
    }

    private int migrateReviewsWithRelationships() {
        log.info("Migrating reviews with relationships to Neo4j...");
        List<Review> reviews = reviewRepository.findAll();
        List<ReviewNode> reviewNodes = new ArrayList<>();

        for (Review review : reviews) {
            ReviewNode node = convertToReviewNode(review);

            // Set relationship to User
            if (review.getUser() != null) {
                userNeo4jRepository.findByUserId(review.getUser().getUserId())
                        .ifPresent(node::setUser);
            }

            // Set relationship to Product
            if (review.getProduct() != null) {
                productNeo4jRepository.findByProductId(review.getProduct().getProductId())
                        .ifPresent(node::setProduct);
            }

            reviewNodes.add(node);
        }

        reviewNeo4jRepository.saveAll(reviewNodes);
        log.info("Migrated {} reviews with relationships to Neo4j", reviewNodes.size());
        return reviewNodes.size();
    }

    // ==================== Conversion methods (properties only) ====================

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
