-- ======================
-- Core reference tables
-- ======================
CREATE EXTENSION IF NOT EXISTS pg_cron;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE brand (
                       brand_id    UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                       name        TEXT NOT NULL,
                       description TEXT NOT NULL
);

CREATE TABLE category (
                          category_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                          name        TEXT NOT NULL,
                          description TEXT NOT NULL
);

CREATE TABLE warehouse (
                           warehouse_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                           name         TEXT NOT NULL,
                           address      TEXT NOT NULL,
                           city         TEXT NOT NULL,
                           postal_code  TEXT NOT NULL,
                           country      TEXT NOT NULL,
                           phone        TEXT NOT NULL
);

-- ======================
-- Users
-- ======================

CREATE TABLE "user" (
                        user_id   UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                        email         TEXT NOT NULL UNIQUE,
                        password      TEXT NOT NULL,
                        first_name    TEXT NOT NULL,
                        last_name     TEXT NOT NULL,
                        phone_number  TEXT NOT NULL,
                        date_of_birth DATE NOT NULL,
                        is_admin BOOLEAN DEFAULT FALSE
);

-- ======================
-- Address
-- ======================

CREATE TABLE address (
                         address_id   UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                         street TEXT NOT NULL,
                         street_number TEXT NOT NULL,
                         zip TEXT NOT NULL,
                         city TEXT NOT NULL,
                         user_id UUID NOT NULL REFERENCES "user"(user_id)
);

-- ======================
-- Coupon
-- ======================

CREATE TYPE discount_type_enum AS ENUM ('percentage', 'fixed_amount');

CREATE TABLE coupon (
                        coupon_id           UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                        code                TEXT NOT NULL UNIQUE,
                        discount_type       discount_type_enum NOT NULL,
                        discount_value      NUMERIC(12,2) NOT NULL,
                        minimum_order_value NUMERIC(12,2) NOT NULL,
                        expiry_date         DATE,
                        usage_limit         INTEGER NOT NULL,
                        times_used          INTEGER DEFAULT 0,
                        is_active           BOOLEAN DEFAULT TRUE
);

-- ======================
-- Warranty
-- ======================

CREATE TABLE warranty (
                          warranty_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                          start_date  DATE NOT NULL,
                          end_date    DATE NOT NULL,
                          description TEXT NOT NULL
);

-- ======================
-- Product
-- ======================

CREATE TABLE product (
                         product_id     UUID DEFAULT uuid_generate_v4() PRIMARY KEY,
                         brand_id       UUID NOT NULL REFERENCES brand(brand_id),
                         warranty_id     UUID NOT NULL REFERENCES warranty(warranty_id),
                         sku            TEXT UNIQUE NOT NULL,
                         name           TEXT NOT NULL,
                         description    TEXT NOT NULL,
                         price          NUMERIC(12,2) NOT NULL,
                         weight         NUMERIC(8,2)
);


-- ======================
-- Orders
-- ======================

CREATE TYPE order_type_enum AS ENUM ('pending', 'confirmed', 'processing', 'shipped', 'delivered', 'cancelled', 'returned');

CREATE TABLE "order" (
                         order_id         UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                         user_id      UUID NOT NULL REFERENCES "user"(user_id),
                         payment_method_id UUID,
                         shipping_address_id UUID,
                         order_date       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                         status           order_type_enum DEFAULT 'pending',
                         subtotal         NUMERIC(12,2) NOT NULL,
                         tax_amount       NUMERIC(12,2) NOT NULL,
                         shipping_cost    NUMERIC(12,2) NOT NULL,
                         discount_amount  NUMERIC(12,2),
                         total_amount     NUMERIC(12,2) NOT NULL
);

-- ======================
-- Payments
-- ======================

CREATE TYPE payment_type_enum AS ENUM('credit_card', 'paypal', 'bank', 'klarna', 'cash');
CREATE TYPE status_type_enum AS ENUM('pending', 'completed', 'failed', 'refunded');

CREATE TABLE payment (
                         payment_id     UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                         order_id       UUID NOT NULL REFERENCES "order"(order_id),
                         payment_method payment_type_enum NOT NULL,
                         amount         NUMERIC(12,2) NOT NULL,
                         status         status_type_enum DEFAULT 'pending',
                         payment_date   TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ======================
-- Review
-- ======================

CREATE TABLE review (
                        review_id           UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                        product_id          UUID NOT NULL REFERENCES product(product_id),
                        user_id         UUID NOT NULL REFERENCES "user"(user_id),
                        order_id            UUID NOT NULL REFERENCES "order"(order_id),
                        review_value        INTEGER CHECK (review_value >= 1 AND review_value <= 5),
                        title               TEXT,
                        comment             TEXT,
                        is_verified_purchase BOOLEAN DEFAULT FALSE,
                        created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ======================
-- Wishlist
-- ======================

CREATE TABLE wishlist (
                          wishlist_id UUID DEFAULT uuid_generate_v4() PRIMARY KEY,
                          user_id     UUID NOT NULL REFERENCES "user"(user_id),
                          name        TEXT NOT NULL
);

-- ======================
-- Join Tables
-- ======================

-- Order-Coupon many-to-many relationship
CREATE TABLE order_coupon (
                              order_id    UUID NOT NULL REFERENCES "order"(order_id) ON DELETE CASCADE,
                              coupon_id   UUID NOT NULL REFERENCES coupon(coupon_id) ON DELETE CASCADE,
                              PRIMARY KEY (order_id, coupon_id)
);

-- Order-Product many-to-many relationship (order line items)
CREATE TABLE order_product (
                               order_id    UUID NOT NULL REFERENCES "order"(order_id) ON DELETE CASCADE,
                               product_id  UUID NOT NULL REFERENCES product(product_id) ON DELETE RESTRICT,
                               quantity    INTEGER NOT NULL CHECK (quantity > 0),
                               unit_price  NUMERIC(12,2) NOT NULL,
                               total_price NUMERIC(12,2) NOT NULL,
                               PRIMARY KEY (order_id, product_id)
);

-- Wishlist-Product many-to-many relationship
CREATE TABLE wishlist_product (
                                  wishlist_id UUID NOT NULL REFERENCES wishlist(wishlist_id) ON DELETE CASCADE,
                                  product_id  UUID NOT NULL REFERENCES product(product_id) ON DELETE CASCADE,
                                  PRIMARY KEY (wishlist_id, product_id)
);

-- Warehouse-Product many-to-many relationship with stock tracking
CREATE TABLE warehouse_product (
                                   warehouse_id   UUID NOT NULL REFERENCES warehouse(warehouse_id) ON DELETE CASCADE,
                                   product_id     UUID NOT NULL REFERENCES product(product_id) ON DELETE CASCADE,
                                   stock_quantity INTEGER NOT NULL DEFAULT 0 CHECK (stock_quantity >= 0),
                                   PRIMARY KEY (warehouse_id, product_id)
);

CREATE TABLE product_category (
                                  product_id  UUID NOT NULL,
                                  category_id UUID NOT NULL,
                                  PRIMARY KEY (product_id, category_id),
                                  FOREIGN KEY (product_id) REFERENCES product(product_id) ON DELETE CASCADE,
                                  FOREIGN KEY (category_id) REFERENCES category(category_id) ON DELETE CASCADE
);

-- =========================================
-- PROCEDURES
-- =========================================

-- 1. Process an order: confirm, update stock, and mark payment
-- Updated to work with warehouse_product table
CREATE OR REPLACE PROCEDURE sp_ProcessOrder(p_order_id UUID)
LANGUAGE plpgsql
AS $$
DECLARE
    rec RECORD;
    total_stock INT;
BEGIN
    -- Loop through each product in the order
    FOR rec IN
        SELECT product_id, quantity
        FROM order_product
        WHERE order_id = p_order_id
    LOOP
        -- Check total stock across all warehouses
        SELECT COALESCE(SUM(stock_quantity), 0) INTO total_stock
        FROM warehouse_product
        WHERE product_id = rec.product_id;

        IF total_stock < rec.quantity THEN
            RAISE EXCEPTION 'Not enough stock for product %. Available: %, Needed: %', 
                rec.product_id, total_stock, rec.quantity;
        END IF;

        -- Deduct stock from warehouse(s) with available inventory
        -- This is a simple FIFO approach - deduct from warehouses in order
        DECLARE
            remaining_qty INT := rec.quantity;
            warehouse_rec RECORD;
        BEGIN
            FOR warehouse_rec IN
                SELECT warehouse_id, stock_quantity
                FROM warehouse_product
                WHERE product_id = rec.product_id AND stock_quantity > 0
                ORDER BY warehouse_id
            LOOP
                IF remaining_qty <= 0 THEN
                    EXIT;
                END IF;

                IF warehouse_rec.stock_quantity >= remaining_qty THEN
                    -- This warehouse has enough stock
                    UPDATE warehouse_product
                    SET stock_quantity = stock_quantity - remaining_qty
                    WHERE warehouse_id = warehouse_rec.warehouse_id
                      AND product_id = rec.product_id;
                    remaining_qty := 0;
                ELSE
                    -- Use all stock from this warehouse and continue
                    UPDATE warehouse_product
                    SET stock_quantity = 0
                    WHERE warehouse_id = warehouse_rec.warehouse_id
                      AND product_id = rec.product_id;
                    remaining_qty := remaining_qty - warehouse_rec.stock_quantity;
                END IF;
            END LOOP;
        END;
    END LOOP;

    -- Update order status
    UPDATE "order"
    SET status = 'confirmed'
    WHERE order_id = p_order_id;

    -- Process payment: mark as completed (simplified)
    UPDATE payment
    SET status = 'completed', payment_date = CURRENT_TIMESTAMP
    WHERE order_id = p_order_id;

    RAISE NOTICE 'Order % processed successfully', p_order_id;
END;
$$;

-- 2. Calculate shipping cost
CREATE OR REPLACE PROCEDURE sp_CalculateShipping(
    p_order_id UUID,
    OUT shipping_cost NUMERIC
)
LANGUAGE plpgsql
AS $$
DECLARE
total_weight NUMERIC := 0;
BEGIN
SELECT COALESCE(SUM(p.weight * op.quantity),0) INTO total_weight
FROM order_product op
         JOIN product p ON op.product_id = p.product_id
WHERE op.order_id = p_order_id;

-- Simple formula: $5 per kg
shipping_cost := total_weight * 5;

    -- Update order total_amount to include shipping
UPDATE "order"
SET shipping_cost = shipping_cost,
    total_amount = subtotal + tax_amount + shipping_cost - COALESCE(discount_amount,0)
WHERE order_id = p_order_id;

RAISE NOTICE 'Shipping cost for order %: %', p_order_id, shipping_cost;
END;
$$;

-- 3. Update inventory in warehouse and global stock
CREATE OR REPLACE PROCEDURE sp_UpdateInventory(
    p_product_id UUID,
    p_warehouse_id UUID,
    p_quantity INT
)
LANGUAGE plpgsql
AS $$
BEGIN
    -- Update warehouse stock
    IF EXISTS (
        SELECT 1 FROM warehouse_product
        WHERE warehouse_id = p_warehouse_id AND product_id = p_product_id
    ) THEN
UPDATE warehouse_product
SET stock_quantity = stock_quantity + p_quantity,
    last_updated = CURRENT_TIMESTAMP
WHERE warehouse_id = p_warehouse_id AND product_id = p_product_id;
ELSE
        INSERT INTO warehouse_product(warehouse_id, product_id, stock_quantity)
        VALUES (p_warehouse_id, p_product_id, p_quantity);
END IF;

    -- Update global product stock
UPDATE products
SET stock_quantity = stock_quantity + p_quantity
WHERE product_id = p_product_id;

RAISE NOTICE 'Inventory updated for product % in warehouse %', p_product_id, p_warehouse_id;
END;
$$;


-- 5. Generate monthly sales report (example procedure)
CREATE OR REPLACE PROCEDURE evt_GenerateMonthlyReports(p_month DATE)
LANGUAGE plpgsql
AS $$
DECLARE
total_orders INT;
    total_revenue NUMERIC(12,2);
BEGIN
SELECT COUNT(*), COALESCE(SUM(total_amount),0)
INTO total_orders, total_revenue
FROM "order"
WHERE date_trunc('month', order_date) = date_trunc('month', p_month);

-- You could insert into a report table, or just raise notice
RAISE NOTICE 'Month: %, Total Orders: %, Total Revenue: %', p_month, total_orders, total_revenue;
END;
$$;

-- VIEWS
CREATE OR REPLACE VIEW vw_LowStockProducts AS
SELECT
    p.product_id,
    p.name AS product_name,
    p.sku,
    b.name AS brand_name,
    p.price,
    p.description,
    COALESCE(SUM(wp.stock_quantity), 0) AS total_stock_across_warehouses
FROM product p
         JOIN brand b ON p.brand_id = b.brand_id
         LEFT JOIN warehouse_product wp ON p.product_id = wp.product_id
GROUP BY p.product_id, p.name, p.sku, b.name, p.price, p.description
HAVING COALESCE(SUM(wp.stock_quantity), 0) < 10
ORDER BY total_stock_across_warehouses ASC;


CREATE OR REPLACE VIEW vw_BestSellingProducts AS
SELECT
    p.product_id,
    p.name AS product_name,
    b.name AS brand_name,
    SUM(op.quantity) AS total_units_sold,
    SUM(op.total_price) AS total_revenue,
    COUNT(DISTINCT op.order_id) AS total_orders
FROM order_product op
JOIN product p ON op.product_id = p.product_id
JOIN brand b ON p.brand_id = b.brand_id
GROUP BY p.product_id, p.name, b.name
ORDER BY total_units_sold DESC;

-- 6. Update product rankings based on total orders
CREATE OR REPLACE PROCEDURE evt_UpdateProductRankings()
LANGUAGE plpgsql
AS $$
BEGIN
UPDATE product p
SET stock_quantity = p.stock_quantity -- just placeholder, you could add popularity_score column
    FROM (
        SELECT op.product_id, SUM(op.quantity) AS total_sold
        FROM order_product op
        GROUP BY op.product_id
    ) AS sub
WHERE p.product_id = sub.product_id;

RAISE NOTICE 'Product rankings updated';
END;
$$;



CREATE OR REPLACE FUNCTION fn_GetProductRating(p_product_id UUID)
RETURNS NUMERIC(3,2)
LANGUAGE plpgsql
AS $$
DECLARE
avg_rating NUMERIC;
BEGIN
SELECT ROUND(AVG(review_value), 2)
INTO avg_rating
FROM review
WHERE product_id = p_product_id;

RETURN COALESCE(avg_rating, 0);
END;
$$;


CREATE OR REPLACE FUNCTION fn_CalculateUserLifetimeValue(p_user_id UUID)
RETURNS NUMERIC(12,2)
LANGUAGE plpgsql
AS $$
DECLARE
total_spent NUMERIC;
BEGIN
SELECT COALESCE(SUM(total_amount), 0)
INTO total_spent
FROM "order"
WHERE user_id = p_user_id;

RETURN total_spent;
END;
$$;

-- ======================
-- TRIGGERS
-- ======================

-- 1. Automatically increment coupon usage when applied to an order
CREATE OR REPLACE FUNCTION increment_coupon_usage()
RETURNS TRIGGER AS $$
BEGIN
UPDATE coupon
SET times_used = times_used + 1
WHERE coupon_id = NEW.coupon_id;
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_increment_coupon_usage
    AFTER INSERT ON order_coupon
    FOR EACH ROW
    EXECUTE FUNCTION increment_coupon_usage();

-- 2. Validate coupon before applying to order
CREATE OR REPLACE FUNCTION validate_coupon()
RETURNS TRIGGER AS $$
DECLARE
v_coupon RECORD;
  v_order_total NUMERIC(12,2);
BEGIN
  -- Get coupon details
SELECT * INTO v_coupon
FROM coupon
WHERE coupon_id = NEW.coupon_id;

-- Get order total
SELECT total_amount INTO v_order_total
FROM "order"
WHERE order_id = NEW.order_id;

-- Check if coupon is active
IF NOT v_coupon.is_active THEN
    RAISE EXCEPTION 'Coupon is not active';
END IF;

  -- Check if coupon has expired
  IF v_coupon.expiry_date IS NOT NULL AND v_coupon.expiry_date < CURRENT_DATE THEN
    RAISE EXCEPTION 'Coupon has expired';
END IF;

  -- Check usage limit
  IF v_coupon.times_used >= v_coupon.usage_limit THEN
    RAISE EXCEPTION 'Coupon usage limit exceeded';
END IF;

  -- Check minimum order value
  IF v_order_total < v_coupon.minimum_order_value THEN
    RAISE EXCEPTION 'Order total does not meet minimum order value for this coupon';
END IF;

RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_validate_coupon
    BEFORE INSERT ON order_coupon
    FOR EACH ROW
    EXECUTE FUNCTION validate_coupon();

-- 3. Check stock availability when order is placed
-- Updated to work with warehouse_product table
CREATE OR REPLACE FUNCTION check_product_stock()
RETURNS TRIGGER AS $$
DECLARE
    total_stock INT;
BEGIN
    -- Check total stock across all warehouses
    SELECT COALESCE(SUM(stock_quantity), 0) INTO total_stock
    FROM warehouse_product
    WHERE product_id = NEW.product_id;

    IF total_stock < NEW.quantity THEN
        RAISE EXCEPTION 'Insufficient stock for product %. Available: %, Requested: %', 
            NEW.product_id, total_stock, NEW.quantity;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_check_product_stock
    BEFORE INSERT ON order_product
    FOR EACH ROW
    EXECUTE FUNCTION check_product_stock();

-- 4. Restore product stock when order is cancelled
-- Updated to work with warehouse_product table
CREATE OR REPLACE FUNCTION restore_product_stock()
RETURNS TRIGGER AS $$
BEGIN
    -- Only restore stock if order status changed to cancelled or returned
    IF NEW.status IN ('cancelled', 'returned') AND OLD.status NOT IN ('cancelled', 'returned') THEN
        -- Restore stock to the first available warehouse for each product
        -- In a real system, you'd track which warehouse the stock came from
        UPDATE warehouse_product wp
        SET stock_quantity = stock_quantity + op.quantity
        FROM order_product op
        WHERE op.order_id = NEW.order_id
          AND wp.product_id = op.product_id
          AND wp.warehouse_id = (
              SELECT warehouse_id 
              FROM warehouse_product 
              WHERE product_id = op.product_id 
              LIMIT 1
          );
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_restore_product_stock
    AFTER UPDATE ON "order"
    FOR EACH ROW
    EXECUTE FUNCTION restore_product_stock();


SELECT cron.schedule('deactivate-expired-coupons', '0 0 * * *', $$
    UPDATE coupon
  SET is_active = FALSE
  WHERE expiry_date < CURRENT_DATE AND is_active = TRUE
$$);

SELECT cron.schedule('cancel-old-pending-orders', '0 2 * * *', $$
    UPDATE "order"
  SET status = 'cancelled'
  WHERE status = 'pending'
  AND order_date < CURRENT_TIMESTAMP - INTERVAL '7 days'
$$);