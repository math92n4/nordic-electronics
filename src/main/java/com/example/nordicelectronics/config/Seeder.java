package com.example.nordicelectronics.config;

import com.example.nordicelectronics.entity.*;
import com.example.nordicelectronics.entity.enums.DiscountType;
import com.example.nordicelectronics.entity.enums.OrderStatus;
import com.example.nordicelectronics.entity.enums.PaymentMethod;
import com.example.nordicelectronics.entity.enums.PaymentStatus;
import com.example.nordicelectronics.repositories.sql.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.instancio.Instancio;
import org.instancio.Select;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.instancio.Select.field;

@Component
@RequiredArgsConstructor
@Slf4j
public class Seeder implements CommandLineRunner {

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
    private final OrderCouponRepository orderCouponRepository;
    private final WishlistRepository wishlistRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        log.info("Starting database seeding...");
        
        // Check if database already has complete data
        if (userRepository.count() > 0 && productRepository.count() > 0) {
            log.info("Database already contains data. Skipping seeding.");
            return;
        }

        try {
            // 1. Create Users
            List<User> users;
            if (userRepository.count() == 0) {
                users = createUsers();
                log.info("Created {} users", users.size());
            } else {
                users = userRepository.findAll();
                log.info("Using existing {} users", users.size());
            }

            // 3. Create Brands
            List<Brand> brands;
            if (brandRepository.count() == 0) {
                brands = createBrands();
                log.info("Created {} brands", brands.size());
            } else {
                brands = brandRepository.findAll();
                log.info("Using existing {} brands", brands.size());
            }

            // 4. Create Categories
            List<Category> categories;
            if (categoryRepository.count() == 0) {
                categories = createCategories();
                log.info("Created {} categories", categories.size());
            } else {
                categories = categoryRepository.findAll();
                log.info("Using existing {} categories", categories.size());
            }

            // 5. Create Products (warranties will be created inline)
            log.info("Starting product creation...");
            List<Product> products = createProducts(brands, categories);
            log.info("Created {} products", products.size());

            // 6. Create Warehouses
            log.info("Starting warehouse creation...");
            List<Warehouse> warehouses = createWarehouses();
            log.info("Created {} warehouses", warehouses.size());

            // 7. Create WarehouseProducts (stock)
            log.info("Starting warehouse product creation...");
            List<WarehouseProduct> warehouseProducts = createWarehouseProducts(warehouses, products);
            log.info("Created {} warehouse product entries", warehouseProducts.size());

            // 8. Create Coupons
            log.info("Starting coupon creation...");
            List<Coupon> coupons = createCoupons();
            log.info("Created {} coupons", coupons.size());

            // 9. Create Orders
            log.info("Starting order creation...");
            List<Order> orders = createOrders(users);
            log.info("Created {} orders", orders.size());

            // 10. Create Payments
            log.info("Starting payment creation...");
            List<Payment> payments = createPayments(orders);
            log.info("Created {} payments", payments.size());

            // 11. Create Order Products (line items)
            log.info("Starting order product creation...");
            List<OrderProduct> orderProducts = createOrderProducts(orders, products);
            log.info("Created {} order products", orderProducts.size());

            // 12. Create Order Coupons
            log.info("Starting order coupon creation...");
            List<OrderCoupon> orderCoupons = createOrderCoupons(orders, coupons);
            log.info("Created {} order coupons", orderCoupons.size());

            // 13. Create Wishlists (with products)
            log.info("Starting wishlist creation...");
            List<Wishlist> wishlists = createWishlists(users, products);
            log.info("Created {} wishlists", wishlists.size());

            // 14. Create Reviews
            log.info("Starting review creation...");
            List<Review> reviews = createReviews(users, products, orders);
            log.info("Created {} reviews", reviews.size());

            log.info("Database seeding completed successfully!");
        } catch (Exception e) {
            log.error("Error during database seeding: ", e);
            throw e;
        }
    }

    private List<User> createUsers() {
        List<User> users = new ArrayList<>();

        // Create admin user
        Address adminAddress = Address.builder()
                .street("Vestergade")
                .streetNumber("1")
                .zip("1000")
                .city("Copenhagen")
                .build();
        addressRepository.save(adminAddress);

        User admin = User.builder()
                .firstName("Admin")
                .lastName("User")
                .email("admin@nordic.com")
                .phoneNumber("+45 12 34 56 78")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .password(passwordEncoder.encode("admin123"))
                .isAdmin(true)
                .address(adminAddress) // assign before saving
                .build();
        users.add(userRepository.save(admin));

        // Create test user
        Address testAddress = Address.builder()
                .street("Østergade")
                .streetNumber("10")
                .zip("2100")
                .city("Copenhagen")
                .build();
        addressRepository.save(testAddress);

        User testUser = User.builder()
                .firstName("Test")
                .lastName("User")
                .email("user@nordic.com")
                .phoneNumber("+45 87 65 43 21")
                .dateOfBirth(LocalDate.of(1995, 6, 15))
                .password(passwordEncoder.encode("user123"))
                .isAdmin(false)
                .address(testAddress) // assign before saving
                .build();
        users.add(userRepository.save(testUser));

        // Random users
        for (int i = 0; i < 20; i++) {
            Address randomAddress = Address.builder()
                    .street("Random Street " + i)
                    .streetNumber(String.valueOf(i + 1))
                    .zip(String.valueOf(1000 + i))
                    .city("City " + i)
                    .build();
            addressRepository.save(randomAddress);

            int finalI = i;
            User randomUser = Instancio.of(User.class)
                    .ignore(field(User::getUserId)) // let JPA generate it
                    .supply(field(User::getEmail), random -> "user" + finalI + "@example.com")
                    .set(field(User::getAddress), randomAddress) // assign manually
                    .set(field(User::isAdmin), false)
                    .create();

            users.add(userRepository.save(randomUser));
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
            Brand brand = Instancio.of(Brand.class)
                    .ignore(field(Brand::getBrandId))
                    .ignore(field(Brand::getProducts))
                    .set(field(Brand::getName), brandName)
                    .supply(field(Brand::getDescription), random -> "Leading manufacturer of quality " + brandName + " electronics and technology products.")
                    .create();
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

    private List<Warranty> createWarranties() {
        List<Warranty> warranties = Instancio.ofList(Warranty.class)
                .size(50)
                .ignore(field(Warranty::getWarrantyId))
                .ignore(field(Warranty::getProduct))
                .supply(field(Warranty::getStartDate), random -> LocalDate.now().minusDays(random.intRange(0, 30)))
                .supply(field(Warranty::getEndDate), random -> LocalDate.now().plusYears(random.intRange(1, 5)))
                .supply(field(Warranty::getDescription), random -> random.oneOf(
                    "Standard manufacturer warranty covering defects",
                    "Extended warranty with full coverage",
                    "Limited warranty for parts and labor",
                    "Premium warranty with 24/7 support",
                    "Basic warranty covering manufacturing defects"
                ))
                .create();

        return warrantyRepository.saveAll(warranties);
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

        for (int i = 0; i < 100; i++) {
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

                BigDecimal price = BigDecimal.valueOf(random.nextInt(5000) + 100);
                BigDecimal weight = BigDecimal.valueOf(random.nextDouble() * 10 + 0.1).setScale(2, BigDecimal.ROUND_HALF_UP);

                // Create warranty inline for ALL products (database requires NOT NULL)
                LocalDate startDate = LocalDate.now().minusDays(random.nextInt(30));
                LocalDate endDate = LocalDate.now().plusYears(random.nextInt(3) + 1);
                String[] warrantyDescriptions = {
                    "Standard manufacturer warranty covering defects",
                    "Extended warranty with full coverage",
                    "Limited warranty for parts and labor",
                    "Premium warranty with 24/7 support",
                    "Basic warranty covering manufacturing defects"
                };
                
                Warranty warranty = Warranty.builder()
                        .startDate(startDate)
                        .endDate(endDate)
                        .description(warrantyDescriptions[random.nextInt(warrantyDescriptions.length)])
                        .build();

                Product product = Product.builder()
                        .sku("SKU-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                        .name(productName)
                        .description("High-quality " + productName + " with latest features and technology. Perfect for both professional and personal use.")
                        .price(price)
                        .weight(weight)
                        .brand(brand)
                        .warranty(warranty)
                        .categories(productCategories)
                        .warehouseProducts(new HashSet<>())
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

        String[][] warehouseData = {
                {"Copenhagen Central", "Østerbrogade", "15", "2100", "Copenhagen"},
                {"Aarhus Distribution", "Randersvej", "89", "8000", "Aarhus"},
                {"Odense Logistics", "Vestergade", "45", "5000", "Odense"}
        };

        for (String[] data : warehouseData) {
            // 1. Create and save address first
            Address address = Address.builder()
                    .street(data[1])
                    .streetNumber(data[2])
                    .zip(data[3])
                    .city(data[4])
                    .build();
            addressRepository.save(address);

            // 2. Create warehouse and assign address
            Warehouse warehouse = Warehouse.builder()
                    .name(data[0])
                    .phone("+45 12 34 56 78")
                    .address(address) // must assign before saving
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
            // Each product is stocked in 1-3 warehouses
            int numWarehouses = random.nextInt(3) + 1;
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

    private List<Order> createOrders(List<User> users) {
        List<Order> orders = new ArrayList<>();
        Random random = new Random();

        // Create 50 orders
        for (int i = 0; i < 50; i++) {
            User user = users.get(random.nextInt(users.size()));
            
            BigDecimal subtotal = BigDecimal.valueOf(random.nextInt(5000) + 100);
            BigDecimal taxAmount = subtotal.multiply(BigDecimal.valueOf(0.25)); // 25% tax
            BigDecimal shippingCost = BigDecimal.valueOf(random.nextInt(100) + 20);
            BigDecimal discountAmount = random.nextBoolean() ? BigDecimal.valueOf(random.nextInt(200)) : BigDecimal.ZERO;
            BigDecimal totalAmount = subtotal.add(taxAmount).add(shippingCost).subtract(discountAmount);

            Order order = Order.builder()
                    .user(user)
                    .orderDate(LocalDateTime.now().minusDays(random.nextInt(365)))
                    .orderStatus(OrderStatus.values()[random.nextInt(OrderStatus.values().length)])
                    .subtotal(subtotal)
                    .taxAmount(taxAmount)
                    .shippingCost(shippingCost)
                    .discountAmount(discountAmount)
                    .totalAmount(totalAmount)
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
                    .paymentDate(order.getOrderDate().plusMinutes(random.nextInt(60)))
                    .amount(order.getTotalAmount())
                    .build();

            payments.add(paymentRepository.save(payment));
        }

        return payments;
    }

    private List<Review> createReviews(List<User> users, List<Product> products, List<Order> orders) {
        List<Review> reviews = new ArrayList<>();
        Random random = new Random();

        // Create 75 reviews
        for (int i = 0; i < 75; i++) {
            User user = users.get(random.nextInt(users.size()));
            Product product = products.get(random.nextInt(products.size()));
            Order order = orders.get(random.nextInt(orders.size()));

            String[] titles = {
                "Great product!", "Excellent quality", "Very satisfied", "Good value",
                "Amazing!", "Highly recommend", "Perfect!", "Worth the money",
                "Not bad", "Could be better", "Disappointed", "Outstanding"
            };

            String[] comments = {
                "This product exceeded my expectations. Highly recommended!",
                "Good quality product, fast delivery. Very happy with the purchase.",
                "Exactly what I was looking for. Works perfectly.",
                "The product is okay, but I expected more for the price.",
                "Excellent product! Will definitely buy again.",
                "Very satisfied with the quality and performance.",
                "Not as good as described, but still acceptable.",
                "Amazing product! Best purchase this year.",
                "Good product overall, minor issues but nothing major.",
                "Fantastic! Exceeded all my expectations."
            };

            Review review = Review.builder()
                    .user(user)
                    .product(product)
                    .orderId(order.getOrderId())
                    .reviewValue(random.nextInt(5) + 1) // 1-5 stars
                    .title(titles[random.nextInt(titles.length)])
                    .comment(comments[random.nextInt(comments.length)])
                    .isVerifiedPurchase(random.nextBoolean())
                    .createdAt(LocalDateTime.now().minusDays(random.nextInt(180)))
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
                        
                        OrderProduct orderProduct = OrderProduct.builder()
                                .orderId(order.getOrderId())
                                .productId(product.getProductId())
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

    private List<OrderCoupon> createOrderCoupons(List<Order> orders, List<Coupon> coupons) {
        List<OrderCoupon> orderCoupons = new ArrayList<>();
        Random random = new Random();

        // Apply coupons to about 30% of orders
        for (Order order : orders) {
            if (random.nextInt(100) < 30) { // 30% chance
                try {
                    Coupon coupon = coupons.get(random.nextInt(coupons.size()));
                    
                    OrderCoupon orderCoupon = OrderCoupon.builder()
                            .orderId(order.getOrderId())
                            .couponId(coupon.getCouponId())
                            .order(order)
                            .coupon(coupon)
                            .build();
                    
                    orderCoupons.add(orderCouponRepository.save(orderCoupon));
                } catch (Exception e) {
                    log.error("Error creating order coupon for order {}: ", order.getOrderId(), e);
                }
            }
        }

        return orderCoupons;
    }

    private List<Wishlist> createWishlists(List<User> users, List<Product> products) {
        List<Wishlist> wishlists = new ArrayList<>();
        Random random = new Random();

        String[] wishlistNames = {
            "My Wishlist", "Favorites", "Gift Ideas", "Want to Buy", 
            "Dream Products", "Shopping List", "Birthday Wishlist"
        };

        // Create wishlists for about 60% of users
        for (User user : users) {
            if (random.nextInt(100) < 60) { // 60% chance
                try {
                    // Random products for this wishlist
                    Set<Product> wishlistProducts = new HashSet<>();
                    int numProducts = random.nextInt(8) + 2; // 2-10 products
                    
                    for (int i = 0; i < numProducts && i < products.size(); i++) {
                        wishlistProducts.add(products.get(random.nextInt(products.size())));
                    }
                    
                    Wishlist wishlist = Wishlist.builder()
                            .user(user)
                            .name(wishlistNames[random.nextInt(wishlistNames.length)])
                            .products(wishlistProducts)
                            .build();
                    
                    wishlists.add(wishlistRepository.save(wishlist));
                } catch (Exception e) {
                    log.error("Error creating wishlist for user {}: ", user.getUserId(), e);
                }
            }
        }

        return wishlists;
    }
}
