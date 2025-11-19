package com.example.nordicelectronics.service;

import com.example.nordicelectronics.document.*;
import com.example.nordicelectronics.entity.*;
import com.example.nordicelectronics.repositories.mongodb.*;
import com.example.nordicelectronics.repositories.sql.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MigrationService {

    // PostgreSQL Repositories
    private final BrandRepository brandRepository;
    private final CategoryRepository categoryRepository;
    private final WarrantyRepository warrantyRepository;
    private final CouponRepository couponRepository;
    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;
    private final WarehouseProductRepository warehouseProductRepository;
    private final OrderRepository orderRepository;
    private final OrderProductRepository orderProductRepository;
    private final OrderCouponRepository orderCouponRepository;
    private final PaymentRepository paymentRepository;
    private final ReviewRepository reviewRepository;

    // MongoDB Repositories
    private final BrandMongoRepository brandMongoRepository;
    private final CategoryMongoRepository categoryMongoRepository;
    private final WarrantyMongoRepository warrantyMongoRepository;
    private final CouponMongoRepository couponMongoRepository;
    private final UserMongoRepository userMongoRepository;
    private final ProductMongoRepository productMongoRepository;
    private final WarehouseMongoRepository warehouseMongoRepository;
    private final OrderMongoRepository orderMongoRepository;
    private final PaymentMongoRepository paymentMongoRepository;
    private final ReviewMongoRepository reviewMongoRepository;

    /**
     * Main migration method that orchestrates the entire migration process
     */
    @Transactional(readOnly = true)
    public void migrateAll() {
        log.info("Starting migration from PostgreSQL to MongoDB...");

        try {
            // Clear existing MongoDB data (optional - comment out if you want to keep existing data)
            clearMongoDBCollections();

            // Step 1: Migrate simple entities (no dependencies)
            Map<String, String> brandIdMap = migrateBrands();
            Map<String, String> categoryIdMap = migrateCategories();
            Map<String, String> warrantyIdMap = migrateWarranties();
            Map<String, String> couponIdMap = migrateCoupons();

            // Step 2: Migrate users with embedded addresses
            Map<String, String> userIdMap = migrateUsers();

            // Step 3: Migrate products with embedded brand, warranty, and category info
            Map<String, String> productIdMap = migrateProducts(brandIdMap, warrantyIdMap, categoryIdMap);

            // Step 4: Migrate warehouses with embedded product info
            migrateWarehouses(productIdMap);

            // Step 5: Migrate orders with embedded products, coupons, and payment info
            migrateOrders(userIdMap, productIdMap, couponIdMap);

            // Step 6: Migrate reviews
            migrateReviews(userIdMap, productIdMap);

            log.info("Migration completed successfully!");
        } catch (Exception e) {
            log.error("Migration failed: {}", e.getMessage(), e);
            throw new RuntimeException("Migration failed", e);
        }
    }

    /**
     * Clear all MongoDB collections
     */
    private void clearMongoDBCollections() {
        log.info("Clearing MongoDB collections...");
        brandMongoRepository.deleteAll();
        categoryMongoRepository.deleteAll();
        warrantyMongoRepository.deleteAll();
        couponMongoRepository.deleteAll();
        userMongoRepository.deleteAll();
        productMongoRepository.deleteAll();
        warehouseMongoRepository.deleteAll();
        orderMongoRepository.deleteAll();
        paymentMongoRepository.deleteAll();
        reviewMongoRepository.deleteAll();
        log.info("MongoDB collections cleared.");
    }

    /**
     * Migrate brands from PostgreSQL to MongoDB
     * @return Map of old UUID to new MongoDB ID
     */
    private Map<String, String> migrateBrands() {
        log.info("Migrating brands...");
        Map<String, String> idMap = new HashMap<>();
        List<Brand> brands = brandRepository.findAll();

        for (Brand brand : brands) {
            BrandDocument document = BrandDocument.builder()
                    .name(brand.getName())
                    .description(brand.getDescription())
                    .build();

            BrandDocument saved = brandMongoRepository.save(document);
            idMap.put(brand.getBrandId().toString(), saved.getId());
            log.debug("Migrated brand: {} -> {}", brand.getName(), saved.getId());
        }

        log.info("Migrated {} brands", brands.size());
        return idMap;
    }

    /**
     * Migrate categories from PostgreSQL to MongoDB
     * @return Map of old UUID to new MongoDB ID
     */
    private Map<String, String> migrateCategories() {
        log.info("Migrating categories...");
        Map<String, String> idMap = new HashMap<>();
        List<Category> categories = categoryRepository.findAll();

        for (Category category : categories) {
            CategoryDocument document = CategoryDocument.builder()
                    .name(category.getName())
                    .description(category.getDescription())
                    .build();

            CategoryDocument saved = categoryMongoRepository.save(document);
            idMap.put(category.getCategoryId().toString(), saved.getId());
            log.debug("Migrated category: {} -> {}", category.getName(), saved.getId());
        }

        log.info("Migrated {} categories", categories.size());
        return idMap;
    }

    /**
     * Migrate warranties from PostgreSQL to MongoDB
     * @return Map of old UUID to new MongoDB ID
     */
    private Map<String, String> migrateWarranties() {
        log.info("Migrating warranties...");
        Map<String, String> idMap = new HashMap<>();
        List<Warranty> warranties = warrantyRepository.findAll();

        for (Warranty warranty : warranties) {
            WarrantyDocument document = WarrantyDocument.builder()
                    .startDate(warranty.getStartDate())
                    .endDate(warranty.getEndDate())
                    .description(warranty.getDescription())
                    .build();

            WarrantyDocument saved = warrantyMongoRepository.save(document);
            idMap.put(warranty.getWarrantyId().toString(), saved.getId());
            log.debug("Migrated warranty: {} -> {}", warranty.getWarrantyId(), saved.getId());
        }

        log.info("Migrated {} warranties", warranties.size());
        return idMap;
    }

    /**
     * Migrate coupons from PostgreSQL to MongoDB
     * @return Map of old UUID to new MongoDB ID
     */
    private Map<String, String> migrateCoupons() {
        log.info("Migrating coupons...");
        Map<String, String> idMap = new HashMap<>();
        List<Coupon> coupons = couponRepository.findAll();

        for (Coupon coupon : coupons) {
            CouponDocument document = CouponDocument.builder()
                    .code(coupon.getCode())
                    .discountType(coupon.getDiscountType().name().toLowerCase())
                    .discountValue(coupon.getDiscountValue())
                    .minimumOrderValue(coupon.getMinimumOrderValue())
                    .expiryDate(coupon.getExpiryDate())
                    .usageLimit(coupon.getUsageLimit())
                    .timesUsed(coupon.getTimesUsed())
                    .isActive(coupon.isActive())
                    .build();

            CouponDocument saved = couponMongoRepository.save(document);
            idMap.put(coupon.getCouponId().toString(), saved.getId());
            log.debug("Migrated coupon: {} -> {}", coupon.getCode(), saved.getId());
        }

        log.info("Migrated {} coupons", coupons.size());
        return idMap;
    }

    /**
     * Migrate users with embedded addresses from PostgreSQL to MongoDB
     * @return Map of old UUID to new MongoDB ID
     */
    private Map<String, String> migrateUsers() {
        log.info("Migrating users...");
        Map<String, String> idMap = new HashMap<>();
        List<User> users = userRepository.findAll();

        for (User user : users) {
            UserDocument.AddressInfo addressInfo = null;
            
            // Embed address if it exists
            if (user.getAddress() != null) {
                Address address = user.getAddress();
                addressInfo = UserDocument.AddressInfo.builder()
                        .street(address.getStreet())
                        .streetNumber(address.getStreetNumber())
                        .zip(address.getZip())
                        .city(address.getCity())
                        .build();
            }

            UserDocument document = UserDocument.builder()
                    .email(user.getEmail())
                    .password(user.getPassword())
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                    .phoneNumber(user.getPhoneNumber())
                    .dateOfBirth(user.getDateOfBirth())
                    .isAdmin(user.isAdmin())
                    .address(addressInfo)
                    .build();

            UserDocument saved = userMongoRepository.save(document);
            idMap.put(user.getUserId().toString(), saved.getId());
            log.debug("Migrated user: {} -> {}", user.getEmail(), saved.getId());
        }

        log.info("Migrated {} users", users.size());
        return idMap;
    }

    /**
     * Migrate products with embedded brand, warranty, and category info
     * @return Map of old UUID to new MongoDB ID
     */
    private Map<String, String> migrateProducts(
            Map<String, String> brandIdMap,
            Map<String, String> warrantyIdMap,
            Map<String, String> categoryIdMap) {
        log.info("Migrating products...");
        Map<String, String> idMap = new HashMap<>();
        List<Product> products = productRepository.findAll();

        for (Product product : products) {
            // Prepare warranty info
            ProductDocument.WarrantyInfo warrantyInfo = null;
            String warrantyMongoId = null;
            if (product.getWarranty() != null) {
                Warranty warranty = product.getWarranty();
                warrantyMongoId = warrantyIdMap.get(warranty.getWarrantyId().toString());
                warrantyInfo = ProductDocument.WarrantyInfo.builder()
                        .startDate(warranty.getStartDate().toString())
                        .endDate(warranty.getEndDate().toString())
                        .description(warranty.getDescription())
                        .build();
            }

            // Prepare category info
            List<String> categoryIds = new ArrayList<>();
            List<ProductDocument.CategoryInfo> categories = new ArrayList<>();
            if (product.getCategories() != null && !product.getCategories().isEmpty()) {
                for (Category category : product.getCategories()) {
                    String categoryMongoId = categoryIdMap.get(category.getCategoryId().toString());
                    if (categoryMongoId != null) {
                        categoryIds.add(categoryMongoId);
                        categories.add(ProductDocument.CategoryInfo.builder()
                                .id(categoryMongoId)
                                .name(category.getName())
                                .build());
                    }
                }
            }

            // Get brand info
            String brandMongoId = null;
            String brandName = null;
            if (product.getBrand() != null) {
                brandMongoId = brandIdMap.get(product.getBrand().getBrandId().toString());
                brandName = product.getBrand().getName();
            }

            ProductDocument document = ProductDocument.builder()
                    .sku(product.getSku())
                    .name(product.getName())
                    .description(product.getDescription())
                    .price(product.getPrice())
                    .weight(product.getWeight())
                    .brandId(brandMongoId)
                    .brandName(brandName)
                    .warrantyId(warrantyMongoId)
                    .warranty(warrantyInfo)
                    .categoryIds(categoryIds)
                    .categories(categories)
                    .build();

            ProductDocument saved = productMongoRepository.save(document);
            idMap.put(product.getProductId().toString(), saved.getId());
            log.debug("Migrated product: {} -> {}", product.getName(), saved.getId());
        }

        log.info("Migrated {} products", products.size());
        return idMap;
    }

    /**
     * Migrate warehouses with embedded product info
     */
    private void migrateWarehouses(Map<String, String> productIdMap) {
        log.info("Migrating warehouses...");
        List<Warehouse> warehouses = warehouseRepository.findAll();

        for (Warehouse warehouse : warehouses) {
            // Get all warehouse products for this warehouse
            List<WarehouseDocument.WarehouseProductInfo> productInfoList = new ArrayList<>();
            
            if (warehouse.getWarehouseProducts() != null) {
                for (WarehouseProduct wp : warehouse.getWarehouseProducts()) {
                    String productMongoId = productIdMap.get(wp.getProduct().getProductId().toString());
                    if (productMongoId != null) {
                        productInfoList.add(WarehouseDocument.WarehouseProductInfo.builder()
                                .productId(productMongoId)
                                .stockQuantity(wp.getStockQuantity())
                                .build());
                    }
                }
            }

            WarehouseDocument document = WarehouseDocument.builder()
                    .name(warehouse.getName())
                    .address(warehouse.getAddress())
                    .city(warehouse.getCity())
                    .postalCode(warehouse.getPostalCode())
                    .country(warehouse.getCountry())
                    .phone(warehouse.getPhone())
                    .products(productInfoList)
                    .build();

            warehouseMongoRepository.save(document);
            log.debug("Migrated warehouse: {}", warehouse.getName());
        }

        log.info("Migrated {} warehouses", warehouses.size());
    }

    /**
     * Migrate orders with embedded products, coupons, and payment info
     */
    private void migrateOrders(
            Map<String, String> userIdMap,
            Map<String, String> productIdMap,
            Map<String, String> couponIdMap) {
        log.info("Migrating orders...");
        List<Order> orders = orderRepository.findAll();

        for (Order order : orders) {
            String userMongoId = userIdMap.get(order.getUser().getUserId().toString());

            // Get order products
            List<OrderDocument.OrderProductInfo> orderProducts = orderProductRepository.findAll().stream()
                    .filter(op -> op.getOrderId().equals(order.getOrderId()))
                    .map(op -> {
                        String productMongoId = productIdMap.get(op.getProductId().toString());
                        String productName = op.getProduct() != null ? op.getProduct().getName() : "Unknown";
                        return OrderDocument.OrderProductInfo.builder()
                                .productId(productMongoId)
                                .productName(productName)
                                .quantity(op.getQuantity())
                                .unitPrice(op.getUnitPrice())
                                .totalPrice(op.getTotalPrice())
                                .build();
                    })
                    .collect(Collectors.toList());

            // Get order coupons
            List<String> orderCouponIds = orderCouponRepository.findAll().stream()
                    .filter(oc -> oc.getOrderId().equals(order.getOrderId()))
                    .map(oc -> couponIdMap.get(oc.getCouponId().toString()))
                    .collect(Collectors.toList());

            // Get payment info
            OrderDocument.PaymentInfo paymentInfo = null;
            if (order.getPayment() != null) {
                Payment payment = order.getPayment();
                paymentInfo = OrderDocument.PaymentInfo.builder()
                        .paymentId(payment.getPaymentId().toString())
                        .paymentMethod(payment.getPaymentMethod().name().toLowerCase())
                        .status(payment.getPaymentStatus().name().toLowerCase())
                        .amount(payment.getAmount())
                        .paymentDate(payment.getPaymentDate())
                        .build();
            }

            OrderDocument document = OrderDocument.builder()
                    .userId(userMongoId)
                    .orderDate(order.getOrderDate())
                    .status(order.getOrderStatus().name().toLowerCase())
                    .totalAmount(order.getTotalAmount())
                    .subtotal(order.getSubtotal())
                    .taxAmount(order.getTaxAmount())
                    .shippingCost(order.getShippingCost())
                    .discountAmount(order.getDiscountAmount())
                    .products(orderProducts)
                    .couponIds(orderCouponIds)
                    .payment(paymentInfo)
                    .build();

            orderMongoRepository.save(document);
            log.debug("Migrated order: {}", order.getOrderId());
        }

        log.info("Migrated {} orders", orders.size());
    }

    /**
     * Migrate reviews
     */
    private void migrateReviews(Map<String, String> userIdMap, Map<String, String> productIdMap) {
        log.info("Migrating reviews...");
        List<Review> reviews = reviewRepository.findAll();

        for (Review review : reviews) {
            String userMongoId = userIdMap.get(review.getUser().getUserId().toString());
            String productMongoId = productIdMap.get(review.getProductId().toString());

            ReviewDocument document = ReviewDocument.builder()
                    .productId(productMongoId)
                    .userId(userMongoId)
                    .orderId(review.getOrderId().toString())
                    .reviewValue(review.getReviewValue())
                    .title(review.getTitle())
                    .comment(review.getComment())
                    .isVerifiedPurchase(review.isVerifiedPurchase())
                    .createdAt(review.getCreatedAt())
                    .build();

            reviewMongoRepository.save(document);
            log.debug("Migrated review: {}", review.getReviewId());
        }

        log.info("Migrated {} reviews", reviews.size());
    }
}
