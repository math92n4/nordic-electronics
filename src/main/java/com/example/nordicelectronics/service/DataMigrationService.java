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
        log.info("Starting full database migration from PostgreSQL to MongoDB");
        
        Map<String, Object> results = new HashMap<>();
        long startTime = System.currentTimeMillis();

        try {
            // Clear existing MongoDB data
            clearMongoDBData();
            results.put("cleared", "Successfully cleared existing MongoDB data");

            // Migrate in order of dependencies
            int brandsCount = migrateBrands();
            results.put("brands", brandsCount);
            
            int categoriesCount = migrateCategories();
            results.put("categories", categoriesCount);
            
            int warrantiesCount = migrateWarranties();
            results.put("warranties", warrantiesCount);
            
            int productsCount = migrateProducts();
            results.put("products", productsCount);
            
            int warehousesCount = migrateWarehouses();
            results.put("warehouses", warehousesCount);
            
            int usersCount = migrateUsers();
            results.put("users", usersCount);
            
            int addressesCount = migrateAddresses();
            results.put("addresses", addressesCount);
            
            int couponsCount = migrateCoupons();
            results.put("coupons", couponsCount);
            
            int ordersCount = migrateOrders();
            results.put("orders", ordersCount);
            
            int paymentsCount = migratePayments();
            results.put("payments", paymentsCount);
            
            int reviewsCount = migrateReviews();
            results.put("reviews", reviewsCount);

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

    private int migrateProducts() {
        log.info("Migrating products...");
        List<Product> products = productRepository.findAll();
        List<ProductDocument> productDocuments = products.stream()
                .map(this::convertToProductDocument)
                .collect(Collectors.toList());
        productMongoRepository.saveAll(productDocuments);
        log.info("Migrated {} products", productDocuments.size());
        return productDocuments.size();
    }

    private int migrateWarehouses() {
        log.info("Migrating warehouses...");
        List<Warehouse> warehouses = warehouseRepository.findAll();
        List<WarehouseDocument> warehouseDocuments = warehouses.stream()
                .map(this::convertToWarehouseDocument)
                .collect(Collectors.toList());
        warehouseMongoRepository.saveAll(warehouseDocuments);
        log.info("Migrated {} warehouses", warehouseDocuments.size());
        return warehouseDocuments.size();
    }

    private int migrateUsers() {
        log.info("Migrating users...");
        List<User> users = userRepository.findAll();
        List<UserDocument> userDocuments = users.stream()
                .map(this::convertToUserDocument)
                .collect(Collectors.toList());
        userMongoRepository.saveAll(userDocuments);
        log.info("Migrated {} users", userDocuments.size());
        return userDocuments.size();
    }

    private int migrateAddresses() {
        log.info("Migrating addresses...");
        List<Address> addresses = addressRepository.findAll();
        List<AddressDocument> addressDocuments = addresses.stream()
                .map(this::convertToAddressDocument)
                .collect(Collectors.toList());
        addressMongoRepository.saveAll(addressDocuments);
        log.info("Migrated {} addresses", addressDocuments.size());
        return addressDocuments.size();
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

    private int migrateOrders() {
        log.info("Migrating orders...");
        List<Order> orders = orderRepository.findAll();
        List<OrderDocument> orderDocuments = orders.stream()
                .map(this::convertToOrderDocument)
                .collect(Collectors.toList());
        orderMongoRepository.saveAll(orderDocuments);
        log.info("Migrated {} orders", orderDocuments.size());
        return orderDocuments.size();
    }

    private int migratePayments() {
        log.info("Migrating payments...");
        List<Payment> payments = paymentRepository.findAll();
        List<PaymentDocument> paymentDocuments = payments.stream()
                .map(this::convertToPaymentDocument)
                .collect(Collectors.toList());
        paymentMongoRepository.saveAll(paymentDocuments);
        log.info("Migrated {} payments", paymentDocuments.size());
        return paymentDocuments.size();
    }

    private int migrateReviews() {
        log.info("Migrating reviews...");
        List<Review> reviews = reviewRepository.findAll();
        List<ReviewDocument> reviewDocuments = reviews.stream()
                .map(this::convertToReviewDocument)
                .collect(Collectors.toList());
        reviewMongoRepository.saveAll(reviewDocuments);
        log.info("Migrated {} reviews", reviewDocuments.size());
        return reviewDocuments.size();
    }

    // Conversion methods
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

    private ProductDocument convertToProductDocument(Product product) {
        ProductDocument doc = ProductDocument.builder()
                .productId(product.getProductId())
                .sku(product.getSku())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .weight(product.getWeight())
                .categoryIds(product.getCategories().stream()
                        .map(Category::getCategoryId)
                        .collect(Collectors.toList()))
                .warrantyId(product.getWarranty() != null ? product.getWarranty().getWarrantyId() : null)
                .brandId(product.getBrand() != null ? product.getBrand().getBrandId() : null)
                .warehouseProducts(product.getWarehouseProducts().stream()
                        .map(wp -> WarehouseProductEmbedded.builder()
                                .warehouseId(wp.getWarehouse().getWarehouseId())
                                .productId(wp.getProduct().getProductId())
                                .stockQuantity(wp.getStockQuantity())
                                .build())
                        .collect(Collectors.toList()))
                .reviewIds(product.getReviews().stream()
                        .map(Review::getReviewId)
                        .collect(Collectors.toList()))
                .build();
        doc.setCreatedAt(product.getCreatedAt());
        doc.setUpdatedAt(product.getUpdatedAt());
        doc.setDeletedAt(product.getDeletedAt());
        return doc;
    }

    private WarehouseDocument convertToWarehouseDocument(Warehouse warehouse) {
        WarehouseDocument doc = WarehouseDocument.builder()
                .warehouseId(warehouse.getWarehouseId())
                .name(warehouse.getName())
                .phoneNumber(warehouse.getPhoneNumber())
                .warehouseProducts(warehouse.getWarehouseProducts().stream()
                        .map(wp -> WarehouseProductEmbedded.builder()
                                .warehouseId(wp.getWarehouse().getWarehouseId())
                                .productId(wp.getProduct().getProductId())
                                .stockQuantity(wp.getStockQuantity())
                                .build())
                        .collect(Collectors.toList()))
                .addressId(warehouse.getAddress() != null ? warehouse.getAddress().getAddressId() : null)
                .build();
        doc.setCreatedAt(warehouse.getCreatedAt());
        doc.setUpdatedAt(warehouse.getUpdatedAt());
        doc.setDeletedAt(warehouse.getDeletedAt());
        return doc;
    }

    private UserDocument convertToUserDocument(User user) {
        UserDocument doc = UserDocument.builder()
                .userId(user.getUserId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .dateOfBirth(user.getDateOfBirth())
                .password(user.getPassword())
                .isAdmin(user.isAdmin())
                .addressIds(user.getAddress().stream()
                        .map(Address::getAddressId)
                        .collect(Collectors.toList()))
                .orderIds(user.getOrders().stream()
                        .map(Order::getOrderId)
                        .collect(Collectors.toList()))
                .build();
        doc.setCreatedAt(user.getCreatedAt());
        doc.setUpdatedAt(user.getUpdatedAt());
        doc.setDeletedAt(user.getDeletedAt());
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

    private OrderDocument convertToOrderDocument(Order order) {
        OrderDocument doc = OrderDocument.builder()
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
                .orderProducts(order.getOrderProducts().stream()
                        .map(op -> OrderProductEmbedded.builder()
                                .orderId(op.getOrder().getOrderId())
                                .productId(op.getProduct().getProductId())
                                .quantity(op.getQuantity())
                                .unitPrice(op.getUnitPrice())
                                .totalPrice(op.getTotalPrice())
                                .build())
                        .collect(Collectors.toList()))
                .couponId(order.getCoupon() != null ? order.getCoupon().getCouponId() : null)
                .build();
        doc.setCreatedAt(order.getCreatedAt());
        doc.setUpdatedAt(order.getUpdatedAt());
        doc.setDeletedAt(order.getDeletedAt());
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

