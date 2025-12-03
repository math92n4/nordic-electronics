CREATE EXTENSION IF NOT EXISTS pg_cron;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE brand (
                       brand_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                       name VARCHAR(100) NOT NULL,
                       description VARCHAR(500) NOT NULL,
                       created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       deleted_at TIMESTAMP NULL
);

CREATE TABLE category (
                          category_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                          name VARCHAR(100) NOT NULL,
                          description VARCHAR(500) NOT NULL,
                          created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          deleted_at TIMESTAMP NULL
);

-- ======================
-- Users
-- ======================
CREATE TABLE "user" (
                        user_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                        first_name VARCHAR(50) NOT NULL,
                        last_name VARCHAR(50) NOT NULL,
                        email VARCHAR(255) NOT NULL UNIQUE,
                        phone_number VARCHAR(8) NOT NULL,  -- Danish prefix + 8 digits
                        date_of_birth DATE NOT NULL,
                        password VARCHAR(64) NOT NULL,  -- BVA: 8-64 characters
                        is_admin BOOLEAN DEFAULT FALSE,
                        created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        deleted_at TIMESTAMP NULL
);

-- ======================
-- Address
-- ======================
CREATE TABLE address (
                         address_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                         user_id UUID NOT NULL REFERENCES "user"(user_id),
                         street VARCHAR(100) NOT NULL,
                         street_number VARCHAR(10) NOT NULL,
                         zip VARCHAR(4) NOT NULL,  -- Danish zip codes are 4 digits
                         city VARCHAR(100) NOT NULL,
                         created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                         updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                         deleted_at TIMESTAMP NULL,
                         FOREIGN KEY (user_id) REFERENCES "user"(user_id)
);

CREATE TABLE warehouse (
                           warehouse_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                           name VARCHAR(100) NOT NULL,
                           phone_number VARCHAR(8) NOT NULL,
                           created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                           updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                           deleted_at TIMESTAMP NULL,
                           address_id UUID NOT NULL REFERENCES address(address_id)
);

-- ======================
-- Coupon
-- ======================
CREATE TYPE discount_type_enum AS ENUM ('percentage', 'fixed_amount');
CREATE TABLE coupon (
                        coupon_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                        code VARCHAR(50) NOT NULL UNIQUE,
                        discount_type discount_type_enum NOT NULL,
                        discount_value NUMERIC(5, 2) NOT NULL CHECK (discount_value >= 0 AND discount_value <= 100),  -- BVA: 0-100%
                        minimum_order_value NUMERIC(12, 2) NOT NULL,
                        expiry_date DATE,
                        usage_limit INTEGER NOT NULL CHECK (usage_limit >= 1 AND usage_limit <= 50),  -- BVA: 1-50 uses
                        times_used INTEGER DEFAULT 0 CHECK (times_used >= 0),
                        is_active BOOLEAN DEFAULT TRUE,
                        created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        deleted_at TIMESTAMP NULL
);

-- ======================
-- Warranty
-- ======================
CREATE TABLE warranty (
                          warranty_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                          start_date DATE NOT NULL,
                          end_date DATE NOT NULL,
                          description VARCHAR(500) NOT NULL,
                          created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          deleted_at TIMESTAMP NULL
);

-- ======================
-- Product
-- ======================
CREATE TABLE product (
                         product_id UUID DEFAULT uuid_generate_v4() PRIMARY KEY,
                         sku VARCHAR(50) UNIQUE NOT NULL,
                         name VARCHAR(200) NOT NULL,
                         description VARCHAR(2000) NOT NULL,
                         price NUMERIC(12, 2) NOT NULL CHECK (price >= 0),  -- BVA: 0 to DECIMAL_MAX
                         weight NUMERIC(8, 2),
                         created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                         updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                         deleted_at TIMESTAMP NULL,
                         brand_id UUID NOT NULL REFERENCES brand(brand_id),
                         warranty_id UUID NOT NULL REFERENCES warranty(warranty_id)
);

-- ======================
-- Orders
-- ======================
CREATE TYPE order_type_enum AS ENUM (
  'pending', 'confirmed', 'processing',
  'shipped', 'delivered', 'cancelled',
  'returned'
);

CREATE TABLE "order" (
                         order_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                         user_id UUID NOT NULL REFERENCES "user"(user_id),
                         address_id UUID REFERENCES address(address_id),
                         coupon_id UUID REFERENCES coupon(coupon_id),
                         order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                         status order_type_enum DEFAULT 'pending',
                         total_amount NUMERIC(12, 2) NOT NULL,
                         subtotal NUMERIC(12, 2) NOT NULL,
                         tax_amount NUMERIC(12, 2) NOT NULL,
                         shipping_cost NUMERIC(12, 2) NOT NULL,
                         discount_amount NUMERIC(12, 2),
                         created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                         updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                         deleted_at TIMESTAMP NULL
);

-- ======================
-- Payments
-- ======================
CREATE TYPE payment_type_enum AS ENUM(
  'credit_card', 'paypal', 'bank', 'klarna',
  'cash'
);

CREATE TYPE status_type_enum AS ENUM(
  'pending', 'completed', 'failed',
  'refunded'
);

CREATE TABLE payment (
                         payment_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                         order_id UUID NOT NULL REFERENCES "order"(order_id),
                         payment_method payment_type_enum NOT NULL,
                         amount NUMERIC(12, 2) NOT NULL,
                         status status_type_enum DEFAULT 'pending',
                         payment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                         created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                         updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                         deleted_at TIMESTAMP NULL
);

-- ======================
-- Review
-- ======================
CREATE TABLE review (
                        review_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                        product_id UUID NOT NULL REFERENCES product(product_id),
                        user_id UUID NOT NULL REFERENCES "user"(user_id),
                        order_id UUID NOT NULL REFERENCES "order"(order_id),
                        review_value INTEGER CHECK (
                            review_value >= 1
                                AND review_value <= 5  -- BVA: 1-5 star rating
                            ),
                        title VARCHAR(200) NOT NULL,
                        comment VARCHAR(2000) NOT NULL,
                        is_verified_purchase BOOLEAN DEFAULT FALSE,
                        created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        deleted_at TIMESTAMP NULL
);

-- ======================
-- Join Tables
-- ======================
-- Order-Product many-to-many relationship (order line items)
CREATE TABLE order_product (
                               order_id UUID NOT NULL REFERENCES "order"(order_id) ON DELETE CASCADE,
                               product_id UUID NOT NULL REFERENCES product(product_id) ON DELETE RESTRICT,
                               quantity INTEGER NOT NULL CHECK (quantity >= 1 AND quantity <= 50),  -- BVA: 1-50 items per order
                               unit_price NUMERIC(12, 2) NOT NULL,
                               total_price NUMERIC(12, 2) NOT NULL,
                               PRIMARY KEY (order_id, product_id)
);

-- Warehouse-Product many-to-many relationship with stock tracking
CREATE TABLE warehouse_product (
                                   warehouse_id UUID NOT NULL REFERENCES warehouse(warehouse_id) ON DELETE CASCADE,
                                   product_id UUID NOT NULL REFERENCES product(product_id) ON DELETE CASCADE,
                                   stock_quantity INTEGER NOT NULL DEFAULT 0 CHECK (stock_quantity >= 0),  -- BVA: 0 to max stock, no negative
                                   PRIMARY KEY (warehouse_id, product_id)
);

CREATE TABLE product_category (
                                  product_id UUID NOT NULL,
                                  category_id UUID NOT NULL,
                                  PRIMARY KEY (product_id, category_id),
                                  FOREIGN KEY (product_id) REFERENCES product(product_id) ON DELETE CASCADE,
                                  FOREIGN KEY (category_id) REFERENCES category(category_id) ON DELETE CASCADE
);

-- ==============================================
-- AUDIT SOLUTION - Simple audit log for tracking changes
-- ==============================================

-- Simple audit log table
CREATE TABLE audit_log (
                           audit_id SERIAL PRIMARY KEY,
                           table_name VARCHAR(100) NOT NULL,
                           operation VARCHAR(20) NOT NULL,
                           record_id VARCHAR(100) NOT NULL,
                           before_values JSONB,
                           after_values JSONB,
                           changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                           changed_by VARCHAR(100) DEFAULT current_user
);

-- Simple audit function
CREATE OR REPLACE FUNCTION log_audit()
RETURNS TRIGGER AS $$
DECLARE
rec_id TEXT;
BEGIN
    -- Get the record ID based on table
    IF TG_OP = 'DELETE' THEN
        rec_id := (to_jsonb(OLD)->>(TG_ARGV[0]))::TEXT;
INSERT INTO audit_log (table_name, operation, record_id, before_values)
VALUES (TG_TABLE_NAME, 'DELETE', rec_id, to_jsonb(OLD));
RETURN OLD;
ELSIF TG_OP = 'UPDATE' THEN
        rec_id := (to_jsonb(NEW)->>(TG_ARGV[0]))::TEXT;
INSERT INTO audit_log (table_name, operation, record_id, before_values, after_values)
VALUES (TG_TABLE_NAME, 'UPDATE', rec_id, to_jsonb(OLD), to_jsonb(NEW));
RETURN NEW;
ELSIF TG_OP = 'INSERT' THEN
        rec_id := (to_jsonb(NEW)->>(TG_ARGV[0]))::TEXT;
INSERT INTO audit_log (table_name, operation, record_id, after_values)
VALUES (TG_TABLE_NAME, 'INSERT', rec_id, to_jsonb(NEW));
RETURN NEW;
END IF;
RETURN NULL;
END;
$$ LANGUAGE plpgsql;

-- Apply audit trigger to Product table (most critical for e-commerce)
CREATE TRIGGER product_audit_trigger
    AFTER INSERT OR UPDATE OR DELETE ON product
    FOR EACH ROW EXECUTE FUNCTION log_audit('product_id');

-- Apply audit trigger to Order table (track order changes)
CREATE TRIGGER order_audit_trigger
    AFTER INSERT OR UPDATE OR DELETE ON "order"
    FOR EACH ROW EXECUTE FUNCTION log_audit('order_id');

-- Apply audit trigger to Payment table (financial tracking)
CREATE TRIGGER payment_audit_trigger
    AFTER INSERT OR UPDATE OR DELETE ON payment
    FOR EACH ROW EXECUTE FUNCTION log_audit('payment_id');

-- Apply audit trigger to Review table (customer feedback)
CREATE TRIGGER review_audit_trigger
    AFTER INSERT OR UPDATE OR DELETE ON review
    FOR EACH ROW EXECUTE FUNCTION log_audit('review_id');

-- Apply audit trigger to User table (user account tracking)
CREATE TRIGGER user_audit_trigger
    AFTER INSERT OR UPDATE OR DELETE ON "user"
    FOR EACH ROW EXECUTE FUNCTION log_audit('user_id');

-- ==============================================
-- INDEXES
-- ==============================================

-- Index product on product_id (enhances lookup speed for product details)
CREATE INDEX IF NOT EXISTS idx_product_product_id ON product(product_id);

-- Index product on brand_id (facilitates faster queries for products by brand)
CREATE INDEX IF NOT EXISTS idx_product_brand_id ON product(brand_id);

-- Index product_category on category_id (improves performance of product searches by category)
CREATE INDEX IF NOT EXISTS idx_product_category_category_id ON product_category(category_id);

-- ==============================================
-- MATERIALIZED VIEWS
-- ==============================================

-- Best Selling Products View: Top 10 products by sales volume in the last month
CREATE MATERIALIZED VIEW IF NOT EXISTS mv_best_selling_products AS
SELECT
    p.product_id,
    p.name AS product_name,
    p.price AS product_price,
    SUM(op.quantity) AS total_units_sold,
    SUM(op.total_price) AS revenue_generated
FROM product p
         INNER JOIN order_product op ON p.product_id = op.product_id
         INNER JOIN "order" o ON op.order_id = o.order_id
WHERE o.order_date >= CURRENT_DATE - INTERVAL '1 month'
  AND o.deleted_at IS NULL
  AND p.deleted_at IS NULL
GROUP BY p.product_id, p.name, p.price
ORDER BY total_units_sold DESC
    LIMIT 10;

-- UNIQUE index required for CONCURRENTLY refresh
CREATE UNIQUE INDEX IF NOT EXISTS idx_mv_best_selling_product_id
    ON mv_best_selling_products(product_id);

CREATE INDEX IF NOT EXISTS idx_mv_best_selling_units_sold
    ON mv_best_selling_products(total_units_sold DESC);


-- Best Reviewed Products View: Top 10 products by average customer rating
CREATE MATERIALIZED VIEW IF NOT EXISTS mv_best_reviewed_products AS
SELECT
    p.product_id,
    p.name AS product_name,
    p.price AS product_price,
    ROUND(AVG(r.review_value)::NUMERIC, 2) AS average_rating,
    COUNT(r.review_id) AS number_of_reviews
FROM product p
         INNER JOIN review r ON p.product_id = r.product_id
WHERE r.deleted_at IS NULL
  AND p.deleted_at IS NULL
GROUP BY p.product_id, p.name
HAVING COUNT(r.review_id) >= 1
ORDER BY average_rating DESC, number_of_reviews DESC
    LIMIT 10;

-- UNIQUE index required for CONCURRENTLY refresh
CREATE UNIQUE INDEX IF NOT EXISTS idx_mv_best_reviewed_product_id
    ON mv_best_reviewed_products(product_id);

-- Index for fast ordered retrieval of best-reviewed products
CREATE INDEX IF NOT EXISTS idx_mv_best_reviewed_rating_reviews
    ON mv_best_reviewed_products(average_rating DESC, number_of_reviews DESC);

-- Function to refresh all materialized views
CREATE OR REPLACE FUNCTION fn_refresh_materialized_views()
RETURNS void AS $$
BEGIN
    REFRESH MATERIALIZED VIEW CONCURRENTLY mv_best_selling_products;
    REFRESH MATERIALIZED VIEW CONCURRENTLY mv_best_reviewed_products;
EXCEPTION
    WHEN OTHERS THEN
        RAISE NOTICE 'Error refreshing materialized views: %', SQLERRM;
        -- Fallback to non-concurrent refresh
        REFRESH MATERIALIZED VIEW mv_best_selling_products;
        REFRESH MATERIALIZED VIEW mv_best_reviewed_products;
END;
$$ LANGUAGE plpgsql;

-- ==============================================
-- STORED FUNCTIONS
-- ==============================================

-- Calculate Order Total Function: Returns final amount after applying subtotal, taxes, shipping, and discounts
CREATE OR REPLACE FUNCTION fn_calculate_order_total(
    p_order_id UUID
)
RETURNS NUMERIC(12, 2) AS $$
DECLARE
v_subtotal NUMERIC(12, 2);
    v_tax_amount NUMERIC(12, 2);
    v_shipping_cost NUMERIC(12, 2);
    v_discount_amount NUMERIC(12, 2);
    v_total NUMERIC(12, 2);
BEGIN
SELECT
    subtotal,
    tax_amount,
    shipping_cost,
    COALESCE(discount_amount, 0)
INTO v_subtotal, v_tax_amount, v_shipping_cost, v_discount_amount
FROM "order"
WHERE order_id = p_order_id
  AND deleted_at IS NULL;

IF v_subtotal IS NULL THEN
        RAISE EXCEPTION 'Order not found: %', p_order_id;
END IF;
    
    v_total := v_subtotal + v_tax_amount + v_shipping_cost - v_discount_amount;

RETURN GREATEST(v_total, 0);
END;
$$ LANGUAGE plpgsql;

-- Check Product Availability Function: Returns boolean indicating sufficient stock
CREATE OR REPLACE FUNCTION fn_check_product_availability(
    p_product_id UUID,
    p_quantity INTEGER,
    p_warehouse_id UUID DEFAULT NULL
)
RETURNS BOOLEAN AS $$
DECLARE
v_available_stock INTEGER;
BEGIN
    IF p_warehouse_id IS NOT NULL THEN
        -- Check specific warehouse
SELECT stock_quantity INTO v_available_stock
FROM warehouse_product
WHERE product_id = p_product_id
  AND warehouse_id = p_warehouse_id;

RETURN COALESCE(v_available_stock, 0) >= p_quantity;
ELSE
        -- Check total stock across all warehouses
SELECT SUM(stock_quantity) INTO v_available_stock
FROM warehouse_product
WHERE product_id = p_product_id;

RETURN COALESCE(v_available_stock, 0) >= p_quantity;
END IF;
END;
$$ LANGUAGE plpgsql;

/*-- Validate Coupon Function: Returns discount amount for a given coupon code and order
CREATE OR REPLACE FUNCTION fn_validate_coupon(
    p_coupon_code TEXT,
    p_order_subtotal NUMERIC(12, 2)
)
RETURNS NUMERIC(12, 2) AS $$
DECLARE
v_coupon coupon%ROWTYPE;
    v_discount_amount NUMERIC(12, 2);
BEGIN
    -- Get coupon details
SELECT * INTO v_coupon
FROM coupon
WHERE code = p_coupon_code
  AND is_active = TRUE
  AND deleted_at IS NULL
  AND (expiry_date IS NULL OR expiry_date >= CURRENT_DATE)
  AND times_used < usage_limit;

IF NOT FOUND THEN
        RAISE EXCEPTION 'Invalid or expired coupon code: %', p_coupon_code;
END IF;
    
    -- Check minimum order value
    IF p_order_subtotal < v_coupon.minimum_order_value THEN
        RAISE EXCEPTION 'Order subtotal (%) is below minimum required (%) for coupon %', 
            p_order_subtotal, v_coupon.minimum_order_value, p_coupon_code;
END IF;
    
    -- Calculate discount amount
    IF v_coupon.discount_type = 'percentage' THEN
        v_discount_amount := (p_order_subtotal * v_coupon.discount_value) / 100;
ELSE
        v_discount_amount := v_coupon.discount_value;
END IF;
    
    -- Ensure discount doesn't exceed subtotal
    v_discount_amount := LEAST(v_discount_amount, p_order_subtotal);

RETURN v_discount_amount;
END;
$$ LANGUAGE plpgsql;*/

-- Get Product Rating Function: Returns average customer rating for a specified product
CREATE OR REPLACE FUNCTION fn_get_product_rating(
    p_product_id UUID
)
RETURNS NUMERIC(3, 2) AS $$
DECLARE
v_average_rating NUMERIC(3, 2);
BEGIN
SELECT ROUND(AVG(review_value)::NUMERIC, 2) INTO v_average_rating
FROM review
WHERE product_id = p_product_id
  AND deleted_at IS NULL;

RETURN COALESCE(v_average_rating, 0);
END;
$$ LANGUAGE plpgsql;

-- ==============================================
-- STORED PROCEDURES
-- ==============================================

CREATE OR REPLACE PROCEDURE sp_place_order(
    p_user_id UUID,
    p_address_id UUID,
    p_order_items JSONB,
    p_coupon_id UUID,
    p_discount_amount NUMERIC(12, 2)
)
LANGUAGE plpgsql
AS $$
DECLARE
v_order_id UUID;
    v_subtotal NUMERIC(12, 2) := 0;
    v_shipping_cost NUMERIC(12, 2) := 50.00;
    v_total NUMERIC(12, 2);
    v_item JSONB;
    v_product_price NUMERIC(12, 2);
    v_item_total NUMERIC(12, 2);
BEGIN

    -- Calculate subtotal and validate stock availability
FOR v_item IN SELECT * FROM jsonb_array_elements(p_order_items)
                                LOOP
SELECT price INTO v_product_price
FROM product
WHERE product_id = (v_item->>'product_id')::UUID
          AND deleted_at IS NULL;

IF NOT FOUND THEN
            RAISE EXCEPTION 'Product not found: %', v_item->>'product_id';
END IF;

        IF NOT fn_check_product_availability(
            (v_item->>'product_id')::UUID,
            (v_item->>'quantity')::INTEGER,
            CASE WHEN v_item->>'warehouse_id' IS NOT NULL
                 THEN (v_item->>'warehouse_id')::UUID
                 ELSE NULL END
        ) THEN
            RAISE EXCEPTION 'Insufficient stock for product: %', v_item->>'product_id';
END IF;

        v_item_total := v_product_price * (v_item->>'quantity')::INTEGER;
        v_subtotal := v_subtotal + v_item_total;
END LOOP;
    -- Calculate total
        v_total := v_subtotal + v_shipping_cost - COALESCE(p_discount_amount, 0);

    -- Create order
INSERT INTO "order" (
    user_id,
    address_id,
    coupon_id,
    subtotal,
    tax_amount,
    shipping_cost,
    discount_amount,
    total_amount,
    status
) VALUES (
             p_user_id,
             p_address_id,
             p_coupon_id,
             v_subtotal,
             0,
             v_shipping_cost,
             COALESCE(p_discount_amount, 0),
             v_total,
             'pending'
         ) RETURNING order_id INTO v_order_id;

-- Create order items and update inventory
FOR v_item IN SELECT * FROM jsonb_array_elements(p_order_items)
                                LOOP
SELECT price INTO v_product_price
FROM product
WHERE product_id = (v_item->>'product_id')::UUID;

v_item_total := v_product_price * (v_item->>'quantity')::INTEGER;

INSERT INTO order_product (
    order_id,
    product_id,
    quantity,
    unit_price,
    total_price
) VALUES (
             v_order_id,
             (v_item->>'product_id')::UUID,
             (v_item->>'quantity')::INTEGER,
             v_product_price,
             v_item_total
         );

-- Update warehouse inventory
IF v_item->>'warehouse_id' IS NOT NULL THEN
UPDATE warehouse_product
SET stock_quantity = stock_quantity - (v_item->>'quantity')::INTEGER
WHERE product_id = (v_item->>'product_id')::UUID
  AND warehouse_id = (v_item->>'warehouse_id')::UUID;
ELSE
            -- Update first warehouse that has enough stock
            -- Fixed: Use product_id and warehouse_id as the composite key
UPDATE warehouse_product
SET stock_quantity = stock_quantity - (v_item->>'quantity')::INTEGER
WHERE (product_id, warehouse_id) = (
    SELECT product_id, warehouse_id
    FROM warehouse_product
    WHERE product_id = (v_item->>'product_id')::UUID
  AND stock_quantity >= (v_item->>'quantity')::INTEGER
    ORDER BY warehouse_id
    LIMIT 1
    );
END IF;
END LOOP;

    IF p_coupon_id IS NOT NULL THEN
UPDATE coupon
SET times_used = times_used + 1,
    updated_at = CURRENT_TIMESTAMP
WHERE coupon_id = p_coupon_id;
END IF;
END;
$$;

-- ==============================================
-- TRIGGERS
-- ==============================================

-- Payment Status Trigger: When payment status changes to "completed", update order status to "confirmed"
CREATE OR REPLACE FUNCTION fn_payment_status_trigger()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.status = 'completed' AND (OLD.status IS NULL OR OLD.status != 'completed') THEN
UPDATE "order"
SET status = 'confirmed',
    updated_at = CURRENT_TIMESTAMP
WHERE order_id = NEW.order_id
  AND status = 'pending';
END IF;

RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER payment_status_trigger
    AFTER UPDATE ON payment
    FOR EACH ROW
    WHEN (NEW.status = 'completed' AND (OLD.status IS NULL OR OLD.status != 'completed'))
    EXECUTE FUNCTION fn_payment_status_trigger();

-- Order Cancelled Trigger: When order status is updated to "cancelled", restore inventory
CREATE OR REPLACE FUNCTION fn_order_cancelled_trigger()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.status = 'cancelled' AND (OLD.status IS NULL OR OLD.status != 'cancelled') THEN
        -- Restore inventory for all order items
UPDATE warehouse_product wp
SET stock_quantity = wp.stock_quantity + op.quantity
    FROM order_product op
WHERE op.order_id = NEW.order_id
  AND wp.product_id = op.product_id;
END IF;

RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER order_cancelled_trigger
    AFTER UPDATE ON "order"
    FOR EACH ROW
    WHEN (NEW.status = 'cancelled' AND (OLD.status IS NULL OR OLD.status != 'cancelled'))
    EXECUTE FUNCTION fn_order_cancelled_trigger();

-- ==============================================
-- EVENTS (pg_cron)
-- ==============================================

-- Function to reindex all tables (used by cron job)
CREATE OR REPLACE FUNCTION fn_reindex_all_tables()
RETURNS void AS $$
DECLARE
v_table_name TEXT;
BEGIN
    -- Reindex all user tables
FOR v_table_name IN
SELECT tablename
FROM pg_tables
WHERE schemaname = 'public'
    LOOP
BEGIN
EXECUTE format('REINDEX TABLE %I', v_table_name);
EXCEPTION
            WHEN OTHERS THEN
                RAISE NOTICE 'Failed to reindex table %: %', v_table_name, SQLERRM;
END;
END LOOP;

    -- Reindex all materialized views
FOR v_table_name IN
SELECT matviewname
FROM pg_matviews
WHERE schemaname = 'public'
    LOOP
BEGIN
EXECUTE format('REINDEX MATERIALIZED VIEW %I', v_table_name);
EXCEPTION
            WHEN OTHERS THEN
                RAISE NOTICE 'Failed to reindex materialized view %: %', v_table_name, SQLERRM;
END;
END LOOP;
END;
$$ LANGUAGE plpgsql;

-- Reindex all Indexes Event: Runs daily at 2 AM
SELECT cron.schedule(
               'reindex-all-indexes',
               '0 2 * * *', -- Daily at 2 AM
               'SELECT fn_reindex_all_tables();'
       );

-- Order Abandonment Follow-up Event: Runs every 1 hour
SELECT cron.schedule(
               'order-abandonment-followup',
               '0 * * * *', -- Every hour
               $$
                   BEGIN
        DECLARE
            v_order_record RECORD;
-- Identify orders in PENDING status older than 1 hour
FOR v_order_record IN
SELECT order_id
FROM "order"
WHERE status = 'pending'
  AND created_at < NOW() - INTERVAL '1 hour'
  AND deleted_at IS NULL
    LOOP
-- Restore inventory for abandoned orders
UPDATE warehouse_product wp
SET stock_quantity = wp.stock_quantity + op.quantity
    FROM order_product op
WHERE op.order_id = v_order_record.order_id
  AND wp.product_id = op.product_id;
END LOOP;
END;
$$
);

-- Refresh Materialized Views Event: Runs daily at 3 AM
SELECT cron.schedule(
               'refresh-materialized-views',
               '0 3 * * *', -- Daily at 3 AM
               'SELECT fn_refresh_materialized_views();'
       );