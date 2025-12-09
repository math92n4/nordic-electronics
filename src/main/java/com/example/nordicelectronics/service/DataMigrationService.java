package com.example.nordicelectronics.service;

import com.example.nordicelectronics.entity.*;
import com.example.nordicelectronics.entity.mongodb.*;
import com.example.nordicelectronics.repositories.mongodb.*;
import com.example.nordicelectronics.repositories.sql.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@ConditionalOnProperty(name = "spring.data.mongodb.enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
@Slf4j
public class DataMigrationService {

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

    // MongoDB Repositories
    private final UserMongoRepository userMongoRepository;
    private final AddressMongoRepository addressMongoRepository;
    private final BrandMongoRepository brandMongoRepository;
    private final CategoryMongoRepository categoryMongoRepository;
    private final WarrantyMongoRepository warrantyMongoRepository;
    private final ProductMongoRepository productMongoRepository;
    private final WarehouseMongoRepository warehouseMongoRepository;
    private final CouponMongoRepository couponMongoRepository;
    private final OrderMongoRepository orderMongoRepository;
    private final PaymentMongoRepository paymentMongoRepository;
    private final ReviewMongoRepository reviewMongoRepository;

    @Transactional(readOnly = true)
    public Map<String, Object> migrateAllData() {
        log.info("Starting full database migration from PostgreSQL to MongoDB (with embedded documents)");
        
        Map<String, Object> results = new HashMap<>();
        long startTime = System.currentTimeMillis();

        try {
            // Clear existing MongoDB data
            clearMongoDBData();
            results.put("cleared", "Successfully cleared existing MongoDB data");

            // Phase 1: Migrate standalone reference collections (still needed for some lookups)
            int brandsCount = migrateBrands();
            results.put("brands", brandsCount);
            
            int categoriesCount = migrateCategories();
            results.put("categories", categoriesCount);
            
            int warrantiesCount = migrateWarranties();
            results.put("warranties", warrantiesCount);

            int couponsCount = migrateCoupons();
            results.put("coupons", couponsCount);

            // Phase 2: Migrate users with embedded addresses
            int usersCount = migrateUsersWithEmbeddedAddresses();
            results.put("users", usersCount);

            // Keep addresses collection for backward compatibility (optional)
            int addressesCount = migrateAddresses();
            results.put("addresses", addressesCount);

            // Phase 3: Migrate products with embedded brand, categories, warranty
            int productsCount = migrateProductsWithEmbeddedData();
            results.put("products", productsCount);

            // Phase 4: Migrate warehouses with embedded address and products
            int warehousesCount = migrateWarehousesWithEmbeddedData();
            results.put("warehouses", warehousesCount);
            
            // Phase 5: Migrate orders with all embedded data
            int ordersCount = migrateOrdersWithEmbeddedData();
            results.put("orders", ordersCount);

            // Keep payments collection for backward compatibility (optional)
            int paymentsCount = migratePayments();
            results.put("payments", paymentsCount);

            // Keep reviews collection for backward compatibility (optional)
            int reviewsCount = migrateReviews();
            results.put("reviews", reviewsCount);

            // Phase 6: Update products with embedded reviews
            updateProductsWithReviews();
            results.put("products_with_reviews", "Updated");

            long duration = System.currentTimeMillis() - startTime;
            results.put("duration_ms", duration);
            results.put("status", "SUCCESS");
            
            log.info("Migration completed successfully in {} ms", duration);
            return results;
            
        } catch (Exception e) {
            log.error("Migration failed: {}", e.getMessage(), e);
            results.put("status", "FAILED");
            results.put("error", e.getMessage());
            throw new RuntimeException("Migration failed: " + e.getMessage(), e);
        }
    }

    private void clearMongoDBData() {
        log.info("Clearing existing MongoDB data");
        reviewMongoRepository.deleteAll();
        paymentMongoRepository.deleteAll();
        orderMongoRepository.deleteAll();
        couponMongoRepository.deleteAll();
        addressMongoRepository.deleteAll();
        userMongoRepository.deleteAll();
        warehouseMongoRepository.deleteAll();
        productMongoRepository.deleteAll();
        warrantyMongoRepository.deleteAll();
        categoryMongoRepository.deleteAll();
        brandMongoRepository.deleteAll();
        log.info("MongoDB data cleared");
    }

    // ==================== Phase 1: Standalone collections ====================

    private int migrateBrands() {
        log.info("Migrating brands...");
        List<Brand> brands = brandRepository.findAll();
        List<BrandDocument> brandDocuments = brands.stream()
                .map(this::convertToBrandDocument)
                .collect(Collectors.toList());
        brandMongoRepository.saveAll(brandDocuments);
        log.info("Migrated {} brands", brandDocuments.size());
        return brandDocuments.size();
    }

    private int migrateCategories() {
        log.info("Migrating categories...");
        List<Category> categories = categoryRepository.findAll();
        List<CategoryDocument> categoryDocuments = categories.stream()
                .map(this::convertToCategoryDocument)
                .collect(Collectors.toList());
        categoryMongoRepository.saveAll(categoryDocuments);
        log.info("Migrated {} categories", categoryDocuments.size());
        return categoryDocuments.size();
    }

    private int migrateWarranties() {
        log.info("Migrating warranties...");
        List<Warranty> warranties = warrantyRepository.findAll();
        List<WarrantyDocument> warrantyDocuments = warranties.stream()
                .map(this::convertToWarrantyDocument)
                .collect(Collectors.toList());
        warrantyMongoRepository.saveAll(warrantyDocuments);
        log.info("Migrated {} warranties", warrantyDocuments.size());
        return warrantyDocuments.size();
    }

    private int migrateCoupons() {
        log.info("Migrating coupons...");
        List<Coupon> coupons = couponRepository.findAll();
        List<CouponDocument> couponDocuments = coupons.stream()
                .map(this::convertToCouponDocument)
                .collect(Collectors.toList());
        couponMongoRepository.saveAll(couponDocuments);
        log.info("Migrated {} coupons", couponDocuments.size());
        return couponDocuments.size();
    }

    private int migrateAddresses() {
        log.info("Migrating addresses (standalone collection)...");
        List<Address> addresses = addressRepository.findAll();
        List<AddressDocument> addressDocuments = addresses.stream()
                .map(this::convertToAddressDocument)
                .collect(Collectors.toList());
        addressMongoRepository.saveAll(addressDocuments);
        log.info("Migrated {} addresses", addressDocuments.size());
        return addressDocuments.size();
    }

    private int migratePayments() {
        log.info("Migrating payments (standalone collection)...");
        List<Payment> payments = paymentRepository.findAll();
        List<PaymentDocument> paymentDocuments = payments.stream()
                .map(this::convertToPaymentDocument)
                .collect(Collectors.toList());
        paymentMongoRepository.saveAll(paymentDocuments);
        log.info("Migrated {} payments", paymentDocuments.size());
        return paymentDocuments.size();
    }

    private int migrateReviews() {
        log.info("Migrating reviews (standalone collection)...");
        List<Review> reviews = reviewRepository.findAll();
        List<ReviewDocument> reviewDocuments = reviews.stream()
                .map(this::convertToReviewDocument)
                .collect(Collectors.toList());
        reviewMongoRepository.saveAll(reviewDocuments);
        log.info("Migrated {} reviews", reviewDocuments.size());
        return reviewDocuments.size();
    }

    // ==================== Phase 2: Users with embedded addresses ====================

    private int migrateUsersWithEmbeddedAddresses() {
        log.info("Migrating users with embedded addresses...");
        List<User> users = userRepository.findAll();
        List<UserDocument> userDocuments = new ArrayList<>();

        for (User user : users) {
            UserDocument doc = UserDocument.builder()
                    .userId(user.getUserId())
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                    .email(user.getEmail())
                    .phoneNumber(user.getPhoneNumber())
                    .dateOfBirth(user.getDateOfBirth())
                    .password(user.getPassword())
                    .isAdmin(user.isAdmin())
                    // EMBEDDED: All addresses for this user
                    .addresses(user.getAddress().stream()
                            .map(this::convertToAddressEmbedded)
                            .collect(Collectors.toList()))
                    .orderIds(user.getOrders().stream()
                            .map(Order::getOrderId)
                            .collect(Collectors.toList()))
                    .build();
            doc.setCreatedAt(user.getCreatedAt());
            doc.setUpdatedAt(user.getUpdatedAt());
            doc.setDeletedAt(user.getDeletedAt());
            userDocuments.add(doc);
        }

        userMongoRepository.saveAll(userDocuments);
        log.info("Migrated {} users with embedded addresses", userDocuments.size());
        return userDocuments.size();
    }

    // ==================== Phase 3: Products with embedded data ====================

    private int migrateProductsWithEmbeddedData() {
        log.info("Migrating products with embedded brand, categories, warranty...");
        List<Product> products = productRepository.findAll();
        List<ProductDocument> productDocuments = new ArrayList<>();

        for (Product product : products) {
            ProductDocument doc = ProductDocument.builder()
                    .productId(product.getProductId())
                    .sku(product.getSku())
                    .name(product.getName())
                    .description(product.getDescription())
                    .price(product.getPrice())
                    .weight(product.getWeight())
                    // EMBEDDED: Brand info
                    .brand(product.getBrand() != null ? convertToBrandEmbedded(product.getBrand()) : null)
                    // EMBEDDED: All categories
                    .categories(product.getCategories().stream()
                            .map(this::convertToCategoryEmbedded)
                            .collect(Collectors.toList()))
                    // EMBEDDED: Warranty details
                    .warranty(product.getWarranty() != null ? convertToWarrantyEmbedded(product.getWarranty()) : null)
                    // EMBEDDED: Warehouse stock info
                    .warehouseStock(product.getWarehouseProducts().stream()
                            .map(wp -> WarehouseStockEmbedded.builder()
                                    .warehouseId(wp.getWarehouse().getWarehouseId())
                                    .warehouseName(wp.getWarehouse().getName())
                                    .stockQuantity(wp.getStockQuantity())
                                    .build())
                            .collect(Collectors.toList()))
                    // Reviews will be added in Phase 6
                    .reviews(new ArrayList<>())
                    .build();
            doc.setCreatedAt(product.getCreatedAt());
            doc.setUpdatedAt(product.getUpdatedAt());
            doc.setDeletedAt(product.getDeletedAt());
            productDocuments.add(doc);
        }

        productMongoRepository.saveAll(productDocuments);
        log.info("Migrated {} products with embedded data", productDocuments.size());
        return productDocuments.size();
    }

    // ==================== Phase 4: Warehouses with embedded data ====================

    private int migrateWarehousesWithEmbeddedData() {
        log.info("Migrating warehouses with embedded address and products...");
        List<Warehouse> warehouses = warehouseRepository.findAll();
        List<WarehouseDocument> warehouseDocuments = new ArrayList<>();

        for (Warehouse warehouse : warehouses) {
            WarehouseDocument doc = WarehouseDocument.builder()
                    .warehouseId(warehouse.getWarehouseId())
                    .name(warehouse.getName())
                    .phoneNumber(warehouse.getPhoneNumber())
                    // EMBEDDED: Address
                    .address(warehouse.getAddress() != null ? convertToAddressEmbedded(warehouse.getAddress()) : null)
                    // EMBEDDED: Products with stock and product details
                    .products(warehouse.getWarehouseProducts().stream()
                            .map(wp -> WarehouseProductEmbedded.builder()
                                    .productId(wp.getProduct().getProductId())
                                    .productName(wp.getProduct().getName())
                                    .productSku(wp.getProduct().getSku())
                                    .productPrice(wp.getProduct().getPrice())
                                    .stockQuantity(wp.getStockQuantity())
                                    .build())
                            .collect(Collectors.toList()))
                    .build();
            doc.setCreatedAt(warehouse.getCreatedAt());
            doc.setUpdatedAt(warehouse.getUpdatedAt());
            doc.setDeletedAt(warehouse.getDeletedAt());
            warehouseDocuments.add(doc);
        }

        warehouseMongoRepository.saveAll(warehouseDocuments);
        log.info("Migrated {} warehouses with embedded data", warehouseDocuments.size());
        return warehouseDocuments.size();
    }

    // ==================== Phase 5: Orders with all embedded data ====================

    private int migrateOrdersWithEmbeddedData() {
        log.info("Migrating orders with embedded customer, address, payment, products...");
        List<Order> orders = orderRepository.findAll();
        List<OrderDocument> orderDocuments = new ArrayList<>();

        for (Order order : orders) {
            OrderDocument doc = OrderDocument.builder()
                    .orderId(order.getOrderId())
                    // EMBEDDED: Customer snapshot
                    .customer(order.getUser() != null ? convertToCustomerEmbedded(order.getUser()) : null)
                    // EMBEDDED: Shipping address
                    .shippingAddress(order.getAddress() != null ? convertToAddressEmbedded(order.getAddress()) : null)
                    // EMBEDDED: Payment details
                    .payment(order.getPayment() != null ? convertToPaymentEmbedded(order.getPayment()) : null)
                    .orderDate(order.getOrderDate())
                    .orderStatus(order.getOrderStatus())
                    .totalAmount(order.getTotalAmount())
                    .subtotal(order.getSubtotal())
                    .taxAmount(order.getTaxAmount())
                    .shippingCost(order.getShippingCost())
                    .discountAmount(order.getDiscountAmount())
                    // EMBEDDED: Order products with product snapshots
                    .orderProducts(order.getOrderProducts().stream()
                            .map(op -> OrderProductEmbedded.builder()
                                    .productId(op.getProduct().getProductId())
                                    .productName(op.getProduct().getName())
                                    .productSku(op.getProduct().getSku())
                                    .productDescription(op.getProduct().getDescription())
                                    .quantity(op.getQuantity())
                                    .unitPrice(op.getUnitPrice())
                                    .totalPrice(op.getTotalPrice())
                                    .build())
                            .collect(Collectors.toList()))
                    // EMBEDDED: Coupon snapshot (if used)
                    .coupon(order.getCoupon() != null ? convertToCouponEmbedded(order.getCoupon()) : null)
                    .build();
            doc.setCreatedAt(order.getCreatedAt());
            doc.setUpdatedAt(order.getUpdatedAt());
            doc.setDeletedAt(order.getDeletedAt());
            orderDocuments.add(doc);
        }

        orderMongoRepository.saveAll(orderDocuments);
        log.info("Migrated {} orders with embedded data", orderDocuments.size());
        return orderDocuments.size();
    }

    // ==================== Phase 6: Update products with reviews ====================

    private void updateProductsWithReviews() {
        log.info("Updating products with embedded reviews...");
        List<Review> reviews = reviewRepository.findAll();
        
        // Group reviews by product
        Map<java.util.UUID, List<Review>> reviewsByProduct = reviews.stream()
                .filter(r -> r.getProduct() != null)
                .collect(Collectors.groupingBy(r -> r.getProduct().getProductId()));

        for (Map.Entry<java.util.UUID, List<Review>> entry : reviewsByProduct.entrySet()) {
            productMongoRepository.findByProductId(entry.getKey()).ifPresent(productDoc -> {
                List<ReviewEmbedded> embeddedReviews = entry.getValue().stream()
                        .map(this::convertToReviewEmbedded)
                        .collect(Collectors.toList());
                productDoc.setReviews(embeddedReviews);
                productMongoRepository.save(productDoc);
            });
        }
        log.info("Updated products with {} reviews", reviews.size());
    }

    // ==================== Embedded conversion methods ====================

    private AddressEmbedded convertToAddressEmbedded(Address address) {
        return AddressEmbedded.builder()
                .addressId(address.getAddressId())
                .street(address.getStreet())
                .streetNumber(address.getStreetNumber())
                .zip(address.getZip())
                .city(address.getCity())
                .build();
    }

    private BrandEmbedded convertToBrandEmbedded(Brand brand) {
        return BrandEmbedded.builder()
                .brandId(brand.getBrandId())
                .name(brand.getName())
                .description(brand.getDescription())
                .build();
    }

    private CategoryEmbedded convertToCategoryEmbedded(Category category) {
        return CategoryEmbedded.builder()
                .categoryId(category.getCategoryId())
                .name(category.getName())
                .description(category.getDescription())
                .build();
    }

    private WarrantyEmbedded convertToWarrantyEmbedded(Warranty warranty) {
        return WarrantyEmbedded.builder()
                .warrantyId(warranty.getWarrantyId())
                .startDate(warranty.getStartDate())
                .endDate(warranty.getEndDate())
                .description(warranty.getDescription())
                .build();
    }

    private CustomerEmbedded convertToCustomerEmbedded(User user) {
        return CustomerEmbedded.builder()
                .userId(user.getUserId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .build();
    }

    private PaymentEmbedded convertToPaymentEmbedded(Payment payment) {
        return PaymentEmbedded.builder()
                .paymentId(payment.getPaymentId())
                .paymentMethod(payment.getPaymentMethod())
                .paymentStatus(payment.getPaymentStatus())
                .paymentDate(payment.getPaymentDate())
                .amount(payment.getAmount())
                .build();
    }

    private CouponEmbedded convertToCouponEmbedded(Coupon coupon) {
        return CouponEmbedded.builder()
                .couponId(coupon.getCouponId())
                .code(coupon.getCode())
                .discountType(coupon.getDiscountType())
                .discountValue(coupon.getDiscountValue())
                .build();
    }

    private ReviewEmbedded convertToReviewEmbedded(Review review) {
        return ReviewEmbedded.builder()
                .reviewId(review.getReviewId())
                .userId(review.getUser() != null ? review.getUser().getUserId() : null)
                .userName(review.getUser() != null ? 
                        review.getUser().getFirstName() + " " + review.getUser().getLastName() : null)
                .reviewValue(review.getReviewValue())
                .title(review.getTitle())
                .comment(review.getComment())
                .isVerifiedPurchase(review.isVerifiedPurchase())
                .createdAt(review.getCreatedAt())
                .build();
    }

    // ==================== Document conversion methods (for standalone collections) ====================

    private BrandDocument convertToBrandDocument(Brand brand) {
        BrandDocument doc = BrandDocument.builder()
                .brandId(brand.getBrandId())
                .name(brand.getName())
                .description(brand.getDescription())
                .productIds(brand.getProducts().stream()
                        .map(Product::getProductId)
                        .collect(Collectors.toList()))
                .build();
        doc.setCreatedAt(brand.getCreatedAt());
        doc.setUpdatedAt(brand.getUpdatedAt());
        doc.setDeletedAt(brand.getDeletedAt());
        return doc;
    }

    private CategoryDocument convertToCategoryDocument(Category category) {
        CategoryDocument doc = CategoryDocument.builder()
                .categoryId(category.getCategoryId())
                .name(category.getName())
                .description(category.getDescription())
                .productIds(category.getProducts().stream()
                        .map(Product::getProductId)
                        .collect(Collectors.toList()))
                .build();
        doc.setCreatedAt(category.getCreatedAt());
        doc.setUpdatedAt(category.getUpdatedAt());
        doc.setDeletedAt(category.getDeletedAt());
        return doc;
    }

    private WarrantyDocument convertToWarrantyDocument(Warranty warranty) {
        WarrantyDocument doc = WarrantyDocument.builder()
                .warrantyId(warranty.getWarrantyId())
                .startDate(warranty.getStartDate())
                .endDate(warranty.getEndDate())
                .description(warranty.getDescription())
                .productId(warranty.getProduct() != null ? warranty.getProduct().getProductId() : null)
                .build();
        doc.setCreatedAt(warranty.getCreatedAt());
        doc.setUpdatedAt(warranty.getUpdatedAt());
        doc.setDeletedAt(warranty.getDeletedAt());
        return doc;
    }

    private CouponDocument convertToCouponDocument(Coupon coupon) {
        CouponDocument doc = CouponDocument.builder()
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
        doc.setCreatedAt(coupon.getCreatedAt());
        doc.setUpdatedAt(coupon.getUpdatedAt());
        doc.setDeletedAt(coupon.getDeletedAt());
        return doc;
    }

    private AddressDocument convertToAddressDocument(Address address) {
        AddressDocument doc = AddressDocument.builder()
                .addressId(address.getAddressId())
                .userId(address.getUser() != null ? address.getUser().getUserId() : null)
                .street(address.getStreet())
                .streetNumber(address.getStreetNumber())
                .zip(address.getZip())
                .city(address.getCity())
                .build();
        doc.setCreatedAt(address.getCreatedAt());
        doc.setUpdatedAt(address.getUpdatedAt());
        doc.setDeletedAt(address.getDeletedAt());
        return doc;
    }

    private PaymentDocument convertToPaymentDocument(Payment payment) {
        PaymentDocument doc = PaymentDocument.builder()
                .paymentId(payment.getPaymentId())
                .orderId(payment.getOrder() != null ? payment.getOrder().getOrderId() : null)
                .paymentMethod(payment.getPaymentMethod())
                .paymentStatus(payment.getPaymentStatus())
                .paymentDate(payment.getPaymentDate())
                .amount(payment.getAmount())
                .build();
        doc.setCreatedAt(payment.getCreatedAt());
        doc.setUpdatedAt(payment.getUpdatedAt());
        doc.setDeletedAt(payment.getDeletedAt());
        return doc;
    }

    private ReviewDocument convertToReviewDocument(Review review) {
        ReviewDocument doc = ReviewDocument.builder()
                .reviewId(review.getReviewId())
                .userId(review.getUser() != null ? review.getUser().getUserId() : null)
                .orderId(review.getOrderId())
                .reviewValue(review.getReviewValue())
                .title(review.getTitle())
                .comment(review.getComment())
                .isVerifiedPurchase(review.isVerifiedPurchase())
                .productId(review.getProduct() != null ? review.getProduct().getProductId() : null)
                .build();
        doc.setCreatedAt(review.getCreatedAt());
        doc.setUpdatedAt(review.getUpdatedAt());
        doc.setDeletedAt(review.getDeletedAt());
        return doc;
    }
}
