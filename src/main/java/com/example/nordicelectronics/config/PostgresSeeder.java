package com.example.nordicelectronics.config;

import com.example.nordicelectronics.entity.*;
import com.example.nordicelectronics.entity.enums.DiscountType;
import com.example.nordicelectronics.entity.enums.OrderStatus;
import com.example.nordicelectronics.entity.enums.PaymentMethod;
import com.example.nordicelectronics.entity.enums.PaymentStatus;
import com.example.nordicelectronics.repositories.sql.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datafaker.Faker;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
@org.springframework.core.annotation.Order(1)
@Profile("!test")
public class PostgresSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final BrandRepository brandRepository;
    private final CategoryRepository categoryRepository;
    private final WarrantyRepository warrantyRepository;
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;
    private final WarehouseProductRepository warehouseProductRepository;
    private final CouponRepository couponRepository;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final ReviewRepository reviewRepository;
    private final OrderProductRepository orderProductRepository;
    private final PasswordEncoder passwordEncoder;

    private final Faker faker = new Faker(new Locale("da", "DK"));

    @Override
    public void run(String... args) throws Exception {
        log.info("Starting database seeding...");

        // Check if database already has complete data
        // if (userRepository.count() > 0 && productRepository.count() > 0) {
        //     log.info("Database already contains data. Skipping seeding.");
        //     return;
        // }

        try {
            // 1. Create Users (increased to support more orders and reviews)
            List<User> users;
            if (userRepository.count() == 0) {
                users = createUsers();
                log.info("Created {} users", users.size());
            } else {
                users = userRepository.findAll();
                log.info("Using existing {} users", users.size());
            }

            // 2. Create Brands
            List<Brand> brands;
            if (brandRepository.count() == 0) {
                brands = createBrands();
                log.info("Created {} brands", brands.size());
            } else {
                brands = brandRepository.findAll();
                log.info("Using existing {} brands", brands.size());
            }

            // 3. Create Categories
            List<Category> categories;
            if (categoryRepository.count() == 0) {
                categories = createCategories();
                log.info("Created {} categories", categories.size());
            } else {
                categories = categoryRepository.findAll();
                log.info("Using existing {} categories", categories.size());
            }

            // 4. Create Warranties (if needed)
            // Note: Warranties are created on-demand during product creation
            // due to OneToOne relationship - each product needs its own warranty
            log.info("Checking warranty count...");
            long warrantyCount = warrantyRepository.count();
            log.info("Found {} existing warranties", warrantyCount);

            // 5. Create Products (REDUCED from 100 to 50 for better review concentration)
            log.info("Starting product creation...");
            List<Product> products = createProducts(brands, categories);
            log.info("Created {} products", products.size());

            // 6. Create Warehouses (REDUCED from 3 to 2)
            log.info("Starting warehouse creation...");
            List<Warehouse> warehouses = createWarehouses();
            log.info("Created {} warehouses", warehouses.size());

            // 7. Create WarehouseProducts (stock)
            log.info("Starting warehouse product creation...");
            List<WarehouseProduct> warehouseProducts = createWarehouseProducts(warehouses, products);
            log.info("Created {} warehouse product entries", warehouseProducts.size());

            // 8. Create Coupons
            List<Coupon> coupons;
            if (couponRepository.count() == 0) {
                log.info("Starting coupon creation...");
                coupons = createCoupons();
                log.info("Created {} coupons", coupons.size());
            } else {
                coupons = couponRepository.findAll();
                log.info("Using existing {} coupons", coupons.size());
            }

            // 9. Create Orders (INCREASED from 50 to 150 for better sales data)
            log.info("Starting order creation...");
            List<Order> orders = createOrders(users, coupons);
            log.info("Created {} orders", orders.size());

            // 10. Create Payments
            log.info("Starting payment creation...");
            List<Payment> payments = createPayments(orders);
            log.info("Created {} payments", payments.size());

            // 11. Create Order Products (line items)
            log.info("Starting order product creation...");
            List<OrderProduct> orderProducts = createOrderProducts(orders, products);
            log.info("Created {} order products", orderProducts.size());

            // 12. Create Reviews (INCREASED from 75 to 250 for better review data)
            log.info("Starting review creation...");
            List<Review> reviews = createReviews(users, products, orders);
            log.info("Created {} reviews", reviews.size());

            log.info("Database seeding completed successfully!");
            log.info("Summary: {} users, {} products, {} warehouses, {} orders, {} reviews",
                    users.size(), products.size(), warehouses.size(), orders.size(), reviews.size());
        } catch (Exception e) {
            log.error("Error during database seeding: ", e);
            throw e;
        }
    }

    private List<User> createUsers() {
        List<User> users = new ArrayList<>();

        // Create admin user
        User admin = User.builder()
                .firstName("Admin")
                .lastName("User")
                .email("admin@nordic.com")
                .phoneNumber("12345678")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .password(passwordEncoder.encode("admin123"))
                .isAdmin(true)
                .address(new ArrayList<>())
                .orders(new ArrayList<>())
                .build();
        admin = userRepository.save(admin);

        // Create admin address
        Address adminAddress = Address.builder()
                .user(admin)
                .street("Vestergade")
                .streetNumber("1")
                .zip("1000")
                .city("Copenhagen")
                .build();
        addressRepository.save(adminAddress);
        users.add(admin);

        // Create test user
        User testUser = User.builder()
                .firstName("Test")
                .lastName("User")
                .email("user@nordic.com")
                .phoneNumber("87654321")
                .dateOfBirth(LocalDate.of(1995, 6, 15))
                .password(passwordEncoder.encode("user123"))
                .isAdmin(false)
                .address(new ArrayList<>())
                .orders(new ArrayList<>())
                .build();
        testUser = userRepository.save(testUser);

        // Create test user address
        Address testAddress = Address.builder()
                .user(testUser)
                .street("Østergade")
                .streetNumber("10")
                .zip("2100")
                .city("Copenhagen")
                .build();
        addressRepository.save(testAddress);
        users.add(testUser);

        // Random users with realistic data (INCREASED from 20 to 40 for more reviewers)
        for (int i = 0; i < 40; i++) {
            User randomUser = User.builder()
                    .firstName(faker.name().firstName())
                    .lastName(faker.name().lastName())
                    .email(faker.internet().emailAddress())
                    .phoneNumber(faker.numerify("########"))
                    .dateOfBirth(faker.date().birthday(18, 80).toInstant().atZone(ZoneId.systemDefault()).toLocalDate())
                    .password(passwordEncoder.encode("password123"))
                    .isAdmin(false)
                    .address(new ArrayList<>())
                    .orders(new ArrayList<>())
                    .build();

            randomUser = userRepository.save(randomUser);

            // Create realistic address for random user
            Address randomAddress = Address.builder()
                    .user(randomUser)
                    .street(faker.address().streetName())
                    .streetNumber(faker.address().buildingNumber())
                    .zip(faker.address().zipCode())
                    .city(faker.address().city())
                    .build();
            addressRepository.save(randomAddress);
            users.add(randomUser);
        }

        return users;
    }

    private List<Brand> createBrands() {
        String[] brandNames = {
                "Samsung", "Apple", "Sony", "LG", "Panasonic",
                "Philips", "Bose", "JBL", "Dell", "HP",
                "Lenovo", "Asus", "Acer", "Canon", "Nikon"
        };

        List<Brand> brands = new ArrayList<>();
        for (String brandName : brandNames) {
            Brand brand = Brand.builder()
                    .name(brandName)
                    .description(faker.lorem().paragraph(2))
                    .build();
            brands.add(brandRepository.save(brand));
        }

        return brands;
    }

    private List<Category> createCategories() {
        Map<String, String> categoryData = new LinkedHashMap<>();
        categoryData.put("Smartphones", "Mobile phones and smartphones with latest technology");
        categoryData.put("Laptops", "Portable computers and notebooks for work and entertainment");
        categoryData.put("Tablets", "Tablet computers and e-readers");
        categoryData.put("Televisions", "Smart TVs, LED, OLED and QLED televisions");
        categoryData.put("Audio", "Headphones, speakers, soundbars and audio systems");
        categoryData.put("Cameras", "Digital cameras, DSLRs and camera accessories");
        categoryData.put("Gaming", "Gaming consoles, accessories and gaming peripherals");
        categoryData.put("Wearables", "Smartwatches, fitness trackers and wearable technology");
        categoryData.put("Home Appliances", "Smart home devices and appliances");
        categoryData.put("Accessories", "Electronic accessories and peripherals");

        List<Category> categories = new ArrayList<>();
        for (Map.Entry<String, String> entry : categoryData.entrySet()) {
            Category category = Category.builder()
                    .name(entry.getKey())
                    .description(entry.getValue())
                    .products(new HashSet<>())
                    .build();
            categories.add(categoryRepository.save(category));
        }

        return categories;
    }

    private Warranty createWarranty(Random random) {
        String[] warrantyTypes = {
                "Standard manufacturer warranty covering defects",
                "Extended warranty with full coverage",
                "Limited warranty for parts and labor",
                "Premium warranty with 24/7 support",
                "Basic warranty covering manufacturing defects",
                "Comprehensive warranty with replacement guarantee",
                "Standard 1-year warranty",
                "Extended 2-year warranty",
                "Premium 3-year warranty with accidental damage coverage"
        };

        LocalDate startDate = faker.date().past(30, java.util.concurrent.TimeUnit.DAYS)
                .toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate endDate = faker.date().future(faker.number().numberBetween(365, 1095), java.util.concurrent.TimeUnit.DAYS)
                .toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        return Warranty.builder()
                .startDate(startDate)
                .endDate(endDate)
                .description(warrantyTypes[random.nextInt(warrantyTypes.length)])
                .build();
    }

    private List<Product> createProducts(List<Brand> brands, List<Category> categories) {
        String[] productPrefixes = {
                "Ultra", "Pro", "Max", "Plus", "Elite", "Premium",
                "Smart", "Advanced", "Digital", "Wireless", "Portable"
        };

        String[] productTypes = {
                "Phone", "Laptop", "Tablet", "TV", "Monitor", "Speaker",
                "Headphones", "Camera", "Console", "Watch", "Router", "Keyboard"
        };

        List<Product> products = new ArrayList<>();
        Random random = new Random();

        // REDUCED from 100 to 50 products to concentrate reviews and sales
        for (int i = 0; i < 50; i++) {
            try {
                Brand brand = brands.get(random.nextInt(brands.size()));

                // Select 1-3 random categories
                Set<Category> productCategories = new HashSet<>();
                int numCategories = random.nextInt(3) + 1;
                for (int j = 0; j < numCategories; j++) {
                    productCategories.add(categories.get(random.nextInt(categories.size())));
                }

                String productName = brand.getName() + " " +
                        productPrefixes[random.nextInt(productPrefixes.length)] + " " +
                        productTypes[random.nextInt(productTypes.length)];

                BigDecimal price = BigDecimal.valueOf(faker.number().numberBetween(100, 5000));
                BigDecimal weight = BigDecimal.valueOf(faker.number().randomDouble(2, 1, 1000)).setScale(2, RoundingMode.HALF_UP);

                // Create a new warranty for each product (OneToOne relationship requires unique warranty per product)
                Warranty warranty = createWarranty(random);

                Product product = Product.builder()
                        .sku("SKU-" + faker.random().hex(10).toUpperCase())
                        .name(productName)
                        .description(faker.lorem().paragraph(3))
                        .price(price)
                        .weight(weight)
                        .brand(brand)
                        .warranty(warranty)
                        .categories(productCategories)
                        .warehouseProducts(new HashSet<>())
                        .reviews(new HashSet<>())
                        .build();
                products.add(productRepository.save(product));

                if ((i + 1) % 20 == 0) {
                    log.info("Created {} products so far...", i + 1);
                }
            } catch (Exception e) {
                log.error("Error creating product {}: ", i, e);
            }
        }

        return products;
    }

    private List<Warehouse> createWarehouses() {
        List<Warehouse> warehouses = new ArrayList<>();

        // REDUCED from 3 to 2 warehouses
        String[] warehouseNames = {
                "Copenhagen Central Distribution Center",
                "Aarhus Regional Warehouse"
        };

        for (String warehouseName : warehouseNames) {
            // 1. Get or create system user for warehouse addresses
            User systemUser = userRepository.findByEmail("admin@nordic.com")
                    .orElseGet(() -> {
                        User admin = User.builder()
                                .firstName("System")
                                .lastName("Admin")
                                .email("system@nordic.com")
                                .phoneNumber(faker.numerify("########"))
                                .dateOfBirth(LocalDate.of(2000, 1, 1))
                                .password(passwordEncoder.encode("system"))
                                .isAdmin(true)
                                .address(new ArrayList<>())
                                .orders(new ArrayList<>())
                                .build();
                        return userRepository.save(admin);
                    });

            // 2. Create realistic address (don't save manually - let cascade handle it)
            Address address = Address.builder()
                    .street(faker.address().streetName())
                    .streetNumber(faker.address().buildingNumber())
                    .zip(faker.address().zipCode())
                    .city(faker.address().city())
                    .user(systemUser)
                    .build();

            // 3. Create warehouse and assign address (cascade will save the address)
            Warehouse warehouse = Warehouse.builder()
                    .name(warehouseName)
                    .phoneNumber(faker.numerify("########"))
                    .address(address)
                    .warehouseProducts(new HashSet<>())
                    .build();

            warehouses.add(warehouseRepository.save(warehouse));
        }

        return warehouses;
    }

    private List<WarehouseProduct> createWarehouseProducts(List<Warehouse> warehouses, List<Product> products) {
        List<WarehouseProduct> warehouseProducts = new ArrayList<>();
        Random random = new Random();

        for (Product product : products) {
            // Each product is stocked in 1-2 warehouses (adjusted from 1-3)
            int numWarehouses = random.nextInt(2) + 1;
            List<Warehouse> selectedWarehouses = new ArrayList<>(warehouses);
            Collections.shuffle(selectedWarehouses);

            for (int i = 0; i < numWarehouses && i < selectedWarehouses.size(); i++) {
                Warehouse warehouse = selectedWarehouses.get(i);

                WarehouseProductKey key = WarehouseProductKey.builder()
                        .warehouseId(warehouse.getWarehouseId())
                        .productId(product.getProductId())
                        .build();

                WarehouseProduct wp = WarehouseProduct.builder()
                        .id(key)
                        .warehouse(warehouse)
                        .product(product)
                        .stockQuantity(random.nextInt(200) + 10)
                        .build();

                warehouseProducts.add(warehouseProductRepository.save(wp));
            }
        }

        return warehouseProducts;
    }

    private List<Coupon> createCoupons() {
        List<Coupon> coupons = new ArrayList<>();
        Random random = new Random();

        String[] couponCodes = {
                "WELCOME10", "SUMMER20", "WINTER25", "SPRING15", "FALL30",
                "NEWYEAR50", "BLACKFRIDAY", "CYBERMONDAY", "LOYALTY15", "FIRST20"
        };

        for (String code : couponCodes) {
            DiscountType discountType = random.nextBoolean() ? DiscountType.percentage : DiscountType.fixed_amount;
            BigDecimal discountValue = discountType == DiscountType.percentage
                    ? BigDecimal.valueOf(random.nextInt(30) + 5)
                    : BigDecimal.valueOf(random.nextInt(500) + 50);

            Coupon coupon = Coupon.builder()
                    .code(code)
                    .discountType(discountType)
                    .discountValue(discountValue)
                    .minimumOrderValue(BigDecimal.valueOf(random.nextInt(1000) + 100))
                    .expiryDate(LocalDate.now().plusMonths(random.nextInt(12) + 1))
                    .usageLimit(random.nextInt(1000) + 100)
                    .timesUsed(random.nextInt(50))
                    .isActive(random.nextBoolean())
                    .build();

            coupons.add(couponRepository.save(coupon));
        }

        return coupons;
    }

    private List<Order> createOrders(List<User> users, List<Coupon> coupons) {
        List<Order> orders = new ArrayList<>();
        Random random = new Random();

        // Get all addresses for users
        List<Address> addresses = addressRepository.findAll();

        // INCREASED from 50 to 150 orders for better sales analytics
        for (int i = 0; i < 150; i++) {
            User user = users.get(random.nextInt(users.size()));

            // Get user's addresses or use any address
            Address orderAddress = addresses.stream()
                    .filter(addr -> addr.getUser().getUserId().equals(user.getUserId()))
                    .findFirst()
                    .orElse(addresses.get(random.nextInt(addresses.size())));

            BigDecimal subtotal = BigDecimal.valueOf(faker.number().numberBetween(100, 5000));
            BigDecimal taxAmount = subtotal.multiply(BigDecimal.valueOf(0.25)); // 25% tax
            BigDecimal shippingCost = BigDecimal.valueOf(faker.number().numberBetween(20, 120));

            // Apply coupon to about 30% of orders
            Coupon coupon = null;
            BigDecimal discountAmount = BigDecimal.ZERO;
            if (!coupons.isEmpty() && random.nextInt(100) < 30) {
                coupon = coupons.get(random.nextInt(coupons.size()));
                // Calculate discount based on coupon type
                if (coupon.getDiscountType() == DiscountType.percentage) {
                    discountAmount = subtotal.multiply(coupon.getDiscountValue().divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP));
                } else {
                    discountAmount = coupon.getDiscountValue();
                }
                // Ensure discount doesn't exceed subtotal
                if (discountAmount.compareTo(subtotal) > 0) {
                    discountAmount = subtotal;
                }
            }

            BigDecimal totalAmount = subtotal.add(taxAmount).add(shippingCost).subtract(discountAmount);

            Order order = Order.builder()
                    .user(user)
                    .address(orderAddress)
                    .orderDate(faker.date().past(365, java.util.concurrent.TimeUnit.DAYS)
                            .toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime())
                    .orderStatus(OrderStatus.values()[random.nextInt(OrderStatus.values().length)])
                    .subtotal(subtotal)
                    .taxAmount(taxAmount)
                    .shippingCost(shippingCost)
                    .discountAmount(discountAmount)
                    .totalAmount(totalAmount)
                    .coupon(coupon)
                    .orderProducts(new ArrayList<>())
                    .build();

            orders.add(orderRepository.save(order));
        }

        return orders;
    }

    private List<Payment> createPayments(List<Order> orders) {
        List<Payment> payments = new ArrayList<>();
        Random random = new Random();

        for (Order order : orders) {
            Payment payment = Payment.builder()
                    .order(order)
                    .paymentMethod(PaymentMethod.values()[random.nextInt(PaymentMethod.values().length)])
                    .paymentStatus(PaymentStatus.values()[random.nextInt(PaymentStatus.values().length)])
                    .paymentDate(order.getOrderDate().plusMinutes(faker.number().numberBetween(1, 60)))
                    .amount(order.getTotalAmount())
                    .build();

            payments.add(paymentRepository.save(payment));
        }

        return payments;
    }

    private List<Review> createReviews(List<User> users, List<Product> products, List<Order> orders) {
        List<Review> reviews = new ArrayList<>();
        Random random = new Random();

        // INCREASED from 75 to 250 reviews for better review analytics
        // This ensures most products will have multiple reviews for the materialized view
        for (int i = 0; i < 250; i++) {
            User user = users.get(random.nextInt(users.size()));
            Product product = products.get(random.nextInt(products.size()));
            Order order = orders.get(random.nextInt(orders.size()));

            Review review = Review.builder()
                    .user(user)
                    .product(product)
                    .orderId(order.getOrderId())
                    .reviewValue(faker.number().numberBetween(1, 6)) // 1-5 stars
                    .title(faker.lorem().sentence(3, 6))
                    .comment(faker.lorem().paragraph(2))
                    .isVerifiedPurchase(faker.bool().bool())
                    .build();

            reviews.add(reviewRepository.save(review));
        }

        return reviews;
    }

    private List<OrderProduct> createOrderProducts(List<Order> orders, List<Product> products) {
        List<OrderProduct> orderProducts = new ArrayList<>();
        Random random = new Random();

        for (Order order : orders) {
            try {
                // Each order has 1-5 products
                int numProducts = random.nextInt(5) + 1;
                Set<Product> selectedProducts = new HashSet<>();

                for (int i = 0; i < numProducts; i++) {
                    Product product = products.get(random.nextInt(products.size()));
                    if (selectedProducts.add(product)) { // Avoid duplicates
                        int quantity = random.nextInt(3) + 1; // 1-3 items
                        BigDecimal unitPrice = product.getPrice();
                        BigDecimal totalPrice = unitPrice.multiply(BigDecimal.valueOf(quantity));

                        OrderProductKey key = new OrderProductKey(order.getOrderId(), product.getProductId());

                        OrderProduct orderProduct = OrderProduct.builder()
                                .id(key)
                                .order(order)
                                .product(product)
                                .quantity(quantity)
                                .unitPrice(unitPrice)
                                .totalPrice(totalPrice)
                                .build();

                        orderProducts.add(orderProductRepository.save(orderProduct));
                    }
                }
            } catch (Exception e) {
                log.error("Error creating order products for order {}: ", order.getOrderId(), e);
            }
        }

        return orderProducts;
    }
}